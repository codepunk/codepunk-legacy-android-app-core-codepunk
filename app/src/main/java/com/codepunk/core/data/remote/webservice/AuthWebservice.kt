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

import com.codepunk.core.data.model.auth.AccessToken
import com.codepunk.core.data.model.auth.GrantType
import com.codepunk.core.data.model.http.ResponseMessage
import com.codepunk.core.data.remote.HEADER_ACCEPT_APPLICATION_JSON
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Webservice that defines authorization-related calls.
 */
@Suppress("UNUSED")
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
        clientId: String,

        @Field("client_secret")
        clientSecret: String,

        @Field("username")
        username: String,

        @Field("password")
        password: String,

        @Field("scope")
        scope: String
    ): Call<AccessToken>

    /**
     * Gets an authorization token using default values.
     */
    fun getAuthToken(username: String, password: String, scope: String): Call<AccessToken>

    /**
     * Gets an authorization token using default values.
     */
    fun getAuthToken(username: String, password: String): Call<AccessToken>

    /**
     * Gets an authorization token from an existing [refreshToken].
     */
    @POST("oauth/token")
    @FormUrlEncoded
    @Headers(HEADER_ACCEPT_APPLICATION_JSON)
    fun refreshToken(
        @Field("grant_type")
        grantType: GrantType,

        @Field("client_id")
        clientId: String,

        @Field("client_secret")
        clientSecret: String,

        @Field("refresh_token")
        refreshToken: String
    ): Call<AccessToken>

    /**
     * Gets an authorization token from an existing [refreshToken].
     */
    fun refreshToken(refreshToken: String): Call<AccessToken>

    /**
     * Registers a new account.
     */
    @POST("register")
    @FormUrlEncoded
    @Headers(HEADER_ACCEPT_APPLICATION_JSON)
    fun register(
        @Field("name")
        name: String,

        @Field("email")
        email: String,

        @Field("password")
        password: String,

        @Field("password_confirmation")
        passwordConfirmation: String
    ): Call<ResponseMessage>

    // endregion Methods

}
