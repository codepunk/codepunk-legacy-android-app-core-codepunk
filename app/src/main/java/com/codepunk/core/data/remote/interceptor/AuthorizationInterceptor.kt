/*
 * Copyright (C) 2018 Codepunk, LLC
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

package com.codepunk.core.data.remote.interceptor

import com.codepunk.core.data.remote.HEADER_NAME_AUTHORIZATION
import com.codepunk.core.data.remote.HEADER_NAME_TEMP_AUTH_TOKEN
import com.codepunk.core.data.remote.HEADER_VALUE_AUTHORIZATION
import com.codepunk.core.data.remote.HEADER_VALUE_AUTH_TOKEN_PLACEHOLDER
import com.codepunk.core.domain.session.SessionManager
import dagger.Lazy
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Singleton class that intercepts Retrofit requests and looks for a header with a name
 * of [HEADER_NAME_AUTHORIZATION] ("RemoteAuthorization"). If found, any instance in the value matching
 * [HEADER_VALUE_AUTH_TOKEN_PLACEHOLDER] will be replaced with the authToken (if any) currently
 * stored in [SessionManager].
 */
@Singleton
class AuthorizationInterceptor @Inject constructor(

    /**
     * The session manager used for managing a user session. Lazy to avoid circular reference in
     * Dagger.
     */
    private val lazySessionManager: Lazy<SessionManager>

) : Interceptor {

    // region Implemented methods

    /**
     * Implementation of [Interceptor]. Looks for a header with a name of
     * [HEADER_NAME_AUTHORIZATION] ("RemoteAuthorization") and replaces any instance of
     * [HEADER_VALUE_AUTH_TOKEN_PLACEHOLDER] in the value with the authToken (if any) currently
     * stored in [SessionManager]. Also looks for a header with a name of
     * [HEADER_NAME_TEMP_AUTH_TOKEN] and converts it into a [HEADER_NAME_AUTHORIZATION] header.
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        var request: Request = chain.request()

        // Replace authToken in headers matching HEADER_NAME_AUTHORIZATION
        request.header(HEADER_NAME_AUTHORIZATION)?.run {
            val authToken = lazySessionManager.get().session?.authToken ?: ""
            request = request.newBuilder()
                .header(
                    HEADER_NAME_AUTHORIZATION,
                    replace(
                        HEADER_VALUE_AUTH_TOKEN_PLACEHOLDER,
                        authToken,
                        true
                    )
                )
                .build()
        }

        // Convert HEADER_NAME_TEMP_AUTH_TOKEN header to HEADER_NAME_AUTHORIZATION
        request.header(HEADER_NAME_TEMP_AUTH_TOKEN)?.run {
            request = request.newBuilder()
                .header(
                    HEADER_NAME_AUTHORIZATION,
                    HEADER_VALUE_AUTHORIZATION.replace(
                        HEADER_VALUE_AUTH_TOKEN_PLACEHOLDER,
                        this,
                        true
                    )
                )
                .removeHeader(HEADER_NAME_TEMP_AUTH_TOKEN)
                .build()
        }

        return chain.proceed(request)
    }

    // endregion Implemented methods

}
