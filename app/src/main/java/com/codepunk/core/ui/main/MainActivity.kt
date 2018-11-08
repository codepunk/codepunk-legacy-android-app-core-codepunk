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

/*
 * TODO NEXT:
 * Clean EVERY. SINGLE. New file just added. Clear those warnings!
 * Add DI to all new Settings screens/logic
 * Add "Logout" to Main Settings screen
 */

package com.codepunk.core.ui.main

import android.accounts.Account
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.codepunk.core.BuildConfig.ACTION_SETTINGS
import com.codepunk.core.BuildConfig.KEY_ACCOUNT
import com.codepunk.core.R
import com.codepunk.core.di.scope.ActivityScope
import com.codepunk.core.ui.auth.AuthViewModel
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

const val ACCOUNT_REQUIRED_REQUEST_CODE = 1

/**
 * The main [Activity] for the Codepunk app.
 */
@ActivityScope
class MainActivity :
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
     * An instance of [AuthViewModel] for managing account-related data.
     */
    private val mainViewModel: MainViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(MainViewModel::class.java)
    }

    // endregion Properties

    // region Lifecycle methods

    /**
     * Sets the content view for the activity.
     */
    @Suppress("UNUSED")
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    // endregion Lifecycle methods

    // region Inherited methods

    /**
     * Called when an [Activity] launched with [Activity.startActivityForResult] exits.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            ACCOUNT_REQUIRED_REQUEST_CODE -> when (resultCode) {
                RESULT_OK -> {
                    val account: Account? = data?.getParcelableExtra(KEY_ACCOUNT)
                    when (account) {
                        null -> finish() // TODO Show error message then finish? OR show the msg in authenticate activity dismiss?
                        else -> mainViewModel.authenticate()
                    }
                }
                RESULT_CANCELED -> finish()
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    /**
     * Creates the main options menu.
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    /**
     * Handles the various menu options.
     */
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.menu_settings -> {
                startActivity(Intent(ACTION_SETTINGS))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // endregion Inherited methods

    // region Implemented methods

    /**
     * Implementation of [HasSupportFragmentInjector]. Returns a
     * [DispatchingAndroidInjector] that injects dependencies into fragments.
     */
    override fun supportFragmentInjector(): AndroidInjector<Fragment> =
        fragmentDispatchingAndroidInjector

    // endregion Implemented methods

}
