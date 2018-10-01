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

package com.codepunk.codepunk.di.component

import com.codepunk.codepunk.CodepunkApp
import com.codepunk.codepunk.di.module.ActivityBuilderModule
import com.codepunk.codepunk.di.module.AppModule
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

/**
 * A [Component] for dependency injection into the application.
 */
@Singleton
@Component(modules = [
    AndroidSupportInjectionModule::class,
    AppModule::class,
    ActivityBuilderModule::class])
interface AppComponent : AndroidInjector<CodepunkApp> {

    // region Nested/inner classes

    /**
     * Helper class for creating an instance of [AppComponent].
     */
    @Singleton
    @Component.Builder
    abstract class Builder : AndroidInjector.Builder<CodepunkApp>()

    // endregion Nested/inner classes

}
