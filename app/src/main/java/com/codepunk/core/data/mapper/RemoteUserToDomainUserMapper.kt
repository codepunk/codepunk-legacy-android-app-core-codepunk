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

import com.codepunk.core.data.remote.entity.RemoteUser
import com.codepunk.core.data.util.Mapper
import com.codepunk.core.domain.model.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteUserToDomainUserMapper @Inject constructor() : Mapper<RemoteUser, User>() {

    override fun map(source: RemoteUser): User = User(
        source.id,
        source.username,
        source.email,
        source.familyName,
        source.givenName,
        source.active,
        source.createdAt,
        source.updatedAt
    )

}
