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

package com.codepunk.core.session

import android.content.Context
import com.codepunk.core.di.qualifier.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Class that manages any currently-logged in user session.
 */
@Singleton
class SessionManager @Inject constructor(

    /**
     * The application [Context].
     */
    @ApplicationContext val context: Context

) {

    /**
     * The currently-active session, if any.
     */
    var session: Session? = null

}
