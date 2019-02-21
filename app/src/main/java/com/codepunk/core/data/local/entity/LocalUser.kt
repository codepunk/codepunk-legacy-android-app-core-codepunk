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

package com.codepunk.core.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/**
 * A data class representing a local user.
 */
@Entity(tableName = "users")
data class LocalUser(

    /**
     * The user id.
     */
    @PrimaryKey
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
    @ColumnInfo(name = "family_name")
    val familyName: String?,

    /**
     * The user's given name.
     */
    @ColumnInfo(name = "given_name")
    val givenName: String?,

    /**
     * Whether the user is active.
     */
    @ColumnInfo(name = "active")
    val active: Boolean,

    /**
     * The date the user was created.
     */
    @ColumnInfo(name = "created_at")
    val createdAt: Date,

    /**
     * The date the user was last updated.
     */
    @ColumnInfo(name = "updated_at")
    val updatedAt: Date

)
