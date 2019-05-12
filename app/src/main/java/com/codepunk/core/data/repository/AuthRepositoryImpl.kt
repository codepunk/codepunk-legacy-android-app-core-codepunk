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

    private abstract class AbsAuthTask<Params, Progress, Result, RemoteResult>(
        protected val retrofit: Retrofit
    ) : DataTaskinator<Params, Progress, Result>() {

        override fun doInBackground(vararg params: Params): ResultUpdate<Progress, Result> {
            val update = getResultUpdate(*params)
            return when (update) {
                is FailureUpdate -> {
                    val data = Bundle().apply {
                        putParcelable(
                            KEY_REMOTE_ERROR_BODY,
                            update.result?.errorBody()?.toRemoteErrorBody(retrofit)
                        )
                    }
                    FailureUpdate(null, update.e, data)
                }
                else -> onSuccessUpdate(update as SuccessUpdate<Progress, Response<RemoteResult>>)
            }
        }

        abstract fun getResultUpdate(vararg params: Params): ResultUpdate<Progress, Response<RemoteResult>>

        abstract fun onSuccessUpdate(update: SuccessUpdate<Progress, Response<RemoteResult>>):
            ResultUpdate<Progress, Result>

    }

    private class AuthenticateTask(

        retrofit: Retrofit,

        private val authWebservice: AuthWebservice,

        private val userWebservice: UserWebservice,

        private val usernameOrEmail: String,

        private val password: String

    ) : AbsAuthTask<Void, Void, Authentication, RemoteAuthentication>(retrofit) {

        override fun getResultUpdate(vararg params: Void):
            ResultUpdate<Void, Response<RemoteAuthentication>> =
            authWebservice.authorize(usernameOrEmail, password).getResultUpdate()

        override fun onSuccessUpdate(update: SuccessUpdate<Void, Response<RemoteAuthentication>>):
            ResultUpdate<Void, Authentication> {
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
            return when (username) {
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

    private abstract class MessageTask(

        retrofit: Retrofit,

        private val networkTranslator: NetworkTranslator

    ) : AbsAuthTask<Void, Void, Message, RemoteMessage>(retrofit) {

        override fun onSuccessUpdate(update: SuccessUpdate<Void, Response<RemoteMessage>>):
            ResultUpdate<Void, Message> = SuccessUpdate(
            update.result?.body().toDomainOrNull(networkTranslator)
        )

    }

    private class RegisterTask(

        retrofit: Retrofit,

        networkTranslator: NetworkTranslator,

        private val authWebservice: AuthWebservice,

        private val username: String,

        private val email: String,

        private val password: String,

        private val passwordConfirmation: String

    ) : MessageTask(retrofit, networkTranslator) {

        override fun getResultUpdate(vararg params: Void):
            ResultUpdate<Void, Response<RemoteMessage>> = authWebservice.register(
            username,
            email,
            password,
            passwordConfirmation
        ).getResultUpdate()

    }

    private class ActivationCodeTask(

        retrofit: Retrofit,

        networkTranslator: NetworkTranslator,

        private val authWebservice: AuthWebservice,

        private val email: String

    ) : MessageTask(retrofit, networkTranslator) {

        override fun getResultUpdate(vararg params: Void):
            ResultUpdate<Void, Response<RemoteMessage>> =
            authWebservice.sendActivationCode(email).getResultUpdate()

    }

    private class PasswordResetLinkTask(

        retrofit: Retrofit,

        networkTranslator: NetworkTranslator,

        private val authWebservice: AuthWebservice,

        private val email: String

    ) : MessageTask(retrofit, networkTranslator) {

        override fun getResultUpdate(vararg params: Void):
            ResultUpdate<Void, Response<RemoteMessage>> =
            authWebservice.sendPasswordResetLink(email).getResultUpdate()

    }

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
