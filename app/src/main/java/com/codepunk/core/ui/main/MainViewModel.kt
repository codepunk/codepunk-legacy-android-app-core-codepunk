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

import android.accounts.AccountManager
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import com.codepunk.core.CodepunkApp
import com.codepunk.core.data.remote.webservice.UserWebservice
import com.codepunk.core.domain.session.SessionManager
import javax.inject.Inject

/**
 * An [AndroidViewModel] for managing primary data for the Codepunk application.
 */
@Suppress("UNUSED")
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

    // endregion Properties

    // region Methods

    // endregion Methods

}
