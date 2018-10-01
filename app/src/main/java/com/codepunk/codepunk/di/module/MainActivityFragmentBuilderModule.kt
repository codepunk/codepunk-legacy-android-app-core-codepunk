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

import com.codepunk.codepunk.di.scope.FragmentScope
import com.codepunk.codepunk.di.component.MainFragmentComponent
import com.codepunk.codepunk.ui.main.MainActivity
import com.codepunk.codepunk.ui.main.MainFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * The [Module] used for dependency injection into fragments used in [MainActivity].
 */
@Module(subcomponents = [MainFragmentComponent::class])
abstract class MainActivityFragmentBuilderModule {

    // region Methods

    /**
     * Generates an AndroidInjector for [MainFragment].
     */
    @FragmentScope
    @ContributesAndroidInjector(modules = [MainFragmentModule::class])
    abstract fun contributeMainFragmentInjector(): MainFragment

    // endregion Methods

}
