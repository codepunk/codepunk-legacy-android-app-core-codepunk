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

class RemoteInactiveUserExceptionResponse(

    /**
     * A string message.
     */
    message: String?,

    /**
     * A string describing the type of the exception.
     */
    error: String?,

    /**
     * An optional description of the error.
     */
    errorDescription: String?,

    /**
     * An optional hint that describes what may have triggered the exception.
     */
    hint: String?,

    /**
     * An optional email of the user related to the exception.
     */
    val email: String?

) : RemoteOAuthServerExceptionResponse(message, error, errorDescription, hint) {

    // region Inherited methods
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RemoteInactiveUserExceptionResponse) return false
        if (!super.equals(other)) return false

        if (email != other.email) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (email?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String = RemoteInactiveUserExceptionResponse::class.java.simpleName +
        "(message=$message, error=$error, errorDescription=$errorDescription, " +
        "hint=$hint, email=$email)"


    // endregion Inherited methods

}
