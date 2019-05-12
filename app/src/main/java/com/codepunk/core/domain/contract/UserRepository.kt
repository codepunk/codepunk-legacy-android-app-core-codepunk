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

package com.codepunk.core.domain.contract

import androidx.lifecycle.LiveData
import com.codepunk.core.domain.model.User
import com.codepunk.doofenschmirtz.util.resourceinator.Resource

/**
 * A data repository that defines user-related data access methods.
 */
interface UserRepository {

    // region Methods

    /**
     * Gets [LiveData] [Resource]s related to the current user, if one exists.
     */
    fun authenticateUser(
        forceRefresh: Boolean = true,
        silentMode: Boolean = true
    ): LiveData<Resource<Any, User>>

    // endregion Methods

}
