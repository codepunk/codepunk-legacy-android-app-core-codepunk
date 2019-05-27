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
import com.codepunk.core.lib.retrofit.getResultResource
import com.codepunk.core.lib.retrofit.toRemoteErrorBody
import com.codepunk.doofenschmirtz.util.Translatinator
import com.codepunk.doofenschmirtz.util.resourceinator.*
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
     * An instance of [Retrofit] for error deserialization.
     */
    private val retrofit: Retrofit,

    /**
     * An instance of [Translatinator] for translating network messages.
     */
    private val translatinator: Translatinator

) : AuthRepository {

    // region Properties

    /**
     * An instance of [AuthenticateResourceinator] for authenticating a user.
     */
    private var authenticateResourceinator: AuthenticateResourceinator? = null
        set(value) {
            if (field != value) {
                field?.cancel(true)
                field = value
            }
        }

    /**
     * An instance of [RegisterResourceinator] for registering a new user account.
     */
    private var registerResourceinator: RegisterResourceinator? = null
        set(value) {
            if (field != value) {
                field?.cancel(true)
                field = value
            }
        }

    /**
     * An instance of [ActivationLinkResourceinator] for requesting an activation link.
     */
    private var sendActivationLinkResourceinator: ActivationLinkResourceinator? = null
        set(value) {
            if (field != value) {
                field?.cancel(true)
                field = value
            }
        }

    /**
     * An instance of [PasswordResetLinkResourceinator] for requesting a password reset link.
     */
    private var sendPasswordResetLinkResourceinator: PasswordResetLinkResourceinator? = null
        set(value) {
            if (field != value) {
                field?.cancel(true)
                field = value
            }
        }

    // endregion Properties

    // region Implemented methods

    /**
     * Authenticates a user.
     */
    override fun authenticate(
        usernameOrEmail: String,
        password: String
    ): LiveData<Resource<Void, Authentication>> = AuthenticateResourceinator(
        retrofit,
        authWebservice,
        userWebservice,
        usernameOrEmail,
        password
    ).apply {
        authenticateResourceinator = this
    }.executeOnExecutorAsLiveData()

    /**
     * Registers a new user account.
     */
    override fun register(
        username: String,
        email: String,
        password: String,
        passwordConfirmation: String
    ): LiveData<Resource<Void, Message>> = RegisterResourceinator(
        retrofit,
        translatinator,
        authWebservice,
        username,
        email,
        password,
        passwordConfirmation
    ).apply {
        registerResourceinator = this
    }.executeOnExecutorAsLiveData()

    /**
     * Sends an activation link to the supplied [email].
     */
    override fun sendActivationLink(
        email: String
    ): LiveData<Resource<Void, Message>> = ActivationLinkResourceinator(
        retrofit, translatinator, authWebservice, email
    ).apply {
        sendActivationLinkResourceinator = this
    }.executeOnExecutorAsLiveData()

    /**
     * Sends a password reset link to the supplied [email].
     */
    override fun sendPasswordResetLink(
        email: String
    ): LiveData<Resource<Void, Message>> = PasswordResetLinkResourceinator(
        retrofit, translatinator, authWebservice, email
    ).apply {
        sendPasswordResetLinkResourceinator = this
    }.executeOnExecutorAsLiveData()

    // endregion Implemented methods

    // region Nested/inner classes

    /**
     * An abstract [Resourceinator] class that handles user account-related requests (i.e.
     * authentication, registration, activation links, password reset links, etc.).
     */
    private abstract class AbsAuthResourceinator<Params, Progress, Result, RemoteResult>(

        /**
         * A [Retrofit] instance for error deserialization.
         */
        protected val retrofit: Retrofit

    ) : Resourceinator<Params, Progress, Result>() {

        // region Inherited methods

        /**
         * Gets the [ResultResource] and deserializes any existing error body in the case of an
         * error.
         */
        override fun doInBackground(vararg params: Params): ResultResource<Progress, Result> =
            when (val resource = getResultResource(*params)) {
                is FailureResource -> {
                    val data = try {
                        Bundle().apply {
                            putParcelable(
                                KEY_REMOTE_ERROR_BODY,
                                resource.result?.errorBody()?.toRemoteErrorBody(retrofit)
                            )
                        }
                    } catch (e: Exception) {
                        null
                    }
                    FailureResource(null, resource.e, data)
                }
                else -> onSuccess(resource as SuccessResource<Progress, Response<RemoteResult>>)
            }

        // endregion Inherited methods

        // region Methods

        /**
         * Gets a result resource by calling a method on the appropriate webservice.
         */
        abstract fun getResultResource(vararg params: Params):
            ResultResource<Progress, Response<RemoteResult>>

        /**
         * Handles a successful result resource.
         */
        abstract fun onSuccess(resource: SuccessResource<Progress, Response<RemoteResult>>):
            ResultResource<Progress, Result>


        // endregion Methods

    }

    /**
     * A [Resourceinator] class that handles user authentication.
     */
    private class AuthenticateResourceinator(

        retrofit: Retrofit,

        /**
         * An instance of [AuthWebservice].
         */
        private val authWebservice: AuthWebservice,

        /**
         * An instance of [UserWebservice].
         */
        private val userWebservice: UserWebservice,

        /**
         * The user's username or email.
         */
        private val usernameOrEmail: String,

        /**
         * The user's password.
         */
        private val password: String

    ) : AbsAuthResourceinator<Void, Void, Authentication, RemoteAuthentication>(retrofit) {

        override fun getResultResource(vararg params: Void):
            ResultResource<Void, Response<RemoteAuthentication>> =
            authWebservice.authorize(usernameOrEmail, password).getResultResource()

        override fun onSuccess(resource: SuccessResource<Void, Response<RemoteAuthentication>>):
            ResultResource<Void, Authentication> {
            val remoteAuthentication = resource.result?.body()
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
                null -> FailureResource(
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
                    SuccessResource(authentication, data)
                }
            }
        }

    }

    /**
     * An abstract [Resourceinator] class that delivers a simple [Message] result.
     */
    private abstract class AbsMessageResourceinator(

        retrofit: Retrofit,

        /**
         * An instance of [Translatinator] for translating messages from the network.
         */
        private val translatinator: Translatinator

    ) : AbsAuthResourceinator<Void, Void, Message, RemoteMessage>(retrofit) {

        // region Inherited methods

        override fun onSuccess(resource: SuccessResource<Void, Response<RemoteMessage>>):
            ResultResource<Void, Message> = SuccessResource(
            resource.result?.body().toDomainOrNull(translatinator)
        )

        // endregion Inherited methods

    }

    /**
     * A [Resourceinator] class that handles new user account creation.
     */
    private class RegisterResourceinator(

        retrofit: Retrofit,

        translatinator: Translatinator,

        /**
         * An instance of [AuthWebservice].
         */
        private val authWebservice: AuthWebservice,

        /**
         * The user's username.
         */
        private val username: String,

        /**
         * The user's email.
         */
        private val email: String,

        /**
         * The user's password.
         */
        private val password: String,

        /**
         * The user's password again, which the user is required to re-enter for confirmation.
         */
        private val passwordConfirmation: String

    ) : AbsMessageResourceinator(retrofit, translatinator) {

        // region Inherited methods

        override fun getResultResource(vararg params: Void):
            ResultResource<Void, Response<RemoteMessage>> = authWebservice.register(
            username,
            email,
            password,
            passwordConfirmation
        ).getResultResource()

        // endregion Inherited methods

    }

    /**
     * A [Resourceinator] class that handles requesting a user activation link via email.
     */
    private class ActivationLinkResourceinator(

        retrofit: Retrofit,

        translatinator: Translatinator,

        /**
         * An instance of [AuthWebservice].
         */
        private val authWebservice: AuthWebservice,

        /**
         * The user's email.
         */
        private val email: String

    ) : AbsMessageResourceinator(retrofit, translatinator) {

        // region Inherited methods

        override fun getResultResource(vararg params: Void):
            ResultResource<Void, Response<RemoteMessage>> =
            authWebservice.sendActivationLink(email).getResultResource()

        // endregion Inherited methods

    }

    /**
     * A [Resourceinator] class that handles requesting a password reset link via email.
     */
    private class PasswordResetLinkResourceinator(

        retrofit: Retrofit,

        translatinator: Translatinator,

        /**
         * An instance of [AuthWebservice].
         */
        private val authWebservice: AuthWebservice,

        /**
         * The user's email.
         */
        private val email: String

    ) : AbsMessageResourceinator(retrofit, translatinator) {

        // region Inherited methods

        override fun getResultResource(vararg params: Void):
            ResultResource<Void, Response<RemoteMessage>> =
            authWebservice.sendPasswordResetLink(email).getResultResource()

        // endregion Inherited methods

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
