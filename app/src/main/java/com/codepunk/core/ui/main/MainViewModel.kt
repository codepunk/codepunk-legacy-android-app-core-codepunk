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

package com.codepunk.core.ui.main

import android.accounts.Account
import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import com.codepunk.core.BuildConfig.*
import com.codepunk.core.CodepunkApp
import com.codepunk.core.data.model.User
import com.codepunk.core.data.model.auth.AuthTokenType
import com.codepunk.core.data.remote.webservice.UserWebservice
import com.codepunk.core.lib.*
import com.codepunk.core.ui.auth.AuthenticateActivity
import com.codepunk.core.user.SessionManager
import com.codepunk.core.user.getAccountByNameAndType
import javax.inject.Inject

/*
 * Author(s): Scott Slater
 */

/*
 * TODO NEXT:
 * Move the entire authenticate() and logOut() methods to SessionManager?
 * It could still have a liveUser LiveData and could still be observed ...
 */

/**
 * An [AndroidViewModel] for managing primary data for the Codepunk application.
 */
class MainViewModel @Inject constructor(

    /**
     * The Codepunk application.
     */
    val app: CodepunkApp,

    /**
     * The application [SharedPreferences].
     */
    val sharedPreferences: SharedPreferences,

    /**
     * The Android account manage.
     */
    val accountManager: AccountManager,

    /**
     * The user webservice.
     */
    val userWebservice: UserWebservice,

    /**
     * The session manager for tracking user session.
     */
    val sessionManager: SessionManager

) : AndroidViewModel(app) {

    // region Properties

    val liveUser = MediatorLiveData<DataUpdate<Void, User>>().apply {
        value = PendingUpdate()
    }

    // endregion Properties

    // region Methods

    @SuppressLint("StaticFieldLeak")
    fun authenticate() {
        val source = object : DataTask<Void, Void, User>() {
            override fun generateUpdate(vararg params: Void?): DataUpdate<Void, User> {

                // 1) Get all accounts for type AUTHENTICATOR_ACCOUNT_TYPE
                val type: String = AUTHENTICATOR_ACCOUNT_TYPE
                val accounts =
                    accountManager.getAccountsByType(type)

                // 2) Find the account that matches saved account name (or the sole account
                // if there's only one)
                val account: Account = when {
                    accounts.isEmpty() -> return FailureUpdate(
                        e = SecurityException("There are no accounts in the account manager"),
                        data = Bundle().apply {
                            putParcelable(
                                KEY_PENDING_INTENT,
                                PendingIntent.getActivity(
                                    app,
                                    ACCOUNT_REQUIRED_REQUEST_CODE,
                                    Intent(app, AuthenticateActivity::class.java).apply {
                                        addCategory(CATEGORY_CREATE_ACCOUNT)
                                    },
                                    PendingIntent.FLAG_ONE_SHOT
                                )
                            )
                        })
                    accounts.size == 1 -> accounts[0]
                    else -> {
                        val name =
                            sharedPreferences.getString(PREF_KEY_CURRENT_ACCOUNT_NAME, null)
                        when (name) {
                            null -> return FailureUpdate(
                                e = SecurityException("There is more than one account in the account manager and no current account was saved"),
                                data = Bundle().apply {
                                    putParcelable(
                                        KEY_PENDING_INTENT,
                                        PendingIntent.getActivity(
                                            app,
                                            ACCOUNT_REQUIRED_REQUEST_CODE,
                                            Intent(app, AuthenticateActivity::class.java).apply {
                                                addCategory(CATEGORY_MAIN)
                                            },
                                            PendingIntent.FLAG_ONE_SHOT
                                        )
                                    )
                                })
                            else -> accountManager.getAccountByNameAndType(name, type)
                        }
                    }
                } ?: return FailureUpdate(
                    e = SecurityException("No account found that matches the current account"),
                    data = Bundle().apply {
                        putParcelable(
                            KEY_PENDING_INTENT,
                            PendingIntent.getActivity(
                                app,
                                ACCOUNT_REQUIRED_REQUEST_CODE,
                                Intent(app, AuthenticateActivity::class.java).apply {
                                    addCategory(CATEGORY_MAIN)
                                },
                                PendingIntent.FLAG_ONE_SHOT
                            )
                        )
                    })

                // We should have a valid Account

                val authToken: String = accountManager.blockingGetAuthToken(
                    account,
                    AuthTokenType.DEFAULT.value,
                    false
                ) ?: return FailureUpdate(
                    e = SecurityException("Authentication failed getting auth token"),
                    data = Bundle().apply {
                        putParcelable(
                            KEY_PENDING_INTENT,
                            PendingIntent.getActivity(
                                app,
                                ACCOUNT_REQUIRED_REQUEST_CODE,
                                Intent(app, AuthenticateActivity::class.java).apply {
                                    addCategory(CATEGORY_LOG_IN) // TODO Supply name and/or email as extra
                                },
                                PendingIntent.FLAG_ONE_SHOT
                            )
                        )
                    })

                sessionManager.openSession(
                    account.name,
                    account.type,
                    authToken,
                    accountManager.getPassword(account)
                )

                return userWebservice.getUser().toDataUpdate()
            }
        }.fetchOnExecutor()
        liveUser.addSource(source) { liveUser.value = it }
    }

    fun logOut() {
        if (sessionManager.closeSession(true)) {
            liveUser.value = PendingUpdate()
        }
    }

    // endregion Methods

}
