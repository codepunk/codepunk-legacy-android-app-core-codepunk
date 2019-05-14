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

package com.codepunk.core.account

import android.app.Service
import android.content.Intent
import android.os.IBinder
import dagger.android.AndroidInjection
import javax.inject.Inject

/**
 * A [Service] that handles account authentication using [AccountAuthenticator].
 */
class AuthenticatorService : Service() {

    // region Properties

    /**
     * The [AccountAuthenticator] that handles account authentication and management.
     */
    @Inject
    lateinit var accountAuthenticator: AccountAuthenticator

    // endregion Properties

    // region Lifecycle methods

    /**
     * Injects dependencies into this service.
     */
    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
    }

    // endregion Lifecycle methods

    // region Inherited methods

    /**
     * Binds this service to the [AccountAuthenticator].
     */
    override fun onBind(intent: Intent): IBinder {
        return accountAuthenticator.iBinder
    }

    // endregion Inherited methods

}
