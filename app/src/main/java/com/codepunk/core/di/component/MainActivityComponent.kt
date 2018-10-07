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

import com.codepunk.core.di.module.MainActivityModule
import com.codepunk.core.di.scope.ActivityScope
import com.codepunk.core.ui.main.MainActivity
import dagger.Subcomponent
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule

/**
 * A [Subcomponent] used for dependency injection into [MainActivity].
 */
@ActivityScope
@Subcomponent(modules = [
    AndroidSupportInjectionModule::class,
    MainActivityModule::class])
interface MainActivityComponent : AndroidInjector<MainActivity> {

    // region Nested/inner classes

    /**
     * Helper class for creating an instance of [MainActivityComponent].
     */
    @Suppress("unused")
    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<MainActivity>()

    // endregion Nested/inner classes

}
