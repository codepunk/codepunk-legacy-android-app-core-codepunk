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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.codepunk.core.di.key.ViewModelKey
import com.codepunk.core.ui.account.AccountViewModel
import com.codepunk.core.util.InjectingViewModelFactory
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * A [Module] for injecting [ViewModel] instances and the [ViewModelProvider.Factory] that will
 * be used to create them.
 */
@Suppress("UNUSED")
@Module
interface ViewModelModule {

    /**
     * Binds an instance of [AccountViewModel] to this [Module] for dependency injection.
     */
    @Binds
    @IntoMap
    @ViewModelKey(AccountViewModel::class)
    fun bindRepoViewModel(repoViewModel: AccountViewModel): ViewModel

    /**
     * Binds an instance of [InjectingViewModelFactory] to this [Module] for dependency injection.
     */
    // TODO Should this be @Singleton? Why or why not? Probably not because we WANT this factory
    // to be created separately for each Activity etc.?
    @Binds
    fun bindViewModelFactory(factory: InjectingViewModelFactory): ViewModelProvider.Factory

}
