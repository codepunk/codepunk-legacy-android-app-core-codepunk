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

package com.codepunk.codepunk.di.module

import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.codepunk.codepunk.CodepunkApp
import com.codepunk.codepunk.di.component.MainActivityComponent
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * A [Module] for injecting application-level dependencies.
 */
@Module(subcomponents = [MainActivityComponent::class])
object AppModule {

    // region Methods

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
