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

import android.os.Bundle
import androidx.lifecycle.LiveData
import com.codepunk.core.BuildConfig
import com.codepunk.core.data.mapper.toDomainOrNull
import com.codepunk.core.data.remote.entity.RemoteAuthorization
import com.codepunk.core.data.remote.entity.RemoteNetworkResponse
import com.codepunk.core.data.remote.webservice.AuthWebservice
import com.codepunk.core.domain.contract.AuthRepository
import com.codepunk.core.domain.model.Authorization
import com.codepunk.core.domain.model.NetworkResponse
import com.codepunk.core.lib.getResultUpdate
import com.codepunk.core.lib.toRemoteNetworkResponse
import com.codepunk.core.util.NetworkTranslator
import com.codepunk.doofenschmirtz.util.taskinator.*
import retrofit2.Response
import retrofit2.Retrofit

/**
 * Implementation of [AuthRepository] that authenticates a user and performs other authorization-
 * related functions.
 */
class AuthRepositoryImpl(

    /**
     * An instance of [AuthWebservice] for making auth-related API calls.
     */
    private val authWebservice: AuthWebservice,

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

    private var registerTask: RegisterTask? = null

    // endregion Properties

    // region Implemented methods

    override fun authenticate(
        username: String,
        password: String
    ): LiveData<DataUpdate<NetworkResponse, Authorization>> {
        authenticateTask?.cancel(true)
        return AuthenticateTask(
            authWebservice,
            retrofit,
            networkTranslator,
            username,
            password
        ).apply {
            authenticateTask = this
        }.executeOnExecutorAsLiveData()
    }

    override fun register(
        username: String,
        email: String,
        password: String,
        passwordConfirmation: String
    ): LiveData<DataUpdate<Void, NetworkResponse>> {
        registerTask?.cancel(true)
        return RegisterTask(
            authWebservice,
            retrofit,
            networkTranslator,
            username,
            email,
            password,
            passwordConfirmation
        ).apply {
            registerTask = this
        }.executeOnExecutorAsLiveData()
    }

    // endregion Implemented methods

    // region Nested/inner classes

    private class AuthenticateTask(

        private val authWebservice: AuthWebservice,

        private val retrofit: Retrofit,

        private val networkTranslator: NetworkTranslator,

        private val username: String,

        private val password: String

    ) : DataTaskinator<Void, NetworkResponse, Authorization>() {

        // region Inherited methods

        override fun doInBackground(vararg params: Void?):
                ResultUpdate<NetworkResponse, Authorization> {
            val update: ResultUpdate<Void, Response<RemoteAuthorization>> =
                authWebservice.authorize(username, password).getResultUpdate()

            return when (update) {
                is FailureUpdate -> {
                    val errorBody = update.result?.errorBody()
                    val networkResponse = errorBody
                        .toRemoteNetworkResponse(retrofit)
                        .toDomainOrNull(networkTranslator)
                    val data = Bundle().apply {
                        putParcelable(BuildConfig.KEY_NETWORK_RESPONSE, networkResponse)
                    }

                    FailureUpdate(null, update.e, data) // TODO !!!
                }
                else -> {
                    val networkResponse =
                        update.result?.body().toDomainOrNull()
                    SuccessUpdate(networkResponse)
                }
            }
        }

        // endregion Inherited methods

    }

    private class RegisterTask(

        private val authWebservice: AuthWebservice,

        private val retrofit: Retrofit,

        private val networkTranslator: NetworkTranslator,

        private val username: String,

        private val email: String,

        private val password: String,

        private val passwordConfirmation: String

    ) : DataTaskinator<Void, Void, NetworkResponse>() {

        // region Inherited methods

        override fun doInBackground(vararg params: Void?): ResultUpdate<Void, NetworkResponse> {

            val update: ResultUpdate<Void, Response<RemoteNetworkResponse>> =
                authWebservice.register(
                    username,
                    email,
                    password,
                    passwordConfirmation
                ).getResultUpdate()

            return when (update) {
                is FailureUpdate -> {
                    val remoteNetworkResponse =
                        update.result.toRemoteNetworkResponse(retrofit)
                    val networkResponse =
                        remoteNetworkResponse.toDomainOrNull(networkTranslator)
                    FailureUpdate(networkResponse, update.e)
                }
                else -> {
                    val networkResponse =
                        update.result?.body().toDomainOrNull(networkTranslator)
                    SuccessUpdate(networkResponse)
                }
            }
        }

        // endregion Inherited methods

    }

    // endregion Nested/inner classes

}
