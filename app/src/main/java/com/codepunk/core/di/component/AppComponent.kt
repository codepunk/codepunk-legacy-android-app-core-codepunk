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
import com.codepunk.core.di.module.ActivityBuildersModule
import com.codepunk.core.di.module.AppModule
import com.codepunk.core.di.module.NetModule
import com.codepunk.core.di.module.ViewModelModule
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
    NetModule::class,
    ViewModelModule::class,
    ActivityBuildersModule::class])
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
