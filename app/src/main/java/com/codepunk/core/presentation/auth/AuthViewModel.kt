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

import androidx.lifecycle.*
import com.codepunk.core.domain.contract.AuthRepository
import com.codepunk.core.domain.model.Authentication
import com.codepunk.core.domain.model.Message
import com.codepunk.doofenschmirtz.util.resourceinator.Resource
import javax.inject.Inject

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
     * A [LiveData] holding the [Authentication] relating to the current authentication attempt.
     */
    val authLiveResource = MediatorLiveData<Resource<Void, Authentication>>()

    /**
     * A [LiveData] holding the result of attempting to register (i.e. create a new account).
     */
    val registerLiveResource = MediatorLiveData<Resource<Void, Message>>()

    /**
     * A [LiveData] holding the [Message] result of an activation request.
     */
    val sendActivationLiveResource = MediatorLiveData<Resource<Void, Message>>()

    /**
     * A [LiveData] holding the [Message] result of a password reset request.
     */
    val sendPasswordResetLiveResource = MediatorLiveData<Resource<Void, Message>>()

    /**
     * A private [LiveData] holding the current source supplying the value to [authLiveResource].
     */
    private var authSource: LiveData<Resource<Void, Authentication>>? = null
        private set(value) {
            if (field != value) {
                field?.also { source -> authLiveResource.removeSource(source) }
                field = value?.apply {
                    authLiveResource.addSource(this) { value ->
                        authLiveResource.value = value
                    }
                }
            }
        }

    /**
     * A private [LiveData] holding the current source supplying the value to
     * [registerLiveResource].
     */
    private var registerSource: LiveData<Resource<Void, Message>>? = null
        private set(value) {
            if (field != value) {
                field?.also { source -> registerLiveResource.removeSource(source) }
                field = value?.apply {
                    registerLiveResource.addSource(this) { value ->
                        registerLiveResource.value = value
                    }
                }
            }
        }


    /**
     * A private [LiveData] holding the current source supplying the value to
     * [sendActivationLiveResource].
     */
    private var sendActivationSource: LiveData<Resource<Void, Message>>? = null
        private set(value) {
            if (field != value) {
                field?.also { source -> sendActivationLiveResource.removeSource(source) }
                field = value?.apply {
                    sendActivationLiveResource.addSource(this) { value ->
                        sendActivationLiveResource.value = value
                    }
                }
            }
        }

    /**
     * A private [LiveData] holding the current source supplying the value to
     * [sendPasswordResetLiveResource].
     */
    private var sendPasswordResetSource: LiveData<Resource<Void, Message>>? = null
        private set(value) {
            if (field != value) {
                field?.also { source -> sendPasswordResetLiveResource.removeSource(source) }
                field = value?.apply {
                    sendPasswordResetLiveResource.addSource(this) { value ->
                        sendPasswordResetLiveResource.value = value
                    }
                }
            }
        }

    // endregion Properties

    // region Methods

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

    /**
     * Requests an activation code to be sent to the supplied [email].
     */
    fun sendActivationCode(email: String) {
        sendActivationSource = authRepository.sendActivationCode(email)
    }

    /**
     * Requests a password reset link to be sent to the supplied [email].
     */
    fun sendPasswordResetLink(email: String) {
        sendPasswordResetSource = authRepository.sendPasswordResetLink(email)
    }

    // endregion methods

}
