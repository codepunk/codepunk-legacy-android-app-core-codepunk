/*
 * Copyright (C) 2019 Codepunk, LLC
 * Author(s): Scott Slater
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// TODO In general handle when tasks get canceled?

package com.codepunk.core.data.repository

import android.accounts.AccountManager.*
import android.os.Bundle
import android.util.Patterns
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.codepunk.core.BuildConfig.AUTHENTICATOR_ACCOUNT_TYPE
import com.codepunk.core.BuildConfig.KEY_REMOTE_ERROR_BODY
import com.codepunk.core.data.mapper.toDomainOrNull
import com.codepunk.core.data.remote.entity.RemoteAuthentication
import com.codepunk.core.data.remote.entity.RemoteMessage
import com.codepunk.core.data.remote.entity.RemoteUser
import com.codepunk.core.data.remote.webservice.AuthWebservice
import com.codepunk.core.data.remote.webservice.UserWebservice
import com.codepunk.core.domain.contract.AuthRepository
import com.codepunk.core.domain.model.Authentication
import com.codepunk.core.domain.model.Message
import com.codepunk.core.lib.getResultUpdate
import com.codepunk.core.lib.toRemoteErrorBody
import com.codepunk.core.util.NetworkTranslator
import com.codepunk.doofenschmirtz.util.taskinator.*
import retrofit2.Response
import retrofit2.Retrofit

/**
 * Implementation of [AuthRepository] that authenticates a user and performs other authentication-
 * related functions.
 */
