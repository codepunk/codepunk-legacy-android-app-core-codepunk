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

import android.os.AsyncTask.THREAD_POOL_EXECUTOR
import android.util.Log
import androidx.lifecycle.LiveData
import com.codepunk.core.data.mapper.toDomainOrNull
import com.codepunk.core.data.remote.entity.RemoteNetworkResponse
import com.codepunk.core.data.remote.webservice.AuthWebservice
import com.codepunk.core.domain.contract.AuthRepository
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

    private var registerTask: RegisterTask? = null

    // endregion Properties

    // region Implemented methods

    override fun register(
        name: String,
        email: String,
        password: String,
        passwordConfirmation: String
    ): LiveData<DataUpdate<Void, NetworkResponse>> {
        registerTask?.cancel(true)
        return RegisterTask(
            authWebservice,
            retrofit,
            networkTranslator
        ).apply {
            registerTask = this
        }.executeOnExecutorAsLiveData(
            THREAD_POOL_EXECUTOR,
            name,
            email,
            password,
            passwordConfirmation
        )
    }

    // endregion Implemented methods

    // region Nested/inner classes

    private class RegisterTask(

        private val authWebservice: AuthWebservice,

        private val retrofit: Retrofit,

        private val networkTranslator: NetworkTranslator

    ) : DataTaskinator<String, Void, NetworkResponse>() {

        // region Inherited methods

        override fun doInBackground(vararg params: String?): ResultUpdate<Void, NetworkResponse> {

            // Extract arguments from params
            val username = params.getOrNull(0) ?: ""
            val email = params.getOrNull(1) ?: ""
            val password = params.getOrNull(2) ?: ""
            val confirmPassword = params.getOrNull(3) ?: ""

            val update: ResultUpdate<Void, Response<RemoteNetworkResponse>> =
                authWebservice.register(
                    username,
                    email,
                    password,
                    confirmPassword
                ).getResultUpdate()
            when (update) {
                is SuccessUpdate -> {
                    Log.d("AuthRepositoryImpl", "doInBackground: update=$update")
                }
                is FailureUpdate -> {
                    val remoteNetworkResponse = update.result.toRemoteNetworkResponse(retrofit)
                    val networkResponse = remoteNetworkResponse.toDomainOrNull(networkTranslator)
                    return FailureUpdate(networkResponse)
                }
            }

            TODO("not implemented")
        }

        // endregion Inherited methods

    }

    // endregion Nested/inner classes

}
