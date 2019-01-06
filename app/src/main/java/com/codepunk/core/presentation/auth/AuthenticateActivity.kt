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

package com.codepunk.core.presentation.auth

import android.accounts.Account
import android.accounts.AccountManager
import android.accounts.AccountManager.*
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import com.codepunk.core.BuildConfig.*
import com.codepunk.core.R
import com.codepunk.core.data.remote.entity.auth.RemoteAuthorization
import com.codepunk.core.data.remote.entity.http.RemoteMessage
import com.codepunk.core.databinding.ActivityAuthenticateBinding
import com.codepunk.core.lib.AccountAuthenticatorAppCompatActivity
import com.codepunk.core.lib.addOrUpdateAccount
import com.codepunk.core.util.addDefaultArgumentsFromBundle
import com.codepunk.doofenschmirtz.util.http.HttpStatus
import com.codepunk.doofenschmirtz.util.loginator.FormattingLoginator
import com.codepunk.doofenschmirtz.util.taskinator.DataUpdate
import com.codepunk.doofenschmirtz.util.taskinator.FailureUpdate
import com.codepunk.doofenschmirtz.util.taskinator.ProgressUpdate
import com.codepunk.doofenschmirtz.util.taskinator.SuccessUpdate
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import retrofit2.Response
import javax.inject.Inject

/**
 * An Activity that handles all actions relating to creating, selecting, and authenticating
 * an account.
 */
class AuthenticateActivity :
    AccountAuthenticatorAppCompatActivity(),
    HasSupportFragmentInjector {

    // region Properties

    /**
     * Performs dependency injection on fragments.
     */
    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    /**
     * The application [FormattingLoginator].
     */
    @Inject
    lateinit var loginator: FormattingLoginator

    /**
     * The Android account manager.
     */
    @Inject
    lateinit var accountManager: AccountManager

    /**
     * The application [SharedPreferences].
     */
    @Inject
    lateinit var sharedPreferences: SharedPreferences

    /**
     * A [ViewModelProvider.Factory] for creating [ViewModel] instances.
     */
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    /**
     * An instance of [AuthViewModel] for managing account-related data.
     */
    private val authViewModel: AuthViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(AuthViewModel::class.java)
    }

    /**
     * The binding for this activity.
     */
    private lateinit var binding: ActivityAuthenticateBinding

    /**
     * The navigation controller for the activity.
     */
    private val navController: NavController by lazy {
        Navigation.findNavController(this, R.id.authenticate_nav_fragment).apply {
            addOnDestinationChangedListener { _, destination, _ ->
                title = destination.label
            }
        }
    }

    private val navHostFragment: NavHostFragment by lazy {
        supportFragmentManager.findFragmentById(R.id.authenticate_nav_fragment) as NavHostFragment
    }

    // endregion Properties

    // region Lifecycle methods

    /**
     * Creates the content view and pulls values from the passed [Intent].
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_authenticate)

        // Set the navigation start destination
        val navInflater = navHostFragment.navController.navInflater
        val navGraph = navInflater.inflate(R.navigation.navigation_authenticate)
        navGraph.startDestination = when {
            intent.categories.contains(CATEGORY_CREATE_ACCOUNT) -> R.id.fragment_create_account
            intent.categories.contains(CATEGORY_LOG_IN) -> R.id.fragment_log_in
            else -> R.id.fragment_authenticate
        }

        navGraph.findNode(navGraph.startDestination)?.addDefaultArgumentsFromBundle(intent?.extras)
        navHostFragment.navController.graph = navGraph

        authViewModel.authorizationDataUpdate.observe(this, Observer { onAuthorizationUpdate(it) })
    }

    // endregion Lifecycle methods

    // region Inherited methods

    /**
     * Reacts to a new intent.
     */
    @Suppress("UNUSED")
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.categories?.apply {
            when {
                contains(CATEGORY_CREATE_ACCOUNT) ->
                    navController.navigate(R.id.action_auth_to_create_account, intent.extras)
                contains(CATEGORY_LOG_IN) ->
                    navController.navigate(R.id.action_auth_to_log_in, intent.extras)
            }
        }
    }

    /**
     * Reacts to the "up" button being pressed.
     */
    override fun onSupportNavigateUp(): Boolean {
        return Navigation.findNavController(this, R.id.authenticate_nav_fragment).navigateUp()
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

    /**
     * Reacts to authorization data changing.
     */
    private fun onAuthorizationUpdate(update: DataUpdate<RemoteMessage, Response<RemoteAuthorization>>) {
        when (update) {
            is ProgressUpdate -> {
                // TODO Loading dialog (show and hide)
            }
            is SuccessUpdate -> {
                val result = update.data
                when (result) {
                    null -> setResult(RESULT_CANCELED)
                    else -> {
                        // Set accountAuthenticatorResult so accountAuthenticatorResponse can
                        // react to it
                        accountAuthenticatorResult = update.data

                        // Update accountManager with the account
                        val accountName = result.getString(KEY_ACCOUNT_NAME)
                        val accountType = result.getString(KEY_ACCOUNT_TYPE)
                        val password = result.getString(KEY_PASSWORD)
                        val account = Account(accountName, accountType)
                        accountManager.addOrUpdateAccount(account, password)

                        sharedPreferences.edit()
                            .putString(PREF_KEY_CURRENT_ACCOUNT_NAME, accountName)
                            .apply()

                        // Set the result to pass back to the calling activity
                        val data = Intent()
                        data.putExtra(KEY_ACCOUNT, account)
                        setResult(RESULT_OK, data)
                    }
                }
                finish()
            }
            is FailureUpdate -> {

                // TODO Error message(s)

            }
        }

        val response = when (update) {
            is SuccessUpdate -> update.result
            is FailureUpdate -> update.result
            else -> null
        }
        val httpStatus = when (response) {
            null -> null
            else -> HttpStatus.lookup(response.code())
        }
        loginator.d("httpStatus=$httpStatus, update=$update")
    }

    // endregion Methods

}
