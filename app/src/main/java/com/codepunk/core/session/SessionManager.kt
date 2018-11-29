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

import android.accounts.Account
import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import com.codepunk.core.BuildConfig.*
import com.codepunk.core.data.model.auth.AuthTokenType
import com.codepunk.core.data.model.auth.AuthTokenType.DEFAULT
import com.codepunk.core.data.remote.webservice.UserWebservice
import com.codepunk.core.di.component.UserComponent
import com.codepunk.core.lib.*
import com.codepunk.core.data.task.*
import java.io.IOException
import javax.inject.Singleton

/**
 * Class that manages any currently-logged in user session.
 */
@Singleton
class SessionManager(

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
    private val userComponentBuilder: UserComponent.Builder

) {

    // region Properties

    /**
     * A [Session] instance for storing the current session.
     */
    var session: Session? = null
        private set

    /**
     * Any currently-running session task.
     */
    private var sessionTask: DataTask<Void, Void, Session>? = null

    /**
     * An observable [Session] wrapped in a [DataUpdate] so observers can be notified of
     * changes to the status of the current session.
     */
    private var liveSession = MediatorLiveData<DataUpdate<Void, Session>>().apply {
        value = PendingUpdate()
    }

    // endregion Properties

    // region Methods

    /**
     * Executes a new [SessionTask] to authenticate a user of the application. An existing
     * task can be canceled using the [cancelExisting] option.
     */
    fun openSession(
        cancelExisting: Boolean = true,
        silentMode: Boolean = false
    ): LiveData<DataUpdate<Void, Session>> {
        sessionTask?.apply {
            if (cancelExisting) {
                cancel(true)
            } else {
                return liveData
            }
        }

        SessionTask(silentMode).apply {
            sessionTask = this
            liveSession.addSource(liveData) {
                liveSession.value = it
            }
            fetchOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
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
    fun observeSession(owner: LifecycleOwner, observer: Observer<DataUpdate<Void, Session>>) {
        liveSession.observe(owner, observer)
    }

    // endregion Methods

    // region Nested/inner classes

    /**
     * A [DataTask] that works with [AccountManager] to authenticate a user in the system and
     * create a new [Session] object to track that user.
     */
    @SuppressLint("StaticFieldLeak")
    private inner class SessionTask(

        /**
         * A flag indicating whether we are running in "silent mode" (i.e. no intents will
         * be generated to present to the user on failure).
         */
        private val silentMode: Boolean

    ) : DataTask<Void, Void, Session>() {

        // region Inherited methods

        override fun generateUpdate(vararg params: Void?): DataUpdate<Void, Session> {
            // TODO Check for isCanceled.

            // Get all saved accounts for type AUTHENTICATOR_ACCOUNT_TYPE
            val type: String = AUTHENTICATOR_ACCOUNT_TYPE
            val accounts = accountManager.getAccountsByType(type)

            // Get the "current" account. The current account is either the account whose name has
            // been saved in shared preferences, or the sole account for the given type if only
            // one account has been stored via the account manager
            val account = when (accounts.size) {
                0 -> return newFailureUpdate(
                    SecurityException("There are no accounts in the account manager"),
                    CATEGORY_CREATE_ACCOUNT
                )
                1 -> accounts[0]
                else -> sharedPreferences.getString(
                    PREF_KEY_CURRENT_ACCOUNT_NAME,
                    null
                )?.let {
                    accountManager.getAccountByNameAndType(it, type)
                }
            } ?: return newFailureUpdate(
                SecurityException("Could not determine the current account"),
                CATEGORY_MAIN
            )

            // 3) Get the auth token associated with the account
            val authToken = try {
                accountManager.blockingGetAuthToken(
                    account,
                    AuthTokenType.DEFAULT.value,
                    false
                ) ?: return newFailureUpdate(
                    SecurityException("Authentication failed getting auth token"),
                    CATEGORY_LOG_IN,
                    account.name)
            } catch (e: Exception) {
                return newFailureUpdate(
                    SecurityException(e),
                    CATEGORY_LOG_IN,
                    account.name
                )
            }

            // 4) Create a temporary session. This will be needed for the getUser call below.
            val tempSession = Session(
                account.name,
                account.type,
                authToken,
                accountManager.getPassword(account),
                userComponentBuilder.build()
            )
            session = tempSession

            try {
                val response = userWebservice.getUser().execute()
                when {
                    response.isSuccessful -> {
                        // Create a new session from the temporary one, substituting in the
                        // newly-fetched user
                        val user = response.body()
                        session = Session(tempSession, user)
                    }
                    else -> return FailureUpdate(e = HttpStatusException(response.code()))
                }
            } catch (e: IOException) {
                return FailureUpdate(e = e)
            }

            return SuccessUpdate(session)
        }

        // endregion Inherited methods

        // region Methods

        /**
         * Generates a [FailureUpdate] with the supplied parameters.
         */
        fun newFailureUpdate(
            e: Exception,
            category: String,
            username: String? = null
        ): FailureUpdate<Void, Session> {
            return FailureUpdate(
                e = e,
                data = if (silentMode) {
                    null
                } else {
                    Bundle().apply {
                        putParcelable(
                            KEY_INTENT,
                            Intent(ACTION_AUTHORIZATION).apply {
                                addCategory(category)
                                if (username != null) {
                                    putExtra(EXTRA_USERNAME, username)
                                }
                            }
                        )
                    }
                }
            )
        }

        // endregion Methods

    }

    // endregion Nested/inner classes

}
