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

package com.codepunk.core.data.remote.entity

import com.codepunk.core.data.remote.annotation.BooleanInt
import com.squareup.moshi.Json
import java.util.*

/**
 * A data class representing a user.
 */
data class RemoteUser(

    /**
     * The user id.
     */
    val id: Long,

    /**
     * The username.
     */
    val username: String,

    /**
     * The user's email.
     */
    val email: String,

    /**
     * The user's family name.
     */
    @field:Json(name = "family_name")
    val familyName: String,

    /**
     * The user's given name.
     */
    @field:Json(name = "given_name")
    val givenName: String,

    /**
     * Whether the user is active.
     */
    @field:Json(name = "active")
    @field:BooleanInt
    val active: Boolean,

    /**
     * The date the user was created.
     */
    @field:Json(name = "created_at")
    val createdAt: Date,

    /**
     * The date the user was last updated.
     */
    @field:Json(name = "updated_at")
    val updatedAt: Date

)
