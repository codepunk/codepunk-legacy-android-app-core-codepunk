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

package com.codepunk.core.user

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.content.SharedPreferences
import com.codepunk.core.BuildConfig.PREF_KEY_CURRENT_ACCOUNT_NAME
import com.codepunk.core.data.model.auth.AuthTokenType
import com.codepunk.core.di.component.UserComponent
import com.codepunk.core.di.qualifier.ApplicationContext
import javax.inject.Singleton

/**
 * Class that manages any currently-logged in user session.
 */
@Singleton
class SessionManager(

    /**
     * The application [Context].
     */
    @ApplicationContext private val context: Context,

    /**
     * The account manager.
     */
    private val accountManager: AccountManager,

    /**
     * The application [SharedPreferences].
     */
    private val sharedPreferences: SharedPreferences,

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
     * The user component for dependency injection.
     */
    private var userComponent: UserComponent? = null

    // endregion Properties

    // region Methods

    /**
     * Opens a user session (i.e. a user has just successfully logged on).
     */
    fun openSession(
        accountName: String,
        accountType: String,
        authToken: String,
        refreshToken: String
    ) {
        session = Session(accountName, accountType, authToken, refreshToken)
        userComponent = userComponentBuilder.build()
        sharedPreferences.edit()
            .putString(PREF_KEY_CURRENT_ACCOUNT_NAME, accountName)
            .apply()
    }

    /**
     * Closes the user session, i.e. a user has just logged off.
     */
    fun closeSession(logOut: Boolean): Boolean {
        val hadSession: Boolean = session ?.let {
            sharedPreferences.edit()
                .remove(PREF_KEY_CURRENT_ACCOUNT_NAME)
                .apply()
            if (logOut) {
                val account = Account(it.accountName, it.accountType) //accountManager.getAccountByNameAndType(it.accountName, it.accountType)
                account.run {
                    accountManager.setAuthToken(
                        this,
                        AuthTokenType.DEFAULT.value,
                        null
                    )
                    accountManager.setPassword(
                        this,
                        null
                    )
                }
            }
            true
        } ?: false
        userComponent = null
        session = null
        return hadSession
    }

    // endregion Methods

}
