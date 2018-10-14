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

package com.codepunk.core.ui.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.codepunk.core.R
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

/**
 * An Activity that handles all actions relating to creating, selecting, and authenticating
 * an account.
 */
class AccountActivity :
    AppCompatActivity(),
    HasSupportFragmentInjector {

    // region Properties

    /**
     * Performs dependency injection on fragments.
     */
    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    /**
     * A [ViewModelProvider.Factory] for creating [ViewModel] instances.
     */
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    /**
     * An instance of [AccountViewModel] for managing account-related data.
     */
    @Suppress("UNUSED")
    private val accountViewModel: AccountViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(AccountViewModel::class.java)
    }

    // endregion Properties

    // region Lifecycle methods

    /**
     * Creates the content view.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)
    }

    // endregion Lifecycle methods

    // region Implemented methods

    /**
     * Implementation of [HasSupportFragmentInjector]. Returns a
     * [DispatchingAndroidInjector] that injects dependencies into fragments.
     */
    override fun supportFragmentInjector(): AndroidInjector<Fragment> =
        fragmentDispatchingAndroidInjector

    // endregion Implemented methods

}
