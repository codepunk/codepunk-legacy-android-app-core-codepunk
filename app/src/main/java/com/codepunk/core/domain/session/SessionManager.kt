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

import android.accounts.Account
import android.accounts.AccountManager
import android.content.SharedPreferences
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import com.codepunk.core.BuildConfig.PREF_KEY_CURRENT_ACCOUNT_NAME
import com.codepunk.core.data.remote.webservice.UserWebservice
import com.codepunk.core.di.component.UserComponent
import com.codepunk.core.domain.contract.SessionRepository
import com.codepunk.core.domain.model.User
import com.codepunk.core.domain.model.auth.AuthTokenType.DEFAULT
import com.codepunk.doofenschmirtz.util.taskinator.DataUpdate
import com.codepunk.doofenschmirtz.util.taskinator.PendingUpdate
import com.codepunk.doofenschmirtz.util.taskinator.SuccessUpdate
import javax.inject.Inject
import javax.inject.Singleton

/* **************************************************
 *  TODO This is in the domain package but SessionTaskinator knows about
 *  data (!) So I need to employ UseCases in there instead of doing it directly (
 ***************************************************/

/**
 * Class that manages any currently-logged in user session.
 */
@Singleton
class SessionManager @Inject constructor(

    /**
     * The account manager.
     */
    private val accountManager: AccountManager,

    /**
     * The application [SharedPreferences].
     */
    private val sharedPreferences: SharedPreferences,

    /**
     * The user webservice.
     */
    private val userWebservice: UserWebservice,

    /**
     * A [UserComponent.Builder] for creating a [UserComponent] instance.
     */
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

    /*
    /**
     * Any currently-running session task.
     */
    private var sessionTaskinator: DataTaskinator<Void, Void, Session>? = null
    */

    /**
     * An observable [Session] wrapped in a [DataUpdate] so observers can be notified of
     * changes to the status of the current session.
     */
    private val liveSession = MediatorLiveData<DataUpdate<User, Session>>().apply {
        value = PendingUpdate()
    }

    private var liveSessionSource: LiveData<DataUpdate<User, Session>>? = null

    // endregion Properties

    // region Methods

    /**
     * Attempts to open a session to establish the current application user.
     */
    fun getSession(
        silentMode: Boolean,
        refresh: Boolean = false
    ): LiveData<DataUpdate<User, Session>> {
        liveSessionSource?.run { liveSession.removeSource(this) }
        liveSessionSource = sessionRepository.getSession(silentMode, refresh).apply {
            liveSession.addSource(this) {
                session = when (it) {
                    is SuccessUpdate -> it.result
                    else -> null
                }
                liveSession.value = it
            }
        }
        return liveSession
    }

    /**
     * Closes a session if one is open. Returns true if there was an existing session that was
     * closed and false otherwise. If [logOut] is set, the user's tokens will also be cleared,
     * meaning that the next time the user attempts to log on, they will have to re-enter their
     * credentials.
     */
    fun closeSession(logOut: Boolean): Boolean {
        // TODO Log out? Call a closeSession method on Repository?
        return session?.let {
            sharedPreferences.edit().remove(PREF_KEY_CURRENT_ACCOUNT_NAME).apply()
            if (logOut) {
                Account(it.accountName, it.accountType).apply {
                    accountManager.setAuthToken(this, DEFAULT.value, null)
                    accountManager.setPassword(this, null)
                }
            }
            liveSession.value = PendingUpdate()
            session = null
            true
        } ?: false
    }

    /**
     * Allows [owner] to observe changes to the state of [session].
     */
    fun observeSession(owner: LifecycleOwner, observer: Observer<DataUpdate<User, Session>>) {
        liveSession.observe(owner, observer)
    }

    // endregion Methods

}
