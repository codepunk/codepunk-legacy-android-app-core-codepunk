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

package com.codepunk.core.data.model

import com.squareup.moshi.Json

data class AuthToken(

    @field:Json(name = "token_type")
    val tokenType: TokenType, /* TODO Make this an enum but don't confuse with session.AuthTokenType */

    @field:Json(name = "expires_in")
    val expiresIn: Long,

    @field:Json(name = "access_token")
    val accessToken: String,

    @field:Json(name = "refresh_token")
    val refreshToken: String

) {

    enum class GrantType(

        val value: String

    ) {

        /**
         * The authorization code grant type is used to obtain both access tokens and refresh
         * tokens and is optimized for confidential clients.
         */
        @field:Json(name = "authorization_code")
        AUTH_CODE("authorization_code"),

        /**
         * The implicit grant type is used to obtain access tokens (it does not support the
         * issuance of refresh tokens) and is optimized for public clients known to operate a
         * particular redirection URI.  These clients are typically implemented in a browser
         * using a scripting language such as JavaScript.
         */
        @field:Json(name = "implicit")
        IMPLICIT("implicit"),

        /**
         * The resource owner password credentials grant type is suitable in cases where the
         * resource owner has a trust relationship with the client, such as the device operating
         * system or a highly privileged application.  The authorization server should take
         * special care when enabling this grant type and only allow it when other flows are not
         * viable.
         *
         * This grant type is suitable for clients capable of obtaining the resource owner's
         * credentials (username and password, typically using an interactive form).  It is also
         * used to migrate existing clients using direct authentication schemes such as HTTP Basic
         * or Digest authentication to OAuth by converting the stored credentials to an access
         * token.
         */
        @field:Json(name = "user_credentials")
        USER_CREDENTIALS("password"),

        /**
         * The client can request an access token using only its client credentials (or other
         * supported means of authentication) when the client is requesting access to the protected
         * resources under its control, or those of another resource owner that have been
         * previously arranged with the authorization server.
         */
        @field:Json(name = "client_credentials")
        CLIENT_CREDENTIALS("client_credentials"),

        /**
         * If the authorization server issued a refresh token to the client, the client can make a
         * refresh request to the token endpoint.
         */
        @field:Json(name = "refresh_token")
        REFRESH_TOKEN("refresh_token"),

        /**
         * The client uses an extension grant type by specifying the grant type using an absolute
         * URI (defined by the authorization server) as the value of the "grant_type" parameter of
         * the token endpoint, and by adding any additional parameters necessary.
         */
        @field:Json(name = "extensions")
        EXTENSIONS("extensions");

    }

    enum class TokenType {

        @field:Json(name = "bearer")
        BEARER,

        @field:Json(name = "mac")
        MAC

    }

}
