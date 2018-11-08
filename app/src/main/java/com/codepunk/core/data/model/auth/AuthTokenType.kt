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

package com.codepunk.core.data.model.auth

import android.accounts.AccountManager
import android.content.Context
import android.content.res.Resources
import androidx.annotation.StringRes
import com.codepunk.core.R
import com.codepunk.core.data.remote.webservice.AuthWebservice

/**
 * Enum class representing authorization token types in accounts managed by Android.
 */
enum class AuthTokenType(

    /**
     * The value associated with this authorization token type.
     */
    val value: String,

    /**
     * A string resource ID pointing to a user-friendly name for the authorization token type.
     */
    @StringRes val resId: Int

) {

    // region Values

    /**
     * The default authorization token type.
     */
    DEFAULT("default", R.string.authenticator_token_type_default);

    // endregion Values

    // region Methods

    /**
     * Returns a user-friendly (readable) name for this authentication token type using the
     * supplied [resources].
     */
    @Suppress("UNUSED")
    fun getFriendlyName(resources: Resources): String {
        return resources.getString(resId)
    }

    /**
     * Returns a user-friendly (readable) name for this authentication token type using the
     * supplied [context].
     */
    fun getFriendlyName(context: Context): String {
        return context.getString(resId)
    }

    // endregion Methods

    // region Companion object

    companion object {

        /**
         * A lookup map of labels to authorization token types.
         */
        private val lookupMap by lazy {
            HashMap<String, AuthTokenType>(values().size).apply {
                for (type in values()) {
                    put(type.value, type)
                }
            }
        }

        /**
         * Returns the authorization token type associated with the given [value], or
         * [defaultValue] if no such type is found.
         */
        fun lookup(
            value: String?,
            defaultValue: AuthTokenType? = null
        ): AuthTokenType? {
            return lookupMap[value] ?: defaultValue
        }

    }

    // endregion Companion object
}
