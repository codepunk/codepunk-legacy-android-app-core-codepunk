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
import com.codepunk.core.ui.account.*
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * The [Module] used for dependency injection into fragments used in [AccountActivity].
 */
@Module
interface AccountActivityFragmentBuildersModule {

    // region Methods

    /**
     * Contributes an AndroidInjector to [AuthenticatingFragment].
     */
    @FragmentScope
    @ContributesAndroidInjector /*(modules = [AuthenticatingFragmentModule::class])*/
    fun contributeAuthenticatingFragmentInjector(): AuthenticatingFragment

    /**
     * Contributes an AndroidInjector to [AuthenticationOptionsFragment].
     */
    @FragmentScope
    @ContributesAndroidInjector /*(modules = [AuthenticationOptionsFragmentModule::class])*/
    fun contributeAuthenticationOptionsFragmentInjector(): AuthenticationOptionsFragment

    /**
     * Contributes an AndroidInjector to [CreateAccountFragment].
     */
    @FragmentScope
    @ContributesAndroidInjector /*(modules = [CreateAccountFragmentModule::class])*/
    fun contributeCreateAccountFragmentInjector(): CreateAccountFragment

    /**
     * Contributes an AndroidInjector to [ForgotPasswordFragment].
     */
    @FragmentScope
    @ContributesAndroidInjector /*(modules = [ForgotPasswordFragmentModule::class])*/
    fun contributeForgotPasswordFragmentInjector(): ForgotPasswordFragment

    /**
     * Contributes an AndroidInjector to [LogInFragment].
     */
    @FragmentScope
    @ContributesAndroidInjector /*(modules = [LogInFragmentModule::class])*/
    fun contributeLogInFragmentInjector(): LogInFragment

    // endregion Methods

}
