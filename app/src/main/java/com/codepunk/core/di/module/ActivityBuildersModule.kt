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

import com.codepunk.core.di.scope.ActivityScope
import com.codepunk.core.presentation.auth.AuthenticateActivity
import com.codepunk.core.presentation.main.MainActivity
import com.codepunk.core.presentation.settings.SettingsActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * The [Module] used for dependency injection into all activities in the app.
 */
@Module
interface ActivityBuildersModule {

    // region Methods

    /**
     * Contributes an Android injector to [MainActivity].
     */
    @ActivityScope
    @ContributesAndroidInjector(modules = [MainActivityModule::class])
    fun contributeMainActivityInjector(): MainActivity

    /**
     * Contributes an Android injector to [AuthenticateActivity].
     */
    @ActivityScope
    @ContributesAndroidInjector(modules = [AuthenticateActivityModule::class])
    fun contributeAuthenticateActivityInjector(): AuthenticateActivity

    /**
     * Contributes an Android injector to [SettingsActivity].
     */
    @ActivityScope
    @ContributesAndroidInjector(modules = [SettingsActivityModule::class])
    fun contributesSettingsActivityInjector(): SettingsActivity

    // endregion Methods

}
