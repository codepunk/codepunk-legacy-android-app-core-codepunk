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

/**
 * Returns an array of [Account]s of any type that match the given [name], or an empty array
 * if no accounts match.
 */
fun AccountManager.getAccountsByName(name: String): Array<Account> {
    val accounts: ArrayList<Account> = ArrayList()
    getAccounts().forEach { account ->
        when (account.name) {
            name -> accounts.add(account)
        }
    }
    return accounts.toTypedArray()
}

/**
 * Returns an [Account] whose name and type match the given [name] and [type], or null if
 * no accounts match.
 */
fun AccountManager.getAccountByNameAndType(name: String, type: String): Account? {
    getAccountsByType(type).forEach { account ->
        when (account.name) {
            name -> return account
        }
    }
    return null
}
