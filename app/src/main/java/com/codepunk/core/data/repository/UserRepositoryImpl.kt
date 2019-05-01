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

package com.codepunk.core.data.repository

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.codepunk.core.BuildConfig.*
import com.codepunk.core.data.local.dao.UserDao
import com.codepunk.core.data.mapper.toDomainOrNull
import com.codepunk.core.data.remote.webservice.UserWebservice
import com.codepunk.core.domain.contract.UserRepository
import com.codepunk.core.domain.model.User
import com.codepunk.core.domain.model.AuthTokenType
import com.codepunk.core.lib.exception.AuthenticationException
import com.codepunk.core.lib.getAccountByNameAndType
import com.codepunk.doofenschmirtz.util.taskinator.DataTaskinator
import com.codepunk.doofenschmirtz.util.taskinator.DataUpdate
import com.codepunk.doofenschmirtz.util.taskinator.FailureUpdate
import com.codepunk.doofenschmirtz.util.taskinator.ResultUpdate

/**
 * A repository for accessing and manipulating user-related data.
 */
class UserRepositoryImpl(

    /**
     * The [UserDao] singleton instance.
     */
    private val userDao: UserDao,

    /**
     * The account manager.
     */
    private val accountManager: AccountManager,

    /**
     * The application [SharedPreferences].
     */
    private val sharedPreferences: SharedPreferences,

    /**
     * An instance of [UserWebservice] for making user-related API calls.
     */
    private val userWebservice: UserWebservice

) : UserRepository {

    // region Properties

    private val userLiveData: MediatorLiveData<DataUpdate<Any, User>> = MediatorLiveData()

    private var userTask: UserTask? = null

    // endregion Properties

    // region Methods

    /**
     * Gets [LiveData] updates related to the current user, if one exists.
     */
    override fun authenticateUser(
        forceRefresh: Boolean,
        silentMode: Boolean
    ): LiveData<DataUpdate<Any, User>> {
        when {
            forceRefresh -> {
                userTask?.apply {
                    userLiveData.removeSource(liveData)
                    cancel(true)
                    userTask = null
                }
            }
            userTask?.isCancelled == true -> {
                userTask?.apply {
                    userLiveData.removeSource(liveData)
                    userTask = null
                }
            }
        }

        if (userTask == null) {
            userTask = UserTask(
                userDao,
                accountManager,
                sharedPreferences
            ).apply {
                userLiveData.addSource(liveData) { update ->
                    // TODO Check cancelled here? Or just let it happen?
                    userLiveData.value = update
                }
                executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, silentMode)
            }
        }

        return userLiveData
    }

    // endregion Methods

    // region Nested/inner classes

    private class UserTask(

        private val userDao: UserDao,

        private val accountManager: AccountManager,

        private val sharedPreferences: SharedPreferences

    ) : DataTaskinator<Boolean, Any, User>() {

        // region Inherited methods

        override fun doInBackground(vararg params: Boolean?): ResultUpdate<Any, User> {
            val silentMode = params.getOrNull(0) ?: true
            val accountName: String? = sharedPreferences.getString(
                PREF_KEY_CURRENT_ACCOUNT_NAME,
                null
            )

            // 1) Get cached user if one exists
            var cachedUser: User? = null
            accountName?.run {
                val localUser = userDao.retrieveByUsername(this)
                cachedUser = localUser.toDomainOrNull()
            }
            cachedUser?.run {
                publishProgress(cachedUser)
            }

            // 2) Get all saved accounts for type AUTHENTICATOR_ACCOUNT_TYPE
            val type: String = AUTHENTICATOR_ACCOUNT_TYPE
            val accounts = accountManager.getAccountsByType(type)

            // 3) Get the "current" account. The current account is either the account whose name has
            // been saved in shared preferences, or the sole account for the given type if only
            // one account has been stored via the account manager
            val account: Account = when {
                accounts.isEmpty() -> return FailureUpdate(
                    e = AuthenticationException("There are no accounts in the account manager"),
                    data = makeDataBundle(silentMode, CATEGORY_REGISTER)
                )
                accounts.size == 1 -> accounts[0]
                accountName != null -> accountManager.getAccountByNameAndType(accountName, type)
                else -> null
            } ?: return FailureUpdate(
                e = AuthenticationException("Could not determine the current account"),
                data = makeDataBundle(silentMode, CATEGORY_MAIN)
            )

            publishProgress(cachedUser, account)

            // 4) Get the auth token associated with the account
            val authToken = try {
                accountManager.blockingGetAuthToken(
                    account,
                    AuthTokenType.DEFAULT.value,
                    false
                ) ?: return FailureUpdate(
                    e = AuthenticationException("Authentication failed getting auth token"),
                    data = makeDataBundle(silentMode, CATEGORY_LOG_IN, account.name)
                )
            } catch (e: Exception) {
                return FailureUpdate(
                    e = AuthenticationException(e),
                    data = makeDataBundle(silentMode, CATEGORY_LOG_IN, account.name)
                )
            }

            // 5) Create a temporary session. This will be needed for the getUser call below.

            // TODO This is going to be very similar to SessionTaskinator in SessionManager.

            TODO("not implemented")
        }

        // endregion Inherited methods

        // region Companion object

        companion object {

            @JvmStatic
            fun makeDataBundle(
                silentMode: Boolean,
                category: String,
                username: String? = null
            ): Bundle? = when (silentMode) {
                true -> null
                else -> Bundle().apply {
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

        }

        // endregion Companion object

    }

    // endregion Nested/inner classes

}
