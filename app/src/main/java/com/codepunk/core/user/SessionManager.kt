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

package com.codepunk.core.user

import android.content.Context
import com.codepunk.core.di.component.UserComponent
import com.codepunk.core.di.qualifier.ApplicationContext
import javax.inject.Singleton

/**
 * Class that manages any currently-logged in user session.
 */
@Singleton
class SessionManager(

    /**
     * The application [Context].
     */
    @ApplicationContext val context: Context,

    /**
     * A [UserComponent.Builder] for creating a [UserComponent] instance.
     */
    private val userComponentBuilder: UserComponent.Builder

) {

    // region Properties

    /**
     * The user component for dependency injection.
     */
    private var userComponent: UserComponent? = null

    /**
     * The currently-active session, if any.
     */
    @Suppress("UNUSED")
    var session: Session? = null

    // endregion Properties

    // region Methods

    /**
     * Opens a user session, i.e. a user has just successfully logged on.
     */
    fun openSession() {
        userComponent = userComponentBuilder.build()
    }

    /**
     * Closes the user session, i.e. a user has just logged off.
     */
    fun closeSession() {
        userComponent = null
    }

    // endregion Methods

}
