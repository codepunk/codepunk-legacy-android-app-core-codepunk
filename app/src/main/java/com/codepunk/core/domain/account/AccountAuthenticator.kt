/*
 * Copyright (C) 2018 Codepunk, LLC
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

package com.codepunk.core.domain.account

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.accounts.AccountManager.*
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import com.codepunk.core.BuildConfig
import com.codepunk.core.BuildConfig.CATEGORY_LOG_IN
import com.codepunk.core.BuildConfig.CATEGORY_REGISTER
import com.codepunk.core.BuildConfig.EXTRA_AUTH_TOKEN_TYPE
import com.codepunk.core.BuildConfig.EXTRA_USERNAME
import com.codepunk.core.R
import com.codepunk.core.data.remote.webservice.AuthWebservice
import com.codepunk.core.di.qualifier.ApplicationContext
import com.codepunk.core.domain.model.AuthTokenType
import com.codepunk.core.domain.model.AuthTokenType.DEFAULT
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [AbstractAccountAuthenticator] that authenticates Codepunk accounts.
 */
@Singleton
class AccountAuthenticator @Inject constructor(

    /**
     * The application [Context] associated with this account authenticator.
     */
    @ApplicationContext private val context: Context,

    /**
     * The Android [AccountManager].
     */
    private val accountManager: AccountManager,

    /**
     * The auth webservice.
     */
    private val authWebservice: AuthWebservice

) : AbstractAccountAuthenticator(context) {

    // region Inherited methods

    /**
     * Adds an account of the specified accountType.
     */
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
                Intent(BuildConfig.ACTION_AUTHENTICATION).apply {
                    addCategory(CATEGORY_REGISTER)
                    putExtra(KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
                    // TODO Anything else here?
                }
            )
        }
    }

    /**
     * Checks that the user knows the credentials of an account.
     */
    override fun confirmCredentials(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        options: Bundle?
    ): Bundle? {
        // TODO
        return null
    }

    /**
     * Returns a [Bundle] that contains the [Intent] of the activity that can be used to edit the
     * properties. In order to indicate success the activity should call
     * [AccountAuthenticatorResponse.onResult] with a non-null Bundle.
     */
    override fun editProperties(
        response: AccountAuthenticatorResponse?,
        accountType: String?
    ): Bundle? {
        // TODO
        return null
    }

    /**
     * Gets an auth token for an account. If not null, the resultant [Bundle] will contain
     * different sets of keys depending on whether a token was successfully issued and, if not,
     * whether one could be issued via some [Activity].
     */
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
                try {
                    val resp = authWebservice.refreshToken(refreshToken).execute()
                    when {
                        !resp.isSuccessful || resp.body() == null -> {
                            putInt(KEY_ERROR_CODE, ERROR_CODE_INVALID_RESPONSE)
                            putString(KEY_ERROR_MESSAGE, "Unable to openSession the account")
                        }
                        else -> resp.body()?.apply {
                            authTokenString = this.authToken
                            refreshToken = this.refreshToken
                            accountManager.setAuthToken(account, DEFAULT.value, authTokenString)
                            accountManager.setPassword(account, refreshToken)
                        }
                    }
                } catch (e: Exception) {
                    putInt(KEY_ERROR_CODE, ERROR_CODE_NETWORK_ERROR)
                    putString(KEY_ERROR_MESSAGE, e.message)
                }
            }

            when {
                containsKey(KEY_ERROR_CODE) -> {
                    // We've already specified an error so don't do anything else.
                }
                TextUtils.isEmpty(authTokenString) ->
                    // We were unable to get an auth token. We need the user to log in again.
                    putParcelable(
                        KEY_INTENT,
                        Intent(BuildConfig.ACTION_AUTHENTICATION).apply {
                            addCategory(CATEGORY_LOG_IN)
                            putExtra(KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
                            putExtra(EXTRA_USERNAME, account.name)
                            putExtra(EXTRA_AUTH_TOKEN_TYPE, authTokenType)
                        }
                    )
                else -> {
                    putString(KEY_ACCOUNT_NAME, account.name)
                    putString(KEY_ACCOUNT_TYPE, account.type)
                    putString(KEY_AUTHTOKEN, authTokenString)
                    putString(KEY_PASSWORD, refreshToken)
                }
            }

            // TODO Maybe get the user here so we can set it?
            Log.d("AccountAuthenticator", "")
        }
    }

    /**
     * Ask the authenticator for a localized label for the given authTokenType.
     */
    override fun getAuthTokenLabel(authTokenType: String?): String {
        return AuthTokenType.lookup(authTokenType)?.getFriendlyName(context)
            ?: context.getString(R.string.authenticator_token_type_unknown)
    }

    /**
     * Checks if the account supports all the specified authenticator specific features.
     */
    override fun hasFeatures(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        features: Array<out String>?
    ): Bundle? {
        // TODO
        return null
    }

    /**
     * Update the locally stored credentials for an account.
     */
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
