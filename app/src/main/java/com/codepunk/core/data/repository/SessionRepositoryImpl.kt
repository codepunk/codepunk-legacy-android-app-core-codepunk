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

package com.codepunk.core.data.repository

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import androidx.lifecycle.LiveData
import com.codepunk.core.BuildConfig
import com.codepunk.core.BuildConfig.CATEGORY_REGISTER
import com.codepunk.core.data.local.dao.UserDao
import com.codepunk.core.data.mapper.toLocal
import com.codepunk.core.data.mapper.toDomain
import com.codepunk.core.data.mapper.toDomainOrNull
import com.codepunk.core.data.remote.entity.RemoteUser
import com.codepunk.core.data.remote.webservice.UserWebservice
import com.codepunk.core.di.component.UserComponent
import com.codepunk.core.domain.contract.SessionRepository
import com.codepunk.core.domain.model.User
import com.codepunk.core.domain.model.AuthTokenType
import com.codepunk.core.domain.session.Session
import com.codepunk.core.lib.exception.AuthenticationException
import com.codepunk.core.lib.getAccountByNameAndType
import com.codepunk.core.lib.getResultUpdate
import com.codepunk.doofenschmirtz.util.taskinator.*
import retrofit2.Response

/**
 * Implementation of [SessionRepository] that attempts to open a session (i.e. authenticate a
 * user).
 */
