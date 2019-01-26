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
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.codepunk.core.BuildConfig
import com.codepunk.core.data.local.dao.UserDao
import com.codepunk.core.data.mapper.toLocalUser
import com.codepunk.core.data.mapper.toUser
import com.codepunk.core.data.mapper.toUserOrNull
import com.codepunk.core.data.remote.entity.RemoteUser
import com.codepunk.core.data.remote.webservice.UserWebservice
import com.codepunk.core.di.component.UserComponent
import com.codepunk.core.domain.contract.SessionRepository
import com.codepunk.core.domain.model.User
import com.codepunk.core.domain.model.auth.AuthTokenType
import com.codepunk.core.domain.session.Session
import com.codepunk.core.lib.exception.AuthenticationException
import com.codepunk.core.lib.getAccountByNameAndType
import com.codepunk.core.lib.getResultUpdate
import com.codepunk.doofenschmirtz.util.taskinator.*
import retrofit2.Response

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

    private val sessionLiveData: MediatorLiveData<DataUpdate<User, Session>> = MediatorLiveData()

    private var sessionTask: SessionTask? = null

    // endregion Properties

    // region Implemented methods

    override fun openSession(
        silentMode: Boolean
    ): LiveData<DataUpdate<User, Session>> {
        sessionTask?.apply {
            sessionLiveData.removeSource(liveData)
            if (!isCancelled) {
                cancel(true)
            }
            sessionTask = null
        }
        /*
        when {
            alwaysRefresh -> {
                sessionTask?.apply {
                    sessionLiveData.removeSource(liveData)
                    if (!isCancelled) {
                        cancel(true)
                    }
                    sessionTask = null
                }
            }
            sessionTask?.isCancelled == true -> {
                sessionTask?.apply {
                    sessionLiveData.removeSource(liveData)
                    sessionTask = null
                }
            }
        }
        */

        if (sessionTask == null) {
            sessionTask = SessionTask(
                sharedPreferences,
                userDao,
                accountManager,
                userWebservice,
                userComponentBuilder
            ).apply {
                sessionLiveData.addSource(liveData) { update ->
                    // TODO Check cancelled here? Or just let it happen?
                    Log.d("SessionRepositoryImpl", "update=$update")
                    sessionLiveData.value = update
                }
                executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, silentMode)
            }
        }

        return sessionLiveData
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
            // TODO NEXT This might be ResultUpdate<User, Session> because I might want to
            // cache/retrieve the last known authenticated user

            // TODO Check for isCanceled

            val silentMode = params.getOrNull(0) ?: true
            val accountName: String? = sharedPreferences.getString(
                BuildConfig.PREF_KEY_CURRENT_ACCOUNT_NAME,
                null
            )

            // 1) Get any cached authenticated user
            val user: User? = accountName?.run {
                val localUser = userDao.retrieveByUsername(this)
                localUser.toUserOrNull().apply {
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
                    BuildConfig.CATEGORY_CREATE_ACCOUNT
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
                            val localUser = toLocalUser()
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
                                        localUser.toUser()
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
                Bundle().apply {
                    putParcelable(
                        BuildConfig.KEY_INTENT,
                        Intent(BuildConfig.ACTION_AUTHORIZATION).apply {
                            addCategory(category)
                            account?.name?.also { name ->
                                putExtra(BuildConfig.EXTRA_USERNAME, name)
                            }
                        }
                    )
                }
            } else {
                null
            }
        )

        // endregion Methods

    }

    // endregion Companion object

}
