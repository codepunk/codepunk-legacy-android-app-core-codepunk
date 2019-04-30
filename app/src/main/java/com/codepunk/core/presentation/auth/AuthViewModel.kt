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
import com.codepunk.core.domain.model.Authorization
import com.codepunk.core.domain.model.NetworkResponse
import com.codepunk.core.lib.getResultUpdate
import com.codepunk.doofenschmirtz.util.taskinator.DataUpdate
import com.codepunk.doofenschmirtz.util.taskinator.FailureUpdate
import com.codepunk.doofenschmirtz.util.taskinator.ResultUpdate
import com.codepunk.doofenschmirtz.util.taskinator.SuccessUpdate
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
    val registerDataUpdate = MediatorLiveData<DataUpdate<Void, NetworkResponse>>()

    /**
     * A private [LiveData] holding the current source supplying the value to [registerDataUpdate].
     */
    private var registerSource: LiveData<DataUpdate<Void, NetworkResponse>>? = null
        private set(value) {
            field?.also { source -> registerDataUpdate.removeSource(source) }
            field = value?.apply {
                registerDataUpdate.addSource(this) { value ->
                    registerDataUpdate.value = value
                }
            }
        }

    /**
     * A [LiveData] holding the [Authorization] relating to the current authorization attempt.
     */
    val authorizationDataUpdate = MediatorLiveData<DataUpdate<NetworkResponse, Authorization>>()

    /**
     * A private [LiveData] holding the current source supplying the value to
     * [authorizationDataUpdate].
     */
    private var authSource: LiveData<DataUpdate<NetworkResponse, Authorization>>? = null
        private set(value) {
            field?.also { source -> authorizationDataUpdate.removeSource(source) }
            field = value?.apply {
                authorizationDataUpdate.addSource(this) { value ->
                    authorizationDataUpdate.value = value
                }
            }
        }

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
    fun authenticate(usernameOrEmail: String, password: String) {
        authSource = authRepository.authenticate(usernameOrEmail, password)
    }

    /**
     * Registers a new user. Note that this user will not be activated as the user will
     * still need to respond to the activation email.
     */
    fun register(
        username: String,
        email: String,
        password: String,
        passwordConfirmation: String
    ) {
        registerSource = authRepository.register(username, email, password, passwordConfirmation)
    }

    // endregion methods

}