class SessionRepositoryImpl(

    /**
     * The application [SharedPreferences].
     */
    private val sharedPreferences: SharedPreferences,

    /**
     * The [UserDao] singleton instance.
     */
    private val userDao: UserDao,

    /**
     * The account manager.
     */
    private val accountManager: AccountManager,

    /**
     * The user webservice.
     */
    private val userWebservice: UserWebservice,

    /**
     * A [UserComponent.Builder] for creating a [UserComponent] instance.
     */
    private val userComponentBuilder: UserComponent.Builder

) : SessionRepository {

    // region Properties

    private var sessionTask: SessionTask? = null

    // endregion Properties

    // region Implemented methods

    /**
     * Gets a [Session] or opens one (i.e. authenticates a user) if no session exists. If
     * [silentMode] is false, authentication failures will be set in the resulting LiveData with
     * an [Intent] that will allow the appropriate authentication activity to be presented.
     * If [refresh] is set, then a new [Session] will be opened regardless of whether an existing
     * session is currently open.
     */
    override fun getSession(
        silentMode: Boolean,
        refresh: Boolean
    ): LiveData<DataUpdate<User, Session>> {
        sessionTask?.run {
            if (refresh) {
                this.cancel(true)
            } else {
                return this.liveData
            }
        }

        return SessionTask(
            sharedPreferences,
            userDao,
            accountManager,
            userWebservice,
            userComponentBuilder
        ).let {
            sessionTask = it
            it.executeOnExecutorAsLiveData(AsyncTask.THREAD_POOL_EXECUTOR, silentMode)
        }
    }

    /**
     * Closes a session if one is open. Returns true if there was an existing session that was
     * closed and false otherwise. If [logOut] is set, the user's tokens will also be cleared,
     * meaning that the next time the user attempts to log on, they will have to re-enter their
     * credentials.
     */
    override fun closeSession(session: Session, logOut: Boolean) {
        sharedPreferences.edit().remove(BuildConfig.PREF_KEY_CURRENT_ACCOUNT_NAME).apply()
        if (logOut) {
            Account(session.accountName, session.accountType).apply {
                accountManager.setAuthToken(this, AuthTokenType.DEFAULT.value, null)
                accountManager.setPassword(this, null)
            }
        }
    }

    // endregion Implemented methods

    // region Nested/inner classes

    private class SessionTask(

        private val sharedPreferences: SharedPreferences,

        private val userDao: UserDao,

        private val accountManager: AccountManager,

        private val userWebservice: UserWebservice,

        private val userComponentBuilder: UserComponent.Builder

    ) : DataTaskinator<Boolean, User, Session>() {

        // region Inherited methods

        override fun doInBackground(vararg params: Boolean?): ResultUpdate<User, Session> {
            // TODO Check for isCanceled

            val silentMode = params.getOrNull(0) ?: true
            val accountName: String? = sharedPreferences.getString(
                BuildConfig.PREF_KEY_CURRENT_ACCOUNT_NAME,
                null
            )

            // 1) Get any cached authenticated user
            val user: User? = accountName?.run {
                val localUser = userDao.retrieveByUsername(this)
                localUser.toDomainOrNull().apply {
                    publishProgress(this)
                }
            }

            // 2) Get all saved accounts for type AUTHENTICATOR_ACCOUNT_TYPE
            val type: String = BuildConfig.AUTHENTICATOR_ACCOUNT_TYPE
            val accounts = accountManager.getAccountsByType(type)

            // 3) Get the "current" account. The current account is either the account whose name has
            // been saved in shared preferences, or the sole account for the given type if only
            // one account has been stored via the account manager
            val account: Account = when {
                accounts.isEmpty() -> return makeFailureUpdate(
                    silentMode,
                    AuthenticationException("There are no accounts in the account manager"),
                    CATEGORY_REGISTER
                )
                accounts.size == 1 -> accounts[0]
                accountName != null -> accountManager.getAccountByNameAndType(accountName, type)
                else -> null
            } ?: return makeFailureUpdate(
                silentMode,
                AuthenticationException("Could not determine the current account"),
                BuildConfig.CATEGORY_MAIN
            )

            // TODO NEXT Does it make sense to have steps 4 & 5 in a loop in case refresh token
            // is stale?
            var e: Exception? = null
            var done = false
            while (!done) {

                // 4) Get the auth token associated with the account and create and return a
                // new session instance with a pending user
                val authToken: String = try {
                    accountManager.blockingGetAuthToken(
                        account,
                        AuthTokenType.DEFAULT.value,
                        false
                    ) ?: return makeFailureUpdate(
                        silentMode,
                        AuthenticationException("Authentication failed getting auth token"),
                        BuildConfig.CATEGORY_LOG_IN,
                        account
                    )
                } catch (e: Exception) {
                    return makeFailureUpdate(
                        silentMode,
                        AuthenticationException(e),
                        BuildConfig.CATEGORY_LOG_IN,
                        account
                    )
                }

                // 5) Get the authenticated user from the network
                val update: ResultUpdate<Void, Response<RemoteUser>> =
                    userWebservice.getUser(authToken).getResultUpdate()
                when (update) {
                    is SuccessUpdate -> {
                        update.result?.body()?.run {
                            val localUser = toLocal()
                            if (user == null || userDao.update(localUser) == 0) {
                                userDao.insert(localUser)
                            }

                            userDao.retrieveByUsername(account.name)?.run {
                                return SuccessUpdate(
                                    Session(
                                        account.name,
                                        account.type,
                                        authToken,
                                        accountManager.getPassword(account),
                                        userComponentBuilder.build(),
                                        localUser.toDomain()
                                    )
                                )
                            }
                        }
                    }
                    is FailureUpdate -> {
                        e = update.e
                        // TODO NEXT If unauthorized, try with refresh token
                    }
                }

                done = true // TODO TEMP
            }

            return makeFailureUpdate(
                silentMode,
                AuthenticationException("Authentication failed getting remote user", e),
                BuildConfig.CATEGORY_LOG_IN,
                account
            )
        }

        // endregion Inherited methods

    }

    // endregion Nested/inner classes

    // region Companion object

    companion object {

        // region Methods

        @JvmStatic
        fun makeFailureUpdate(
            silentMode: Boolean,
            e: Exception?,
            category: String,
            account: Account? = null
        ): FailureUpdate<User, Session> = FailureUpdate(
            null,
            e,
            if (silentMode) {
                null
            } else {
                Bundle().apply {
                    putParcelable(
                        BuildConfig.KEY_INTENT,
                        Intent(BuildConfig.ACTION_AUTHENTICATION).apply {
                            addCategory(category)
                            account?.name?.also { name ->
                                putExtra(BuildConfig.EXTRA_USERNAME, name)
                            }
                        }
                    )
                }
            }
        )

        // endregion Methods

    }

    // endregion Companion object

}
