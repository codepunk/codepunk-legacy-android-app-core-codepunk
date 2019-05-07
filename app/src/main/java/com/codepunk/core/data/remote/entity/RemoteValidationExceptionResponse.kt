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

/**
 * A response message from the server indicating a (Laravel) ValidationException occurred. This
 * response contains a [message] string and optional [errors] detailing any errors encountered
 * during the request.
 */
data class RemoteValidationExceptionResponse(

    /**
     * A string message.
     */
    val message: String?,

    /**
     * Any errors (relating to specific keys) that were discovered during the request.
     */
    val errors: Map<String, Array<String>>? = null

)
