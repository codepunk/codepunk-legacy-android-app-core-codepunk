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

package com.codepunk.core.lib

import android.annotation.TargetApi
import android.os.Build
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import java.lang.RuntimeException

/**
 * A [RuntimeException] that holds a [Response].
 */
@Suppress("UNUSED")
class ResponseException : RuntimeException {

    // region Properties

    /**
     * The [Response] returned from executing a [Retrofit] [Call].
     */
    val response: Response<*>

    // endregion Properties

    // region Constructors

    /**
     * Constructs a new runtime exception with the specified [response] and with null as its detail
     * message.
     */
    constructor(response: Response<*>) : super() {
        this.response = response
    }

    /**
     * Constructs a new runtime exception with the specified [response] and detail [message].
     */
    constructor(response: Response<*>, message: String?) : super(message) {
        this.response = response
    }

    /**
     * Constructs a new runtime exception with the specified [response], detail [message] and
     * [cause].
     */
    constructor(response: Response<*>, message: String?, cause: Throwable?) : super(
        message,
        cause
    ) {
        this.response = response
    }

    /**
     * Constructs a new runtime exception with the specified [response], [cause] and a detail
     * message of (cause==null ? null : cause.toString()) (which typically contains the class and detail message of cause).
     */
    constructor(response: Response<*>, cause: Throwable?) : super(cause) {
        this.response = response
    }

    /**
     * Constructs a new runtime exception with the specified [response], detail [message], [cause],
     * suppression enabled or disabled, and writable stack trace enabled or disabled.
     */
    @TargetApi(Build.VERSION_CODES.N)
    constructor(
        response: Response<*>,
        message: String?,
        cause: Throwable?,
        enableSuppression: Boolean,
        writableStackTrace: Boolean
    ) : super(message, cause, enableSuppression, writableStackTrace) {
        this.response = response
    }

    override fun toString(): String {
        return "${javaClass.simpleName}(response=$response)"
    }

    // endregion Constructors

    // region Inherited methods



    // endregion Inherited methods

}
