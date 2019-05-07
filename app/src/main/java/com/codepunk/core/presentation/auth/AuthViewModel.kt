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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.codepunk.core.domain.contract.AuthRepository
import com.codepunk.core.domain.model.Authentication
import com.codepunk.core.domain.model.Message
import com.codepunk.doofenschmirtz.util.taskinator.DataUpdate
import javax.inject.Inject

// TODO Convert objects to classes to avoid static field leak warnings

/**
 * A [ViewModel] for managing account-related data.
 */
class AuthViewModel @Inject constructor(

    /**
     * The authentication repository.
     */
    private val authRepository: AuthRepository

) : ViewModel() {

    // region Properties

    /**
     * A [LiveData] holding the result of attempting to register (i.e. create a new account).
     */
    val registerDataUpdate = MediatorLiveData<DataUpdate<Void, Message>>()

    /**
     * A private [LiveData] holding the current source supplying the value to [registerDataUpdate].
     */
    private var registerSource: LiveData<DataUpdate<Void, Message>>? = null
        private set(value) {
            if (field != value) {
                field?.also { source -> registerDataUpdate.removeSource(source) }
                field = value?.apply {
                    registerDataUpdate.addSource(this) { value ->
                        registerDataUpdate.value = value
                    }
                }
            }
        }

    /**
     * A [LiveData] holding the [Authentication] relating to the current authentication attempt.
     */
    val authDataUpdate = MediatorLiveData<DataUpdate<Void, Authentication>>()

    /**
     * A private [LiveData] holding the current source supplying the value to
     * [authDataUpdate].
     */
    private var authSource: LiveData<DataUpdate<Void, Authentication>>? = null
        private set(value) {
            if (field != value) {
                field?.also { source -> authDataUpdate.removeSource(source) }
                field = value?.apply {
                    authDataUpdate.addSource(this) { value ->
                        authDataUpdate.value = value
                    }
                }
            }
        }

    val sendActivationDataUpdate = MediatorLiveData<DataUpdate<Void, Message>>()

    private var sendActivationSource: LiveData<DataUpdate<Void, Message>>? = null
        private set(value) {
            if (field != value) {
                field?.also {source -> sendActivationDataUpdate.removeSource(source) }
                field = value?.apply {
                    sendActivationDataUpdate.addSource(this) { value ->
                        sendActivationDataUpdate.value = value
                    }
                }
            }
        }

    // endregion Properties

    // region Methods

    /*
    /**
     * Synchronously calls the authorize endpoint and adds a [Bundle] needed by the
     * [AccountManager].
     */
    @WorkerThread
    private fun getAuthToken(
        usernameOrEmail: String,
        password: String
    ): ResultUpdate<RemoteNetworkResponse, Response<RemoteAuthentication>> {
        // TODO go through AccountAuthenticator somehow? Probably not because I need to create
        // an account

        // Call the authorize endpoint
        val authTokenUpdate: ResultUpdate<RemoteNetworkResponse, Response<RemoteAuthentication>> =
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
    */

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

    fun sendActivationCode(email: String) {
        sendActivationSource = authRepository.sendActivationEmail(email)
    }

    // endregion methods

}
