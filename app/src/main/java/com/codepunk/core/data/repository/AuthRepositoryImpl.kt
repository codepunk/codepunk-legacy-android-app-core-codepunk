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
import com.codepunk.core.data.mapper.toDomainOrNull
import com.codepunk.core.data.remote.entity.RemoteAuthentication
import com.codepunk.core.data.remote.entity.RemoteMessage
import com.codepunk.core.data.remote.entity.RemoteUser
import com.codepunk.core.data.remote.webservice.AuthWebservice
import com.codepunk.core.data.remote.webservice.UserWebservice
import com.codepunk.core.domain.contract.AuthRepository
import com.codepunk.core.domain.model.Authentication
import com.codepunk.core.domain.model.Message
import com.codepunk.core.exception.OAuthServerException
import com.codepunk.core.exception.ValidationException
import com.codepunk.core.lib.getResultUpdate
import com.codepunk.core.lib.toRemoteOAuthServerExceptionResponse
import com.codepunk.core.lib.toRemoteValidationExceptionResponse
import com.codepunk.core.util.NetworkTranslator
import com.codepunk.doofenschmirtz.util.http.HttpStatus
import com.codepunk.doofenschmirtz.util.http.HttpStatusException
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

    override fun sendActivationEmail(
        email: String
    ): LiveData<DataUpdate<Void, Message>> = ActivationCodeTask(
        retrofit, networkTranslator, authWebservice, email
    ).apply {
        sendActivationCodeTask = this
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
                    // TODO This is duplicated in ActivationCodeTask
                    val e = when (val updateException = update.e) {
                        is HttpStatusException -> {
                            when (updateException.httpStatus.category) {
                                HttpStatus.Category.CLIENT_ERROR -> {
                                    val response = update.result?.errorBody()
                                        .toRemoteOAuthServerExceptionResponse(retrofit)
                                    OAuthServerException.from(response)
                                }
                                else -> updateException
                            }
                        }
                        else -> update.e
                    }
                    FailureUpdate(null, e)
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

    private class RegisterTask(

        private val retrofit: Retrofit,

        private val networkTranslator: NetworkTranslator,

        private val authWebservice: AuthWebservice,

        private val username: String,

        private val email: String,

        private val password: String,

        private val passwordConfirmation: String

    ) : DataTaskinator<Void, Void, Message>() {

        // region Inherited methods

        override fun doInBackground(vararg params: Void?): ResultUpdate<Void, Message> {
            val update: ResultUpdate<Void, Response<RemoteMessage>> =
                authWebservice.register(username, email, password, passwordConfirmation)
                    .getResultUpdate()
            return when (update) {
                is FailureUpdate -> {
                    val e = when (val updateException = update.e) {
                        is HttpStatusException -> {
                            when (updateException.httpStatus) {
                                HttpStatus.UNPROCESSABLE_ENTITY -> {
                                    val response = update.result?.errorBody()
                                        .toRemoteValidationExceptionResponse(retrofit)
                                    ValidationException(
                                        response?.message,
                                        update.e,
                                        response?.errors
                                    )
                                }
                                else -> updateException
                            }
                        }
                        else -> updateException
                    }
                    FailureUpdate(null, e)
                }
                else -> SuccessUpdate(update.result?.body().toDomainOrNull(networkTranslator))
            }
        }

        // endregion Inherited methods

    }

    private class ActivationCodeTask(

        private val retrofit: Retrofit,

        private val networkTranslator: NetworkTranslator,

        private val authWebservice: AuthWebservice,

        private val email: String

    ) : DataTaskinator<Void, Void, Message>() {

        override fun doInBackground(vararg params: Void?): ResultUpdate<Void, Message> {
            val update: ResultUpdate<Void, Response<RemoteMessage>> =
                authWebservice.sendActivationCode(email).getResultUpdate()
            return when (update) {
                is FailureUpdate -> {
                    // TODO This is duplicated in AuthenticateTask
                    val e = when (val updateException = update.e) {
                        is HttpStatusException -> {
                            when (updateException.httpStatus.category) {
                                HttpStatus.Category.CLIENT_ERROR -> {
                                    val response = update.result?.errorBody()
                                        .toRemoteOAuthServerExceptionResponse(retrofit)
                                    OAuthServerException.from(response)
                                }
                                else -> updateException
                            }
                        }
                        else -> update.e
                    }
                    FailureUpdate(null, e)
                }
                else -> {
                    val networkResponse =
                        update.result?.body().toDomainOrNull(networkTranslator)
                    SuccessUpdate(networkResponse)
                }
            }
        }
    }

    // endregion Nested/inner classes

    // region Companion object

    companion object {

        /**
         * Retrieves the [RemoteUser] from the network using the given [authToken].
         */
        @JvmStatic
        @WorkerThread
        private fun getRemoteUser(
            userWebservice: UserWebservice,
            authToken: String?
        ): RemoteUser? = when (authToken) {
            null -> null
            else -> {
                val response = userWebservice.getUser(authToken).execute()
                if (response.isSuccessful) {
                    response.body()
                } else {
                    null
                }
            }
        }

    }

    // endregion Companion object

}
