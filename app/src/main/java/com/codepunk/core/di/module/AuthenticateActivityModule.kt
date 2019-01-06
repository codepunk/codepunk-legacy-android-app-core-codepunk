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
import com.codepunk.core.presentation.auth.*
import com.codepunk.core.presentation.auth.AuthenticateFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * A [Module] for injecting dependencies into [AuthenticateActivity].
 */
@Module
abstract class AuthenticateActivityModule {

    // region Methods

    /**
     * Contributes an AndroidInjector to [AuthenticateFragment].
     */
    @FragmentScope
    @ContributesAndroidInjector //(modules = [AuthenticateFragmentModule::class])
    abstract fun contributeAuthenticateFragmentInjector(): AuthenticateFragment

    /**
     * Contributes an AndroidInjector to [CreateAccountFragment].
     */
    @FragmentScope
    @ContributesAndroidInjector //(modules = [CreateAccountFragmentModule::class])
    abstract fun contributeCreateAccountFragmentInjector(): CreateAccountFragment

    /**
     * Contributes an AndroidInjector to [ForgotPasswordFragment].
     */
    @FragmentScope
    @ContributesAndroidInjector //(modules = [ForgotPasswordFragmentModule::class])
    abstract fun contributeForgotPasswordFragmentInjector(): ForgotPasswordFragment

    /**
     * Contributes an AndroidInjector to [LogInFragment].
     */
    @FragmentScope
    @ContributesAndroidInjector //(modules = [LogInFragmentModule::class])
    abstract fun contributeLogInFragmentInjector(): LogInFragment

    // endregion Methods

    // region Companion object

    @Module
    companion object {

        // region Methods

        /*
        @JvmStatic
        @Provides
        @ActivityScope
        fun providesSomething(): String = "Hello"
        */

        // endregion Methods
    }

    // endregion Companion object

}
