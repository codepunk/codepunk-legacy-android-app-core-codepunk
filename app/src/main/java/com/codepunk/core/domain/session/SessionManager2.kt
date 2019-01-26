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

package com.codepunk.core.domain.session

import android.accounts.Account
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.codepunk.core.domain.contract.SessionRepository
import com.codepunk.core.domain.contract.UserRepository
import com.codepunk.core.domain.model.User
import com.codepunk.doofenschmirtz.util.taskinator.DataUpdate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager2 @Inject constructor(

    private val sessionRepository: SessionRepository,

    private val userRepository: UserRepository

) {

    // region Properties

    val userLiveData: MediatorLiveData<DataUpdate<User, User>> = MediatorLiveData()

    private var sessionLiveData: LiveData<DataUpdate<User, Session>>? = null

    // endregion Properties

    // region Methods

    fun authenticate(
        silentMode: Boolean
    ): LiveData<DataUpdate<User, User>> {
        sessionLiveData?.run {
            userLiveData.removeSource(this)
        }
        sessionLiveData = sessionRepository.openSession(silentMode).apply {
            userLiveData.addSource(this) { update ->
                Log.d("SessionManager", "update=$update")
            }
        }
        return userLiveData
    }

    fun authenticateUser(forceRefresh: Boolean = false): LiveData<DataUpdate<Any, User>> =
        userRepository.authenticateUser(forceRefresh)

    // endregion Methods

}
