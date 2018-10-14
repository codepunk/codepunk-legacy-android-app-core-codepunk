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

package com.codepunk.core.ui.auth

import android.accounts.AccountManager
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.codepunk.core.BuildConfig
import com.codepunk.core.data.model.User
import com.codepunk.core.data.repository.DataUpdate
import com.codepunk.core.data.repository.UserRepository
import javax.inject.Inject

/**
 * A [ViewModel] for managing account-related data.
 */
class AccountViewModel @Inject constructor(

    /**
     * The repository for accessing and manipulating user-related data.
     */
    private val userRepository: UserRepository,

    /**
     * The application shared preferences.
     */
    private val sharedPreferences: SharedPreferences,

    /**
     * The Android [AccountManager].
     */
    @Suppress("UNUSED")
    private val accountManager: AccountManager

) : ViewModel() {

    // region Properties

    /**
     * A [LiveData] that will serve as the trigger to attempt to authenticate the user.
     */
    private val attemptAuthenticate = MutableLiveData<Boolean>()

    /**
     * The [LiveData] that will hold the result of user authentication.
     */
    val userTask: LiveData<DataUpdate<User, User>> = Transformations
        .switchMap(attemptAuthenticate) {
            when (it) {
                true -> userRepository.authenticate()
                else -> null
            }
        }

    // endregion Properties

    // region Methods

    /**
     * Tries to authenticate the user.
     */
    fun authenticate() {
        // Get a list of accounts from AccountManager
//        accountManager.getAccountsByType()

        // First, see if we saved an account name
        sharedPreferences.getString(BuildConfig.PREF_KEY_CURRENT_ACCOUNT, null)?.run {

        } ?: run {
            ""
        }

        attemptAuthenticate.value = true
    }

    // endregion methods

}
