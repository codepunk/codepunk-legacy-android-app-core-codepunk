/*
 * Copyright (C) 2018 Codepunk, LLC
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

package com.codepunk.core.presentation.auth

import android.accounts.AccountManager
import android.accounts.AccountManager.*
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Patterns
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.codepunk.core.BuildConfig.AUTHENTICATOR_ACCOUNT_TYPE
import com.codepunk.core.BuildConfig.KEY_RESPONSE_MESSAGE
import com.codepunk.core.data.remote.entity.RemoteUser
import com.codepunk.core.data.remote.entity.auth.RemoteAuthorization
import com.codepunk.core.data.remote.entity.http.RemoteMessage
import com.codepunk.core.data.remote.webservice.AuthWebservice
import com.codepunk.core.data.remote.webservice.UserWebservice
import com.codepunk.core.lib.*
import com.codepunk.doofenschmirtz.util.taskinator.*
import retrofit2.Response
import retrofit2.Retrofit
import javax.inject.Inject

/**
 * A [ViewModel] for managing account-related data.
 */
class AuthViewModel @Inject constructor(

    /**
     * The [Retrofit] instance.
     */
    private val retrofit: Retrofit,

    /**
     * The authorization webservice.
     */
    private val authWebservice: AuthWebservice,

    /**
     * The user web service.
     */
    private val userWebservice: UserWebservice

) : ViewModel() {

    // region Properties

    /**
     * A [LiveData] holding the [RemoteAuthorization] relating to the current authorization attempt.
     */
    val authorizationDataUpdate =
        MediatorLiveData<DataUpdate<RemoteMessage, Response<RemoteAuthorization>>>()

    // endregion Properties

    // region Methods

    /**
     * Synchronously calls the authorize endpoint and adds a [Bundle] needed by the
     * [AccountManager].
     */
    @WorkerThread
    private fun getAuthToken(
        usernameOrEmail: String,
        password: String
    ): ResultUpdate<RemoteMessage, Response<RemoteAuthorization>> {
        // TODO go through AccountAuthenticator somehow? Probably not because I need to create
        // an account

        // Call the authorize endpoint
        val authTokenUpdate: ResultUpdate<RemoteMessage, Response<RemoteAuthorization>> =
            authWebservice.authorize(usernameOrEmail, password).getResultUpdate()

        // Process the authorize endpoint result
        when (authTokenUpdate) {
            is SuccessUpdate -> {
                authTokenUpdate.result?.body()?.let {
                    val isEmail = Patterns.EMAIL_ADDRESS.matcher(usernameOrEmail).matches()
                    val username = when (isEmail) {
                        true -> {
                            val userUpdate: ResultUpdate<Void, Response<RemoteUser>> =
                                userWebservice.getUser().getResultUpdate()
                            val user = userUpdate.result?.body()
                            when (userUpdate) {
                                is SuccessUpdate -> user?.username
                                    ?: return FailureUpdate() // TODO ??
                                is FailureUpdate -> return FailureUpdate() // TODO
                                else -> return FailureUpdate() // TODO
                            }
                        }
                        false -> usernameOrEmail
                    }

                    authTokenUpdate.data = Bundle().apply {
                        putString(KEY_ACCOUNT_NAME, username)
                        putString(KEY_ACCOUNT_TYPE, AUTHENTICATOR_ACCOUNT_TYPE)
                        putString(KEY_AUTHTOKEN, it.authToken)
                        putString(KEY_PASSWORD, it.refreshToken)
                    }
                }
            }
        }
        return authTokenUpdate
    }

    /**
     * Authenticates using username (or email) and password.
     */
    @SuppressLint("StaticFieldLeak")
    fun authenticate(usernameOrEmail: String, password: String) {
        val task =
            object : DataTaskinator<Void, RemoteMessage, Response<RemoteAuthorization>>() {
                override fun doInBackground(vararg params: Void?):
                        ResultUpdate<RemoteMessage, Response<RemoteAuthorization>> =
                    getAuthToken(usernameOrEmail, password)
            }
        authorizationDataUpdate.addSource(task.executeOnExecutorAsLiveData()) {
            authorizationDataUpdate.value = it
        }
    }

    /**
     * Registers a new user. Note that this user will not be activated as the user will
     * still need to respond to the activation email.
     */
    @SuppressLint("StaticFieldLeak")
    fun register(
        username: String,
        email: String,
        givenName: String,
        familyName: String,
        password: String,
        passwordConfirmation: String
    ) {
        val task =
            object : DataTaskinator<Void, RemoteMessage, Response<RemoteAuthorization>>() {
                override fun doInBackground(vararg params: Void?):
                        ResultUpdate<RemoteMessage, Response<RemoteAuthorization>> {
                    // Call the register endpoint
                    val update: ResultUpdate<Void, Response<RemoteMessage>> =
                        authWebservice.register(
                            username,
                            email,
                            givenName,
                            familyName,
                            password,
                            passwordConfirmation
                        ).getResultUpdate()

                    // Process the register endpoint result
                    return when (update) {
                        is SuccessUpdate -> {
                            publishProgress(update.result?.body())
                            getAuthToken(username, password)
                        }
                        is FailureUpdate -> {
                            val remoteMessage =
                                RemoteMessage("BAD!") // TODO TEMP update.response.toMessage(retrofit)
                            FailureUpdate(
                                e = update.e,
                                data = Bundle().apply {
                                    putParcelable(KEY_RESPONSE_MESSAGE, remoteMessage)
                                }
                            )
                        }
                        else -> FailureUpdate()
                    }
                }
            }
        authorizationDataUpdate.addSource(task.executeOnExecutorAsLiveData()) {
            authorizationDataUpdate.value = it
        }
    }

    // endregion methods

    // region Companion object

    companion object {

        // region Methods

        /**
         * Extracts a [RemoteMessage] from a [Response], or converts the error body message
         * to a [RemoteMessage].
         */
        @Suppress("UNUSED")
        private fun toMessage(
            response: Response<RemoteMessage>,
            retrofit: Retrofit
        ): RemoteMessage? {
            return when {
                response.isSuccessful -> response.body()
                else -> response.errorBody()?.run {
                    retrofit.responseBodyConverter<RemoteMessage>(
                        RemoteMessage::class.java,
                        arrayOf()
                    ).convert(this)
                }
            }
        }

        // endregion Methods

    }

    // endregion Companion object

}