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
import com.codepunk.core.CodepunkApp
import com.codepunk.core.data.remote.webservice.UserWebservice
import com.codepunk.core.di.component.UserComponent
import com.codepunk.core.di.qualifier.ApplicationContext
import com.codepunk.core.session.SessionManager
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
    fun providesLoginator(): FormattingLoginator = FormattingLoginator(LogcatLoginator())

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
     * Provides a [SessionManager] instance for managing a user session.
     */
    @JvmStatic
    @Provides
    @Singleton
    fun providesSessionManager(
        accountManager: AccountManager,
        sharedPreferences: SharedPreferences,
        userWebservice: UserWebservice,
        userComponentBuilder: UserComponent.Builder
    ): SessionManager =
        SessionManager(
            accountManager,
            sharedPreferences,
            userWebservice,
            userComponentBuilder
        )

    /**
     * Provides the default [SharedPreferences] for the app.
     */
    @JvmStatic
    @Provides
    @Singleton
    fun providesSharedPreferences(app: CodepunkApp): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(app)

    // endregion Methods

}
