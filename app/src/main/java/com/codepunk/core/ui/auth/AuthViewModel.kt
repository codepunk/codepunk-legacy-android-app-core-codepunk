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

package com.codepunk.core.ui.auth

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
import com.codepunk.core.data.model.User
import com.codepunk.core.data.model.auth.AccessToken
import com.codepunk.core.data.model.http.ResponseMessage
import com.codepunk.core.data.remote.interceptor.AuthorizationInterceptor
import com.codepunk.core.data.remote.webservice.AuthWebservice
import com.codepunk.core.data.remote.webservice.UserWebservice
import com.codepunk.core.lib.*
import retrofit2.Response
import retrofit2.Retrofit
import javax.inject.Inject

/**
 * A [ViewModel] for managing account-related data.
 */
class AuthViewModel @Inject constructor(

    /**
     * The authorization webservice.
     */
    private val authWebservice: AuthWebservice,

    /**
     * The user webserverice.
     */
    private val userWebservice: UserWebservice,

    /**
     * The authorization interception, which handles adding access tokens to API requests.
     */
    private val authorizationInterceptor: AuthorizationInterceptor

) : ViewModel() {

    // region Properties

    /**
     * A [LiveData] holding the [AccessToken] information relating to the current authorization
     * attempt.
     */
    val accessTokenDataUpdate = MediatorLiveData<DataUpdate<ResponseMessage, AccessToken>>()

    // endregion Properties

    // region Methods

    /**
     * Synchronously calls the getAuthToken endpoint and adds a [Bundle] needed by the
     * [AccountManager].
     *
     */
    @WorkerThread
    private fun getAuthToken(
        usernameOrEmail: String,
        password: String
    ): DataUpdate<ResponseMessage, AccessToken> {
        // Call the getAuthToken endpoint
        val authTokenUpdate: DataUpdate<ResponseMessage, AccessToken> =
            authWebservice.getAuthToken(usernameOrEmail, password).toDataUpdate()

        // Process the getAuthToken endpoint result
        when (authTokenUpdate) {
            is SuccessUpdate -> {
                // If we got a successful AccessToken, add (or update) the
                // account in the AccountManager and pass a bundle back with
                // relevant account information
                authTokenUpdate.result?.let {
                    // Set the auth token in authorizationInterceptor
                    authorizationInterceptor.accessToken = it.accessToken

                    val isEmail = Patterns.EMAIL_ADDRESS.matcher(usernameOrEmail).matches()
                    val username = when (isEmail) {
                        true -> {
                            val userUpdate: DataUpdate<Void, User> =
                                userWebservice.getUser().toDataUpdate()
                            // TODO TEMP -- we need to fetch username here
                            when (userUpdate) {
                                is SuccessUpdate -> userUpdate.result?.username
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
                        putString(KEY_AUTHTOKEN, it.accessToken)
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
        val task = object : DataTask<Void, ResponseMessage, AccessToken>() {
            override fun generateUpdate(vararg params: Void?):
                    DataUpdate<ResponseMessage, AccessToken> =
                getAuthToken(usernameOrEmail, password)
        }
        accessTokenDataUpdate.addSource(task.fetchOnExecutor()) {
            accessTokenDataUpdate.value = it
        }
    }

    /**
     * Registers a new user. Note that this user will not be activated as the user will
     * still need to respond to the activation email.
     */
    @SuppressLint("StaticFieldLeak")
    fun register(username: String, email: String, password: String) {
        val task = object : DataTask<Void, ResponseMessage, AccessToken>() {
            override fun generateUpdate(vararg params: Void?):
                    DataUpdate<ResponseMessage, AccessToken> {
                // First, call the register endpoint
                val update: DataUpdate<Void, ResponseMessage> =
                    authWebservice.register(username, email, password, password).toDataUpdate()

                // Process the register endpoint result
                return when (update) {
                    is SuccessUpdate -> {
                        publishProgress(update.result)
                        getAuthToken(username, password)
                    }
                    is FailureUpdate -> FailureUpdate(e = update.e)
                    else -> FailureUpdate()
                }
            }
        }
        accessTokenDataUpdate.addSource(task.fetchOnExecutor()) {
            accessTokenDataUpdate.value = it
        }
    }

    // endregion methods

    // region Companion object

    companion object {

        // region Methods

        private fun toMessage(
            response: Response<ResponseMessage>,
            retrofit: Retrofit
        ): ResponseMessage? {
            return when {
                response.isSuccessful -> response.body()
                else -> response.errorBody()?.run {
                    retrofit.responseBodyConverter<ResponseMessage>(
                        ResponseMessage::class.java,
                        arrayOf()
                    ).convert(this)
                }
            }
        }

        // endregion Methods

    }

    // endregion Companion object

}
