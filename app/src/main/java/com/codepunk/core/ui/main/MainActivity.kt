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
import android.accounts.AccountManager
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.codepunk.core.BuildConfig.*
import com.codepunk.core.R
import com.codepunk.core.data.model.auth.AuthTokenType
import com.codepunk.core.user.getAccountByNameAndType
import com.codepunk.core.di.scope.ActivityScope
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

private const val ACCOUNT_REQUIRED_REQUEST_CODE = 1

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
     * The application [SharedPreferences].
     */
    @Inject
    lateinit var sharedPreferences: SharedPreferences

    /**
     * The Android account manage.
     */
    @Inject
    lateinit var accountManager: AccountManager

    // endregion Properties

    // region Lifecycle methods

    /**
     * Sets the content view for the activity.
     */
    @Suppress("UNUSED")
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        when (savedInstanceState) {
            null -> {

                // TODO Maybe break this out? Extension function on AccountManager?

                val name: String? = sharedPreferences.getString(PREF_KEY_CURRENT_ACCOUNT_TYPE, null)
                val account: Account? = when (name) {
                    null -> null
                    else -> {
                        val type: String = sharedPreferences.getString(
                            PREF_KEY_CURRENT_ACCOUNT_TYPE,
                            null
                        ) ?: AUTHENTICATOR_ACCOUNT_TYPE
                        accountManager.getAccountByNameAndType(name, type)
                    }
                } ?: let {
                    // There was no "saved" account.
                    // If there's only one account in the AccountManager, choose it
                    val accounts = accountManager.getAccountsByType(AUTHENTICATOR_ACCOUNT_TYPE)
                    when (accounts.size) {
                        1 -> accounts[0]
                        else -> null
                    }
                }

                when (account) {
                    null -> {
                        // We didn't have a saved account
                        // If there's more than one, open AuthenticateActivity with chooser
                        startActivityForResult(
                            Intent(ACTION_AUTHORIZATION),
                            ACCOUNT_REQUIRED_REQUEST_CODE
                        )
                    }
                    else -> {
                        // We had a saved account
                        Log.d("MainActivity", "")
                        accountManager.getAuthToken(
                            account,
                            AuthTokenType.DEFAULT.value,
                            null,
                            this,
                            { future ->
                                val bundle = future?.result
                                Log.d("MainActivity", "onCreate: bundle=$bundle")
                            },
                            null
                        )
                    }
                }
            }
        }

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
                        null -> { /* TODO Respond to null account */
                        }
                        else -> onAccount(account)
                    }
                }
                RESULT_CANCELED -> Log.d("MainActivity", "resultCode=RESULT_CANCELED")
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

    // region Methods

    private fun onAccount(account: Account) {
        with(account) {
            Log.d("MainActivity", "account=$name, type=$type")
        }
    }

    // endregion Methods

}
