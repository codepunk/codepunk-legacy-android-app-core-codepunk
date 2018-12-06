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

package com.codepunk.core.domain.session

import com.codepunk.core.domain.model.User
import com.codepunk.core.di.component.UserComponent
import com.codepunk.core.di.scope.UserScope
import java.util.*

private val PENDING_USER = Date(0L).let {
    User(-1, "", "", "", "", false, it, it)
}

/**
 * A class with information about the current user session.
 */
class Session(

    /**
     * The name of the authenticated account.
     */
    val accountName: String,

    /**
     * The type of the authenticated account.
     */
    val accountType: String,

    /**
     * The auth token for the authenticated account.
     */
    val authToken: String,

    /**
     * The refresh token for the authenticated account.
     */
    @Suppress("WEAKER_ACCESS")
    val refreshToken: String,

    /**
     * The [UserComponent] for [UserScope]-based dependency injection.
     */
    @Suppress("WEAKER_ACCESS")
    val userComponent: UserComponent,

    /**
     * The authenticated user.
     */
    val user: User = PENDING_USER

) {

    // region Constructors

    /**
     * Copy constructor that optionally replaces the supplied [user].
     */
    constructor(session: Session, user: User? = null) : this(
        session.accountName,
        session.accountType,
        session.authToken,
        session.refreshToken,
        session.userComponent,
        when (user) {
            null -> session.user
            else -> user
        }
    )

    // endregion Constructors

}
