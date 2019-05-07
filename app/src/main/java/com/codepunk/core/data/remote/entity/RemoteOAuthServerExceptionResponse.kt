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

import com.squareup.moshi.Json

/**
 * A response message from the server indicating a (Laravel) OAuthServerException occurred. This
 * response contains a [message] string and optional [errors] detailing any errors encountered
 * during the request.
 */
open class RemoteOAuthServerExceptionResponse(

    /**
     * A string message.
     */
    val message: String?,

    /**
     * A string describing the type of the exception.
     */
    val error: String?,

    /**
     * An optional description of the error.
     */
    @field:Json(name = "error_description")
    val errorDescription: String?,

    /**
     * An optional hint that describes what may have triggered the exception.
     */
    val hint: String?

) {

    // region Inherited methods

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RemoteOAuthServerExceptionResponse) return false

        if (message != other.message) return false
        if (error != other.error) return false
        if (errorDescription != other.errorDescription) return false
        if (hint != other.hint) return false

        return true
    }

    override fun hashCode(): Int {
        var result = message?.hashCode() ?: 0
        result = 31 * result + (error?.hashCode() ?: 0)
        result = 31 * result + (errorDescription?.hashCode() ?: 0)
        result = 31 * result + (hint?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String = RemoteOAuthServerExceptionResponse::class.java.simpleName +
        "(message=$message, error=$error, errorDescription=$errorDescription, hint=$hint)"

// endregion Inherited methods

}
