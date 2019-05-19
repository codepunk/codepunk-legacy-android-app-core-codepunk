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

package com.codepunk.core.data.remote.interceptor

import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.Retrofit
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * An OkHttp3 [Interceptor] that allows us to change the base URL.
 */
@Singleton
class UrlOverrideInterceptor @Inject constructor() : Interceptor {

    @Volatile
    private var oldValue: String? = null

    @Volatile
    private var newValue: String? = null

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        oldValue?.also {
            val urlString = request.url().toString()
            val overriddenUrlString = urlString.replace(it, newValue ?: "")
            HttpUrl.parse(overriddenUrlString)?.also { url ->
                request = request.newBuilder().url(url).build()
            }
        }

        return chain.proceed(request)
    }

    fun override(oldValue: String, newValue: String) {
        this.oldValue = oldValue

        // Test and resolve the new value by building a dummy Retrofit instance and converting
        // back to a string
        this.newValue = urlTester.baseUrl(newValue).build().baseUrl().toString()
    }

    fun clear() {
        oldValue = null
        newValue = null
    }

    // region Companion object

    companion object {

        // region Properties

        @JvmStatic
        private val urlTester by lazy {
            Retrofit.Builder()
        }

        // endregion Properties

    }

    // endregion Companion object

}
