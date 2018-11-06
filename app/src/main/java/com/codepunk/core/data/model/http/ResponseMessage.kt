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

package com.codepunk.core.data.model.http

/**
 * A response message from the server. This response contains a [message] string and
 * optional [errors] detailing any issues discovered during the request.
 */
data class ResponseMessage(

    /**
     * A string message.
     */
    val message: String,

    /**
     * Any errors that were discovered during the request.
     */
    val errors: Map<String, Array<String>>? = null

)
