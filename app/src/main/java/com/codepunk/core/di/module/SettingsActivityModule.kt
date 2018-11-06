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

import com.codepunk.core.di.scope.FragmentScope
import com.codepunk.core.ui.settings.DeveloperOptionsSettingsFragment
import com.codepunk.core.ui.settings.MainSettingsFragment
import com.codepunk.core.ui.settings.SettingsActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

/*
 * Author(s): Scott Slater
 */

/**
 * A [Module] for injecting dependencies into [SettingsActivity].
 */
@Module
abstract class SettingsActivityModule {

    // region Methods

    /**
     * Contributes an AndroidInjector to [MainSettingsFragment].
     */
    @FragmentScope
    @ContributesAndroidInjector //(modules = [MainSettingsFragmentModule::class])
    abstract fun contributeMainSettingsFragmentInjector(): MainSettingsFragment

    /**
     * Contributes an AndroidInjector to [DeveloperOptionsSettingsFragment].
     */
    @FragmentScope
    @ContributesAndroidInjector //(modules = [DeveloperOptionsSettingsFragmentFragmentModule::class])
    abstract fun contributeDeveloperOptionsSettingsFragmentInjector():
            DeveloperOptionsSettingsFragment

    // endregion Methods

}
