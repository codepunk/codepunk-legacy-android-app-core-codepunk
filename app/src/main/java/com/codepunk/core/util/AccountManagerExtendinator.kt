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

package com.codepunk.core.util

import android.accounts.Account
import android.accounts.AccountManager
import android.os.Bundle

/**
 * Attempts to add the supplied [account] to the [AccountManager] and, if it fails, attempts
 * to update an existing account instead.
 */
fun AccountManager.addOrUpdateAccount(
    account: Account,
    password: String?,
    userdata: Bundle? = null
) {
    if (!addAccountExplicitly(account, password, userdata)) {
        setPassword(account, password)
        userdata?.run {
            for (key in keySet()) {
                setUserData(account, key, getString(key))
            }
        }
    }
}

/**
 * Returns an [Account] whose name and type match the given [name] and [type], or null if
 * no accounts match.
 */
fun AccountManager.getAccountByNameAndType(name: String?, type: String): Account? {
    getAccountsByType(type).forEach { account ->
        when (account.name) {
            name -> return account
        }
    }
    return null
}
