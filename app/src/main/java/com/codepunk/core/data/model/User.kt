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

package com.codepunk.core.data.model

import com.squareup.moshi.Json
import java.util.*

/**
 * Data class representing a user.
 */
data class User(

    /**
     * The user id.
     */
    @field:Json(name = "id")
    val id: Long,

    /**
     * The user's name.
     */
    @field:Json(name = "name")
    val name: String,

    /**
     * The user's email.
     */
    @field:Json(name = "email")
    val email: String,

    /**
     * Whether the user is active.
     */
    @field:Json(name = "active")
    val active: Int,

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
