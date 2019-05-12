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

package com.codepunk.core.domain.contract

import android.content.Intent
import androidx.lifecycle.LiveData
import com.codepunk.core.domain.model.User
import com.codepunk.core.domain.session.Session
import com.codepunk.doofenschmirtz.util.resourceinator.Resource

/**
 * A repository that opens a session (i.e. authenticates a user).
 */
interface SessionRepository {

    /**
     * Gets a [Session] or opens one (i.e. authenticates a user) if no session exists. If
     * [silentMode] is false, authentication failures will be set in the resulting LiveData with
     * an [Intent] that will allow the appropriate authentication activity to be presented.
     * If [refresh] is set, then a new [Session] will be opened regardless of whether an existing
     * session is currently open.
     */
    fun getSession(
        silentMode: Boolean,
        refresh: Boolean = false
    ): LiveData<Resource<User, Session>>

    /**
     * Closes a session if one is open. Returns true if there was an existing session that was
     * closed and false otherwise. If [logOut] is set, the user's tokens will also be cleared,
     * meaning that the next time the user attempts to log on, they will have to re-enter their
     * credentials.
     */
    fun closeSession(session: Session, logOut: Boolean)

}
