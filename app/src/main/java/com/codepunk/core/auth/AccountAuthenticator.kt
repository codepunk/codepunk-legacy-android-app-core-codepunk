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

package com.codepunk.core.auth

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.accounts.AccountManager.*
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import com.codepunk.core.BuildConfig
import com.codepunk.core.R
import com.codepunk.core.data.model.auth.AuthTokenType
import com.codepunk.core.data.model.auth.AuthTokenType.DEFAULT
import com.codepunk.core.data.remote.webservice.AuthWebservice
import com.codepunk.core.di.qualifier.ApplicationContext
import com.codepunk.core.util.EXTRA_AUTHENTICATOR_INITIAL_ACTION
import com.codepunk.core.util.EXTRA_AUTH_TOKEN_TYPE
import com.codepunk.core.util.EXTRA_USERNAME
import javax.inject.Inject

// TODO Add documentation

/**
 * Implementation of [AbstractAccountAuthenticator] that authenticates Codepunk accounts.
 */
class AccountAuthenticator @Inject constructor(

    /**
     * The application [Context] associated with this account authenticator.
     */
    @ApplicationContext private val context: Context,

    /**
     * TODO
     */
    private val accountManager: AccountManager, // Note that even though we're not using context
    // to get the accountManager instance, it should be the same since it's @ApplicationContext.

    /**
     * TODO
     */
    private val authWebservice: AuthWebservice

) : AbstractAccountAuthenticator(context) {

    // region Inherited methods

    override fun addAccount(
        response: AccountAuthenticatorResponse?,
        accountType: String?,
        authTokenType: String?,
        requiredFeatures: Array<out String>?,
        options: Bundle?
    ): Bundle? {
        return Bundle().apply {
            putParcelable(
                KEY_INTENT,
                Intent(BuildConfig.ACTION_ACCOUNT).apply {
                    putExtra(KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
                    putExtra(
                        EXTRA_AUTHENTICATOR_INITIAL_ACTION,
                        R.id.action_authenticate_to_create_account
                    )
                    // TODO Anything else here?
                }
            )
        }
    }

    override fun confirmCredentials(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        options: Bundle?
    ): Bundle? {
        // TODO
        return null
    }

    override fun editProperties(
        response: AccountAuthenticatorResponse?,
        accountType: String?
    ): Bundle? {
        // TODO
        return null
    }

    override fun getAuthToken(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        authTokenType: String?,
        options: Bundle?
    ): Bundle? {
        // TODO Do I need to check this? Can I make it not null?
        if (account == null) {
            return null
        }

        return Bundle().apply {
            var authTokenString =
                accountManager.peekAuthToken(account, AuthTokenType.DEFAULT.value)

            // TODO Check when auth token expires?

            // TODO Is this common? Or should I add refresh token as user data?
            var refreshToken = accountManager.getPassword(account)

            if (TextUtils.isEmpty(authTokenString) && !TextUtils.isEmpty(refreshToken)) {
                val resp = authWebservice.refreshToken(refreshToken).execute()
                when {
                    !resp.isSuccessful || resp.body() == null -> {
                        // TODO Handle error here
                    }
                    else -> resp.body()?.apply {
                        authTokenString = this.accessToken
                        refreshToken = this.refreshToken
                        accountManager.setAuthToken(account, DEFAULT.value, authTokenString)
                        accountManager.setPassword(account, refreshToken)
                    }
                }
            }

            if (TextUtils.isEmpty(authTokenString)) {
                // We were unable to get an auth token. We need the user to log in again.
                putParcelable(
                    KEY_INTENT,
                    Intent(BuildConfig.ACTION_ACCOUNT).apply {
                        putExtra(KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
                        putExtra(
                            EXTRA_AUTHENTICATOR_INITIAL_ACTION,
                            R.id.action_authenticate_to_log_in
                        )
                        putExtra(EXTRA_USERNAME, account.name)
                        putExtra(EXTRA_AUTH_TOKEN_TYPE, authTokenType)
                    }
                )
            } else {
                putString(KEY_ACCOUNT_NAME, account.name)
                putString(KEY_ACCOUNT_TYPE, account.type)
                putString(KEY_AUTHTOKEN, authTokenString)
                putString(KEY_PASSWORD, refreshToken)
            }
        }
    }

    override fun getAuthTokenLabel(authTokenType: String?): String {
        return AuthTokenType.lookup(authTokenType)?.getFriendlyName(context)
            ?: context.getString(R.string.authenticator_token_type_unknown)
    }

    override fun hasFeatures(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        features: Array<out String>?
    ): Bundle? {
        // TODO
        return null
    }

    override fun updateCredentials(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        authTokenType: String?,
        options: Bundle?
    ): Bundle? {
        // TODO
        return null
    }

    // endregion Inherited methods

}
