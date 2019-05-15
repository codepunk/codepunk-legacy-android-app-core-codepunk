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

package com.codepunk.core.data.remote.entity

import com.codepunk.core.domain.model.TokenType
import com.codepunk.core.domain.model.UNKNOWN
import com.codepunk.doofenschmirtz.util.ellipsize
import com.squareup.moshi.Json

/**
 * A data class representing an OAuth2 authentication.
 */
data class RemoteAuthentication(

    /**
     * The type of auth token, which provides the client with the information required to
     * successfully utilize the auth token to make a protected resource request (along with
     * type-specific attributes).
     */
    @field:Json(name = "token_type")
    val tokenType: TokenType,

    /**
     * A long string of characters that serves as a credential used to access protected resources.
     */
    @field:Json(name = "access_token")
    val authToken: String,

    /**
     * A token which allows the app to fetch a new auth token when the old one expires.
     */
    @field:Json(name = "refresh_token")
    val refreshToken: String,

    /**
     * The number of seconds until the auth token expires.
     */
    @field:Json(name = "expires_in")
    val expiresIn: Long = UNKNOWN

) {

    // region Inherited methods

    /**
     * A version of [toString] that ellipsizes [authToken] and [refreshToken].
     */
    override fun toString(): String =
        "RemoteAuthentication(" +
            "tokenType=$tokenType, " +
            "authToken='${authToken.ellipsize()}', " +
            "refreshToken='${refreshToken.ellipsize()}', " +
            "expiresIn=$expiresIn)"

    // endregion Inherited methods

}
