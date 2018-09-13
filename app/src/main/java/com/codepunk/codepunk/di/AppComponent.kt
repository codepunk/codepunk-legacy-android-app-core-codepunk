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

import com.codepunk.codepunk.CodepunkApp
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

/**
 * [Component] for dependency injection into the application.
 */
@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {

    // region Methods

    /**
     * Injects dependencies into the application (i.e. an [instance] of [CodepunkApp]).
     */
    fun inject(instance: CodepunkApp)

    /**
     * Returns a new [MainActivityComponent.Builder] for building new
     * [MainActivityComponent] instances.
     */
    fun mainActivityComponentBuilder(): MainActivityComponent.Builder

    // endregion Methods

    // region Nested/inner classes

    /**
     * Helper class for creating an instance of [AppComponent].
     */
    @Singleton
    @Component.Builder
    interface Builder {

        // region Methods

        /**
         * Binds an [instance] of [CodepunkApp] to the component.
         */
        @BindsInstance
        fun application(instance: CodepunkApp): Builder

        /**
         * Builds an instance of [AppComponent].
         */
        fun build(): AppComponent

        // endregion Methods

    }

    // endregion Nested/inner classes

}