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
import com.codepunk.core.data.remote.entity.RemoteAuthorization
import com.codepunk.core.data.remote.entity.RemoteNetworkResponse
import com.codepunk.core.data.remote.entity.RemoteUser
import com.codepunk.core.data.remote.webservice.AuthWebservice
import com.codepunk.core.data.remote.webservice.UserWebservice
import com.codepunk.core.domain.contract.AuthRepository
import com.codepunk.core.domain.model.NetworkResponse
import com.codepunk.core.lib.getResultUpdate
import com.codepunk.doofenschmirtz.util.taskinator.*
import retrofit2.Response
import retrofit2.Retrofit
import javax.inject.Inject

// TODO Convert objects to classes to avoid static field leak warnings

/**
 * A [ViewModel] for managing account-related data.
 */
class AuthViewModel @Inject constructor(

    /**
     * The [Retrofit] instance.
     */
    private val retrofit: Retrofit,

    /**
     * The authorization repository.
     */
    private val authRepository: AuthRepository,

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
     * A [LiveData] holding the result of attempting to register (i.e. create a new account).
     */
    val registerDataUpdate =
        MediatorLiveData<DataUpdate<Void, NetworkResponse>>()

    /**
     * A [LiveData] holding the [RemoteAuthorization] relating to the current authorization attempt.
     */
    val authorizationDataUpdate =
        MediatorLiveData<DataUpdate<RemoteNetworkResponse, Response<RemoteAuthorization>>>()

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
    ): ResultUpdate<RemoteNetworkResponse, Response<RemoteAuthorization>> {
        // TODO go through AccountAuthenticator somehow? Probably not because I need to create
        // an account

        // Call the authorize endpoint
        val authTokenUpdate: ResultUpdate<RemoteNetworkResponse, Response<RemoteAuthorization>> =
            authWebservice.authorize(usernameOrEmail, password).getResultUpdate()

        // Process the authorize endpoint result
        when (authTokenUpdate) {
            is SuccessUpdate -> {
                authTokenUpdate.result?.body()?.let {
                    val isEmail = Patterns.EMAIL_ADDRESS.matcher(usernameOrEmail).matches()
                    val username = when (isEmail) {
                        true -> {
                            val userUpdate: ResultUpdate<Void, Response<RemoteUser>> =
                                userWebservice.getUser(it.authToken).getResultUpdate()
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
            object : DataTaskinator<Void, RemoteNetworkResponse, Response<RemoteAuthorization>>() {
                override fun doInBackground(vararg params: Void?):
                        ResultUpdate<RemoteNetworkResponse, Response<RemoteAuthorization>> =
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
        password: String,
        passwordConfirmation: String
    ) {

        // TODO NEXT NEXT NEXT
        // I need to scrap "source" below because I'm calling authRepository here.
        // HOWEVER - authRepository.register is creating a LiveData<DataUpdate<Void, NetworkResponse>>,
        // while "task".executeOnExecutorAsLiveData is producing a LiveData<DataUpdate<RemoteNetworkResponse, Response<RemoteAuthorization>>>.
        // Why??

        // Also TODO: delete the "old" source first?

        // Thoughts:
        // AuthenticateActivity is doing a ton of work decoding the Response<RemoteAuthorization> etc.
        // That should probably happen here and return an appropriate DataUpdate with possible failures etc.

        val source: LiveData<DataUpdate<Void, NetworkResponse>> =
            authRepository.register(
                username,
                email,
                password,
                passwordConfirmation
            )
        registerDataUpdate.addSource(source) {
            registerDataUpdate.value = it
        }

        /*
        val source =
            object : DataTaskinator<Void, RemoteNetworkResponse, Response<RemoteAuthorization>>() {
                override fun doInBackground(vararg params: Void?):
                        ResultUpdate<RemoteNetworkResponse, Response<RemoteAuthorization>> {
                    // Call the register endpoint
                    val update: ResultUpdate<Void, Response<RemoteNetworkResponse>> =
                        authWebservice.register(
                            username,
                            email,
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
                            val text = update.result.toRemoteNetworkResponse(retrofit)
                            val remoteMessage =
                                RemoteNetworkResponse("BAD!") // TODO TEMP update.response.toRemoteNetworkResponse(retrofit)
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
            }.executeOnExecutorAsLiveData()

        authorizationDataUpdate.addSource(source) {
            authorizationDataUpdate.value = it
        }
        */
    }

    // endregion methods

    /*
    // region Companion object

    companion object {

        // region Methods

        /**
         * Extracts a [RemoteNetworkResponse] from a [Response], or converts the error body message
         * to a [RemoteNetworkResponse].
         */
        @Suppress("UNUSED")
        private fun toMessage(
            response: Response<RemoteNetworkResponse>,
            retrofit: Retrofit
        ): RemoteNetworkResponse? {
            return when {
                response.isSuccessful -> response.body()
                else -> response.errorBody()?.run {
                    retrofit.responseBodyConverter<RemoteNetworkResponse>(
                        RemoteNetworkResponse::class.java,
                        arrayOf()
                    ).convert(this)
                }
            }
        }

        // endregion Methods

    }

    // endregion Companion object
    */

}
