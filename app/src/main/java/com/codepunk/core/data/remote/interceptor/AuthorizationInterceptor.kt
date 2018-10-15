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

package com.codepunk.core.data.remote.interceptor

import com.codepunk.core.data.remote.HEADER_NAME_AUTHORIZATION
import com.codepunk.core.data.remote.HEADER_VALUE_ACCESS_TOKEN_PLACEHOLDER
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Singleton class that intercepts Retrofit requests and looks for a header with a name
 * of [HEADER_NAME_AUTHORIZATION] ("Authorization"). If found, any instance in the value matching
 * [HEADER_VALUE_ACCESS_TOKEN_PLACEHOLDER] will be replaced with the current value of [accessToken].
 */
@Singleton
class AuthorizationInterceptor @Inject constructor() : Interceptor {

    // region Properties

    /**
     * The access token corresponding to the current session.
     * TODO Replace this with some sort of Session object (or just set the current Account)
     */
    var accessToken: String = ""

    // endregion Properties

    // region Implemented methods

    /**
     * Implementation of [Interceptor]. Looks for a header with a name of
     * [HEADER_NAME_AUTHORIZATION] ("Authorization") and replaces any instance of
     * [HEADER_VALUE_ACCESS_TOKEN_PLACEHOLDER] in the value with the value of [accessToken].
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        return chain.proceed(
            request.header(HEADER_NAME_AUTHORIZATION)?.let { value ->
                request.newBuilder()
                    .header(
                        HEADER_NAME_AUTHORIZATION,
                        value.replace(
                            HEADER_VALUE_ACCESS_TOKEN_PLACEHOLDER,
                            accessToken,
                            true
                        )
                    )
                    .build()
            } ?: request
        )
    }

    // endregion Implemented methods

}
