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

package com.codepunk.core.data.mapper

import com.codepunk.core.data.local.entity.LocalUser
import com.codepunk.core.data.remote.entity.RemoteUser
import com.codepunk.core.domain.model.User

/**
 * Converts a [RemoteUser] to a locally-cached [LocalUser].
 */
fun RemoteUser.toLocal(): LocalUser = LocalUser(
    id,
    username,
    email,
    familyName,
    givenName,
    active,
    createdAt,
    updatedAt
)

/**
 * Converts a nullable [RemoteUser] to a nullable locally-cached [LocalUser].
 */
fun RemoteUser?.toLocalOrNull(): LocalUser? = this?.toLocal()

/**
 * Converts a [RemoteUser] to a domain [User].
 */
@Suppress("UNUSED")
fun RemoteUser.toDomain(): User = User(
    id,
    username,
    email,
    familyName,
    givenName,
    active,
    createdAt,
    updatedAt
)

/**
 * Converts a nullable [RemoteUser] to a nullable domain [User].
 */
fun RemoteUser?.toDomainOrNull(): User? = this?.toDomain()

/**
 * Converts a [LocalUser] to a domain [User].
 */
fun LocalUser.toDomain(): User = User(
    id,
    username,
    email,
    familyName,
    givenName,
    active,
    createdAt,
    updatedAt
)

/**
 * Converts a nullable [LocalUser] to a nullable domain [User].
 */
fun LocalUser?.toDomainOrNull(): User? = this?.toDomain()
