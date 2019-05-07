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

package com.codepunk.core.exception

import android.annotation.TargetApi
import android.os.Build
import com.codepunk.core.data.remote.entity.RemoteOAuthServerExceptionResponse
import java.lang.RuntimeException

open class OAuthServerException : RuntimeException {

    // region Properties

    val errorDescription: String?

    val hint: String?

    // endregion Properties

    // region Constructors

    constructor(errorDescription: String? = null, hint: String? = null) : super() {
        this.errorDescription = errorDescription
        this.hint = hint
    }

    constructor(
        message: String?,
        errorDescription: String? = null,
        hint: String? = null) : super(message) {
        this.errorDescription = errorDescription
        this.hint = hint
    }

    constructor(
        message: String?,
        cause: Throwable?,
        errorDescription: String? = null, hint: String? = null
    ) : super(message, cause) {
        this.errorDescription = errorDescription
        this.hint = hint
    }

    constructor(
        cause: Throwable?,
        errorDescription: String? = null,
        hint: String? = null
    ) : super(cause) {
        this.errorDescription = errorDescription
        this.hint = hint
    }

    @TargetApi(Build.VERSION_CODES.N)
    constructor(
        message: String?,
        cause: Throwable?,
        enableSuppression: Boolean,
        writableStackTrace: Boolean,
        errorDescription: String? = null,
        hint: String? = null
    ) : super(message, cause, enableSuppression, writableStackTrace) {
        this.errorDescription = errorDescription
        this.hint = hint
    }

    constructor(response: RemoteOAuthServerExceptionResponse) : this(
        response.message,
        response.errorDescription,
        response.hint
    )

    // endregion Constructors

    // region Companion object

    companion object {

        /**
         * A constant that corresponds to an attempt to log in to (or get an auth token for) a
         * user account that has never been activated.
         */
        const val INACTIVE_USER = "codepunk::activatinator.inactive"

        /**
         * A constant that corresponds to a failed log in attempt due to invalid credentials
         * (i.e. username/password).
         */
        const val INVALID_CREDENTIALS = "invalid_credentials"

        /**
         * A constant that corresponds to a failed log in attempt due to an invalid request
         * (i.e. field not supplied, etc.).
         */
        const val INVALID_REQUEST = "invalid_request"

        /**
         * Creates a new OAuthServerException class based on the supplied [response].
         */
        fun from(response: RemoteOAuthServerExceptionResponse?): OAuthServerException =
            when (response?.error) {
                INACTIVE_USER -> InactiveUserServerException(response)
                INVALID_CREDENTIALS -> InvalidCredentialsServerException(
                    response
                )
                INVALID_REQUEST -> InvalidRequestServerException(response)
                null -> OAuthServerException()
                else -> OAuthServerException(response)
            }

    }

    // endregion Companion object

}
