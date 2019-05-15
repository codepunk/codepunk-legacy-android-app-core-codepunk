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

import android.accounts.AccountManager
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.codepunk.core.data.remote.webservice.UserWebservice
import com.codepunk.core.di.component.UserComponent
import com.codepunk.core.domain.contract.SessionRepository
import com.codepunk.core.domain.model.User
import com.codepunk.doofenschmirtz.util.resourceinator.PendingResource
import com.codepunk.doofenschmirtz.util.resourceinator.Resource
import com.codepunk.doofenschmirtz.util.resourceinator.SuccessResource
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A manager class that manages any currently-logged in user session.
 */
@Singleton
class SessionManager @Inject constructor(

    /**
     * The account manager.
     */
    @Suppress("UNUSED")
    private val accountManager: AccountManager,

    /**
     * The application [SharedPreferences].
     */
    @Suppress("UNUSED")
    private val sharedPreferences: SharedPreferences,

    /**
     * The user webservice.
     */
    @Suppress("UNUSED")
    private val userWebservice: UserWebservice,

    /**
     * A [UserComponent.Builder] for creating a [UserComponent] instance.
     */
    @Suppress("UNUSED")
    private val userComponentBuilder: UserComponent.Builder,

    /**
     * The [SessionRepository] singleton.
     */
    private val sessionRepository: SessionRepository

) {

    // region Properties

    /**
     * A [Session] instance for storing the current session.
     */
    var session: Session? = null
        private set

    /**
     * An observable [Session] wrapped in a [Resource] so observers can be notified of
     * changes to the status of the current session.
     */
    val sessionLiveResource: MediatorLiveData<Resource<User, Session>> =
        MediatorLiveData<Resource<User, Session>>().apply {
            value = PendingResource()
        }

    /**
     *
     */
    private var sessionSource: LiveData<Resource<User, Session>>? = null
        private set(value) {
            if (field != value) {
                field?.also { source -> sessionLiveResource.removeSource(source) }
                field = value?.apply {
                    sessionLiveResource.addSource(this) { resource ->
                        session = when (resource) {
                            is SuccessResource -> resource.result
                            else -> null
                        }
                        sessionLiveResource.value = resource
                    }
                }
            }
        }

    // endregion Properties

    // region Methods

    /**
     * Attempts to open a session to establish the current application user.
     */
    fun getSession(
        silentMode: Boolean,
        refresh: Boolean = false
    ): LiveData<Resource<User, Session>> {
        sessionSource = sessionRepository.getSession(silentMode, refresh)
        return sessionLiveResource
    }

    /**
     * Closes a session if one is open. Returns true if there was an existing session that was
     * closed and false otherwise. If [logOut] is set, the user's tokens will also be cleared,
     * meaning that the next time the user attempts to log on, they will have to re-enter their
     * credentials.
     */
    fun closeSession(logOut: Boolean): Boolean {
        return session?.let {
            sessionRepository.closeSession(it, logOut)
            sessionLiveResource.value = PendingResource()
            session = null
            true
        } ?: false
    }

    // endregion Methods

}
