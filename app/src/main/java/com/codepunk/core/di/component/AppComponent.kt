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

package com.codepunk.core.di.component

import com.codepunk.core.CodepunkApp
import com.codepunk.core.di.module.*
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

/**
 * A [Component] for dependency injection into the application.
 */
@Singleton
@Component(
    modules = [
        ActivityBuildersModule::class,
        AndroidSupportInjectionModule::class,
        AppModule::class,
        DataModule::class,
        NetModule::class,
        PersistenceModule::class,
        ViewModelModule::class,
        ServiceBuildersModule::class
    ]
)
interface AppComponent : AndroidInjector<CodepunkApp> {

    // region Methods

    /**
     * Returns a new [UserComponent.Builder] for building new
     * [UserComponent] instances.
     */
    fun userComponentBuilder(): UserComponent.Builder

    // endregion Methods

    // region Nested/inner classes

    /**
     * Helper class for creating an instance of [AppComponent].
     */
    @Component.Builder
    abstract class Builder : AndroidInjector.Builder<CodepunkApp>() {

        // region Methods

        /**
         * Builds an instance of type [AppComponent].
         */
        abstract override fun build(): AppComponent

        // endregion Methods

    }

    // endregion Nested/inner classes

}
