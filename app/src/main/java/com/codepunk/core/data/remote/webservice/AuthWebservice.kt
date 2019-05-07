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

import com.codepunk.core.data.remote.entity.RemoteAuthentication
import com.codepunk.core.domain.model.GrantType
import com.codepunk.core.data.remote.HEADER_ACCEPT_APPLICATION_JSON
import com.codepunk.core.data.remote.entity.RemoteMessage
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Webservice that defines authentication-related calls.
 */
@Suppress("UNUSED")
interface AuthWebservice {

    // region Methods

    /**
     * Gets an authentication token.
     */
    @POST("oauth/token")
    @FormUrlEncoded
    @Headers(HEADER_ACCEPT_APPLICATION_JSON)
    fun authorize(
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
    ): Call<RemoteAuthentication>

    /**
     * Gets an authentication token using default values.
     */
    fun authorize(username: String, password: String, scope: String): Call<RemoteAuthentication>

    /**
     * Gets an authentication token using default values.
     */
    fun authorize(username: String, password: String): Call<RemoteAuthentication>

    /**
     * Gets an authentication token from an existing [refreshToken].
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
    ): Call<RemoteAuthentication>

    /**
     * Gets an authentication token from an existing [refreshToken].
     */
    fun refreshToken(refreshToken: String): Call<RemoteAuthentication>

    /**
     * Registers a new account. // TODO Move this to an "account" webservice? Hmm. It's not
     * really user, and not really auth.
     */
    @POST("register")
    @FormUrlEncoded
    @Headers(HEADER_ACCEPT_APPLICATION_JSON)
    fun register(
        @Field("username")
        username: String,

        @Field("email")
        email: String,

        @Field("password")
        password: String,

        @Field("password_confirmation")
        passwordConfirmation: String
    ): Call<RemoteMessage>

    /**
     * Sends an activation code to the supplied [email].
     */
    @POST("activate/send")
    @FormUrlEncoded
    @Headers(HEADER_ACCEPT_APPLICATION_JSON)
    fun sendActivationCode(
        @Field("email")
        email: String
    ): Call<RemoteMessage>

    // endregion Methods

}
