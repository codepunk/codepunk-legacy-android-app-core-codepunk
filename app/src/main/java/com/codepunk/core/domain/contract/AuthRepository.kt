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

import androidx.lifecycle.LiveData
import com.codepunk.core.domain.model.Authentication
import com.codepunk.core.domain.model.Message
import com.codepunk.doofenschmirtz.util.taskinator.DataUpdate

/**
 * A data repository that defines auth-related methods.
 */
interface AuthRepository {

    // region Methods

    fun authenticate(
        username: String,
        password: String
    ): LiveData<DataUpdate<Void, Authentication>>

    fun register(
        username: String,
        email: String,
        password: String,
        passwordConfirmation: String
    ): LiveData<DataUpdate<Void, Message>>

    fun sendActivationCode(
        email: String
    ): LiveData<DataUpdate<Void, Message>>

    fun sendPasswordResetLink(
        email: String
    ): LiveData<DataUpdate<Void, Message>>

    // endregion Methods

}
