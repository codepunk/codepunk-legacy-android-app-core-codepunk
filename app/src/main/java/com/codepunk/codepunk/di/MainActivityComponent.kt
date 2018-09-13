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

package com.codepunk.codepunk.di

import com.codepunk.codepunk.ui.main.MainActivity
import dagger.BindsInstance
import dagger.Subcomponent

/**
 * A [Subcomponent] used for dependency injection into [MainActivity].
 */
@ActivityScope
@Subcomponent(modules = [MainActivityModule::class])
interface MainActivityComponent {

    // region Methods

    /**
     * Injects dependencies into an [instance] of [MainActivity].
     */
    fun inject(instance: MainActivity)

    /**
     * Returns a new [MainFragmentComponent.Builder] for building new
     * [MainFragmentComponent] instances.
     */
    fun mainFragmentComponentBuilder(): MainFragmentComponent.Builder

    // endregion Methods

    // region Nested/inner classes

    /**
     * Helper class for creating an instance of [MainActivityComponent].
     */
    @Subcomponent.Builder
    interface Builder {

        // region Methods

        /**
         * Binds an [instance] of [MainActivity] to the subcomponent.
         */
        @BindsInstance
        fun activity(instance: MainActivity): Builder

        /**
         * Builds the main activity subcomponent.
         */
        fun build(): MainActivityComponent

        // endregion Methods

    }

    // endregion Nested/inner classes

}
