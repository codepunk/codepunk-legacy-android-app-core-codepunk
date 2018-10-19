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

package com.codepunk.core.data.remote.webservice

import com.codepunk.core.BuildConfig
import com.codepunk.core.data.model.auth.AccessToken
import com.codepunk.core.data.model.auth.GrantType
import com.codepunk.core.data.model.http.ResponseMessage
import okhttp3.ResponseBody
import retrofit2.Call

/**
 * Implementation of [AuthWebservice] that allows for default arguments by wrapping another
 * instance ([base]) and passing default arguments to its methods where appropriate.
 */
class AuthWebserviceWrapper(private val base: AuthWebservice) : AuthWebservice {

    override fun getAuthToken(
        grantType: GrantType,
        clientId: Int,
        clientSecret: String,
        username: String,
        password: String,
        scope: String
    ): Call<AccessToken> = base.getAuthToken(
        grantType,
        clientId,
        clientSecret,
        username,
        password,
        scope
    )

    override fun refreshToken(
        grantType: GrantType,
        clientId: String,
        clientSecret: String,
        refreshToken: String
    ): Call<AccessToken> = base.refreshToken(grantType, clientId, clientSecret, refreshToken)

    /**
     * Gets an authorization token from an existing [refreshToken] by passing default arguments to
     * the base implementation.
     */
    override fun refreshToken(refreshToken: String): Call<AccessToken> = base.refreshToken(
        GrantType.REFRESH_TOKEN,
        BuildConfig.CODEPUNK_LOCAL_CLIENT_ID,
        BuildConfig.CODEPUNK_LOCAL_CLIENT_SECRET,
        refreshToken
    )

    override fun register(
        name: String,
        email: String,
        password: String,
        passwordConfirmation: String
    ): Call<ResponseMessage> = base.register(name, email, password, passwordConfirmation)
}
