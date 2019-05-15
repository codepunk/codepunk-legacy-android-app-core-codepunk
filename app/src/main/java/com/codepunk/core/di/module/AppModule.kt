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

package com.codepunk.core.di.module

import android.accounts.AccountManager
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.codepunk.core.BuildConfig
import com.codepunk.core.CodepunkApp
import com.codepunk.core.R
import com.codepunk.core.di.component.UserComponent
import com.codepunk.core.di.qualifier.ApplicationContext
import com.codepunk.doofenschmirtz.util.Translatinator
import com.codepunk.doofenschmirtz.util.loginator.FormattingLoginator
import com.codepunk.doofenschmirtz.util.loginator.LogcatLoginator
import com.codepunk.doofenschmirtz.util.loginator.Loginator
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * A [Module] for injecting application-level dependencies.
 */
@Module(
    subcomponents = [
        UserComponent::class
    ]
)
object AppModule {

    // region Methods

    /**
     * Provides the application-level [Context].
     */
    @JvmStatic
    @Provides
    @Singleton
    @ApplicationContext
    fun providesContext(app: CodepunkApp): Context = app

    /**
     * Provides the application-level [Loginator].
     */
    @JvmStatic
    @Provides
    @Singleton
    fun providesLoginator(): FormattingLoginator = FormattingLoginator(LogcatLoginator()).apply {
        level = BuildConfig.LOG_LEVEL
    }

    /**
     * Provides an [AccountManager] instance for providing access to a centralized registry of
     * the user's online accounts.
     */
    @JvmStatic
    @Provides
    @Singleton
    fun providesAccountManager(@ApplicationContext context: Context): AccountManager =
        AccountManager.get(context)

    /**
     * Provides the default [SharedPreferences] for the app.
     */
    @JvmStatic
    @Provides
    @Singleton
    fun providesSharedPreferences(app: CodepunkApp): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(app)

    /**
     * Provides an instance of [Translatinator] for translating messages from the network.
     */
    @JvmStatic
    @Provides
    @Singleton
    fun providesNetworkTranslatinator(@ApplicationContext context: Context): Translatinator {
        val patternInputs: Array<Int> = arrayOf(
            R.string.trans_regex_input_confirmed,
            R.string.trans_regex_input_email,
            R.string.trans_regex_input_min,
            R.string.trans_regex_input_max,
            R.string.trans_regex_input_regex,
            R.string.trans_regex_input_required,
            R.string.trans_regex_input_string,
            R.string.trans_regex_input_unique,
            R.string.trans_regex_input_username_regex
        )
        val patternOutputs: Array<Int> = arrayOf(
            R.string.trans_regex_output_confirmed,
            R.string.trans_regex_output_email,
            R.string.trans_regex_output_min,
            R.string.trans_regex_output_max,
            R.string.trans_regex_output_regex,
            R.string.trans_regex_output_required,
            R.string.trans_regex_output_string,
            R.string.trans_regex_output_unique,
            R.string.trans_regex_output_username_regex
        )
        val stringInputs: Array<Int> = arrayOf(
            R.string.trans_input_email,
            R.string.trans_input_invalid,
            R.string.trans_input_password,
            R.string.trans_input_sent_activation_code,
            R.string.trans_input_sent_activation_code_when_you_registered,
            R.string.trans_input_username
        )
        val stringOutputs: Array<Int> = arrayOf(
            R.string.trans_output_email,
            R.string.trans_output_invalid,
            R.string.trans_output_password,
            R.string.trans_output_sent_activation_code,
            R.string.trans_output_sent_activation_code_when_you_registered,
            R.string.trans_output_username
        )
        val builder = Translatinator.Builder(context)
            .debug(false)
        patternInputs.forEachIndexed { index, inRegexResId ->
            builder.mapRegEx(inRegexResId, patternOutputs[index])
        }
        stringInputs.forEachIndexed { index, inResId ->
            builder.map(inResId, stringOutputs[index])
        }
        return builder.build()
    }

    // endregion Methods

}
