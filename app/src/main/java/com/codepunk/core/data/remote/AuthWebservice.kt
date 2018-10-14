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

package com.codepunk.core.data.remote

import com.codepunk.core.data.model.AuthToken
import com.codepunk.core.data.model.AuthToken.GrantType
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Webservice that defines authorization-related calls.
 */
interface AuthWebservice {

    // region Methods

    /**
     * Gets an authorization token.
     */
    @POST("oauth/token")
    @FormUrlEncoded
    @Headers(HEADER_ACCEPT_APPLICATION_JSON)
    fun getAuthToken(
        @Field("grant_type")
        grantType: GrantType,

        @Field("client_id")
        clientId: Int,

        @Field("client_secret")
        clientSecret: String,

        @Field("username")
        username: String,

        @Field("password")
        password: String,

        @Field("scope")
        scope: String
    ): Call<AuthToken>

    @POST("oauth/token")
    @FormUrlEncoded
    @Headers(HEADER_ACCEPT_APPLICATION_JSON)
    fun refreshToken(
        @Field("grant_type")
        grantType: GrantType,

        @Field("client_id")
        clientId: Int,

        @Field("client_secret")
        clientSecret: String,

        @Field("refresh_token")
        refreshToken: String
    ): Call<AuthToken>

    // endregion Methods

}