class AuthRepositoryImpl(

    /**
     * An instance of [AuthWebservice] for making auth-related API calls.
     */
    private val authWebservice: AuthWebservice,

    /**
     * An instance of [UserWebservice] for making user-related API calls.
     */
    private val userWebservice: UserWebservice,

    /**
     * An instance of [Retrofit] for deserializing errors.
     */
    private val retrofit: Retrofit,

    /**
     * An instance of [NetworkTranslator] for translating network messages.
     */
    private val networkTranslator: NetworkTranslator

) : AuthRepository {

    // region Properties

    private var authenticateTask: AuthenticateTask? = null
        set(value) {
            if (field != value) {
                field?.cancel(true)
                field = value
            }
        }

    private var registerTask: RegisterTask? = null
        set(value) {
            if (field != value) {
                field?.cancel(true)
                field = value
            }
        }

    private var sendActivationCodeTask: ActivationCodeTask? = null
        set(value) {
            if (field != value) {
                field?.cancel(true)
                field = value
            }
        }

    private var sendPasswordResetLinkTask: PasswordResetLinkTask? = null
        set(value) {
            if (field != value) {
                field?.cancel(true)
                field = value
            }
        }

    // endregion Properties

    // region Implemented methods

    override fun authenticate(
        username: String,
        password: String
    ): LiveData<DataUpdate<Void, Authentication>> = AuthenticateTask(
        retrofit,
        networkTranslator,
        authWebservice,
        userWebservice,
        username,
        password
    ).apply {
        authenticateTask = this
    }.executeOnExecutorAsLiveData()

    override fun register(
        username: String,
        email: String,
        password: String,
        passwordConfirmation: String
    ): LiveData<DataUpdate<Void, Message>> = RegisterTask(
        retrofit,
        networkTranslator,
        authWebservice,
        username,
        email,
        password,
        passwordConfirmation
    ).apply {
        registerTask = this
    }.executeOnExecutorAsLiveData()

    override fun sendActivationCode(
        email: String
    ): LiveData<DataUpdate<Void, Message>> = ActivationCodeTask(
        retrofit, networkTranslator, authWebservice, email
    ).apply {
        sendActivationCodeTask = this
    }.executeOnExecutorAsLiveData()

    override fun sendPasswordResetLink(
        email: String
    ): LiveData<DataUpdate<Void, Message>> = PasswordResetLinkTask(
        retrofit, networkTranslator, authWebservice, email
    ).apply {
        sendPasswordResetLinkTask = this
    }.executeOnExecutorAsLiveData()

    // endregion Implemented methods

    // region Nested/inner classes

    private class AuthenticateTask(

        private val retrofit: Retrofit,

        private val networkTranslator: NetworkTranslator,

        private val authWebservice: AuthWebservice,

        private val userWebservice: UserWebservice,

        private val usernameOrEmail: String,

        private val password: String

    ) : DataTaskinator<Void, Void, Authentication>() {

        // region Inherited methods

        override fun doInBackground(vararg params: Void?): ResultUpdate<Void, Authentication> {
            val update: ResultUpdate<Void, Response<RemoteAuthentication>> =
                authWebservice.authorize(usernameOrEmail, password).getResultUpdate()
            return when (update) {
                is FailureUpdate -> {
                    // TODO This is duplicated in AccountTask (and maybe elsewhere)
                    val data = Bundle().apply {
                        putParcelable(
                            KEY_REMOTE_ERROR_BODY,
                            update.result?.errorBody()?.toRemoteErrorBody(retrofit)
                        )
                    }
                    FailureUpdate(null, update.e, data)
                }
                else -> {
                    val remoteAuthentication = update.result?.body()
                    val username: String? =
                        when (Patterns.EMAIL_ADDRESS.matcher(usernameOrEmail).matches()) {
                            true -> getRemoteUser(
                                userWebservice,
                                remoteAuthentication?.authToken
                            )?.username
                            else -> usernameOrEmail
                        }
                    val authentication =
                        remoteAuthentication.toDomainOrNull(username ?: "")
                    when (username) {
                        null -> FailureUpdate(
                            authentication,
                            IllegalStateException("Unable to determine username")
                        )
                        else -> {
                            val data = Bundle().apply {
                                putString(KEY_ACCOUNT_NAME, username)
                                putString(KEY_ACCOUNT_TYPE, AUTHENTICATOR_ACCOUNT_TYPE)
                                putString(KEY_AUTHTOKEN, remoteAuthentication?.authToken)
                                putString(KEY_PASSWORD, remoteAuthentication?.refreshToken)
                            }
                            SuccessUpdate(authentication, data)
                        }
                    }
                }
            }
        }

        /*
        override fun doInBackground(vararg params: Void?):
            ResultUpdate<Void, Authentication> {
            return when (val update: ResultUpdate<Void, Response<RemoteAuthentication>> =
                authWebservice.authorize(usernameOrEmail, password).getResultUpdate()) {
                is FailureUpdate -> {
                    val errorBody = update.result?.errorBody()

                    // TODO Change this to RemoteException, create new Exception from the RemoteException
                    val networkResponse = errorBody
                        .toRemoteNetworkResponse(retrofit)
                        .toDomainOrNull(networkTranslator)
                    val e = when (networkResponse?.error) {
                        NetworkResponse.INVALID_CREDENTIALS -> InvalidCredentialsException(
                            networkResponse.localizedMessage,
                            update.e
                        )
                        NetworkResponse.INACTIVE_USER -> {
                            networkResponse.hint?.let {
                                InactiveUserException(
                                    it,
                                    networkResponse.localizedMessage,
                                    update.e
                                )
                            } ?: IllegalStateException("Unable to determine email") // TODO Duplicate this in new setup
                        }
                        else -> update.e
                    }

                    val data = Bundle().apply {
                        putParcelable(KEY_MESSAGE, networkResponse)
                    }

                    FailureUpdate(null, e, data)
                }
                else -> {
                    val remoteAuthentication = update.result?.body()
                    val username: String? =
                        when (Patterns.EMAIL_ADDRESS.matcher(usernameOrEmail).matches()) {
                            true -> getRemoteUser(
                                userWebservice,
                                remoteAuthentication?.authToken
                            )?.username
                            else -> usernameOrEmail
                        }
                    val authentication =
                        remoteAuthentication.toDomainOrNull(username ?: "")
                    when (username) {
                        null -> FailureUpdate(
                            authentication,
                            IllegalStateException("Unable to determine username")
                        )
                        else -> {
                            val data = Bundle().apply {
                                putString(KEY_ACCOUNT_NAME, username)
                                putString(KEY_ACCOUNT_TYPE, AUTHENTICATOR_ACCOUNT_TYPE)
                                putString(KEY_AUTHTOKEN, remoteAuthentication?.authToken)
                                putString(KEY_PASSWORD, remoteAuthentication?.refreshToken)
                            }
                            SuccessUpdate(authentication, data)
                        }
                    }
                }
            }
        }
        */

        // endregion Inherited methods

    }

    private abstract class AccountTask(

        protected val retrofit: Retrofit,

        protected val networkTranslator: NetworkTranslator

    ) : DataTaskinator<Void, Void, Message>() {

        // region Inherited methods

        override fun doInBackground(vararg params: Void?): ResultUpdate<Void, Message> {
            return when (val update = getResultUpdate()) {
                is FailureUpdate -> {
                    // TODO This is duplicated in AuthenticateTask (and maybe elsewhere)
                    val data = Bundle().apply {
                        putParcelable(
                            KEY_REMOTE_ERROR_BODY,
                            update.result?.errorBody()?.toRemoteErrorBody(retrofit)
                        )
                    }
                    FailureUpdate(null, update.e, data)
                }
                else -> {
                    val networkResponse =
                        update.result?.body().toDomainOrNull(networkTranslator)
                    SuccessUpdate(networkResponse)
                }
            }
        }

        // endregion Inherited methods

        // region Methods

        abstract fun getResultUpdate(): ResultUpdate<Void, Response<RemoteMessage>>

        // endregion Methods

    }

    private class RegisterTask(

        retrofit: Retrofit,

        networkTranslator: NetworkTranslator,

        private val authWebservice: AuthWebservice,

        private val username: String,

        private val email: String,

        private val password: String,

        private val passwordConfirmation: String

    ) : AccountTask(retrofit, networkTranslator) {

        // region Inherited methods

        override fun getResultUpdate(): ResultUpdate<Void, Response<RemoteMessage>> =
            authWebservice.register(username, email, password, passwordConfirmation)
                .getResultUpdate()

        // endregion Inherited methods

    }

    private abstract class OAuthTask(

        protected val retrofit: Retrofit,

        protected val networkTranslator: NetworkTranslator

    ) : DataTaskinator<Void, Void, Message>() {

        // region Inherited methods

        override fun doInBackground(vararg params: Void?): ResultUpdate<Void, Message> {
            return when (val update = getResultUpdate()) {
                is FailureUpdate -> {
                    // TODO This is duplicated in AuthenticateTask (and maybe elsewhere)
                    val data = Bundle().apply {
                        putParcelable(
                            KEY_REMOTE_ERROR_BODY,
                            update.result?.errorBody()?.toRemoteErrorBody(retrofit)
                        )
                    }
                    FailureUpdate(null, update.e, data)
                }
                else -> {
                    val networkResponse =
                        update.result?.body().toDomainOrNull(networkTranslator)
                    SuccessUpdate(networkResponse)
                }
            }
        }

        // endregion Inherited methods

        // region Methods

        abstract fun getResultUpdate(): ResultUpdate<Void, Response<RemoteMessage>>

        // endregion Methods

    }

    private class ActivationCodeTask(

        retrofit: Retrofit,

        networkTranslator: NetworkTranslator,

        private val authWebservice: AuthWebservice,

        private val email: String

    ) : OAuthTask(retrofit, networkTranslator) {

        // region Inherited methods

        override fun getResultUpdate(): ResultUpdate<Void, Response<RemoteMessage>> =
            authWebservice.sendActivationCode(email).getResultUpdate()

        // endregion Inherited methods

    }

    private class PasswordResetLinkTask(

        retrofit: Retrofit,

        networkTranslator: NetworkTranslator,

        private val authWebservice: AuthWebservice,

        private val email: String

    ) : AccountTask(retrofit, networkTranslator) {

        override fun getResultUpdate(): ResultUpdate<Void, Response<RemoteMessage>> =
            authWebservice.sendPasswordResetLink(email).getResultUpdate()

    }

    // endregion Nested/inner classes

    // region Companion object

    companion object {

        // region Methods

        /**
         * Retrieves the [RemoteUser] from the network using the given [authToken].
         */
        @JvmStatic
        @WorkerThread
        private fun getRemoteUser(
            userWebservice: UserWebservice,
            authToken: String?
        ): RemoteUser? = authToken?.run {
            userWebservice.getUser(this).execute().run {
                when (isSuccessful) {
                    true -> body()
                    else -> null
                }
            }
        }

        // endregion Methods

    }

    // endregion Companion object

}
