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

import com.codepunk.core.data.remote.HEADER_ACCEPT_APPLICATION_JSON
import com.codepunk.core.data.remote.HEADER_AUTHORIZATION_BEARER
import com.codepunk.core.data.remote.HEADER_NAME_TEMP_AUTH_TOKEN
import com.codepunk.core.data.remote.entity.RemoteUser
import com.codepunk.core.domain.model.User
import com.codepunk.core.domain.session.Session
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers

/**
 * Webservice that defines user-related calls.
 */
interface UserWebservice {

    // region Methods

    /**
     * Gets the authenticated user using the supplied [authToken]. Most endpoints that require
     * authentication will pull the authToken value from the current [Session], but a session
     * needs a [User] in order to be created.
     */
    @GET("api/user")
    @Headers(
        HEADER_ACCEPT_APPLICATION_JSON
    )
    fun getUser(@Header(HEADER_NAME_TEMP_AUTH_TOKEN) authToken: String): Call<RemoteUser>

    /**
     * Gets the current (authenticated) user.
     */
    @Suppress("UNUSED")
    @GET("api/user")
    @Headers(
        HEADER_ACCEPT_APPLICATION_JSON,
        HEADER_AUTHORIZATION_BEARER
    )
    fun getUser(): Call<RemoteUser>

    // endregion Methods

}
