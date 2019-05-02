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
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.ContentLoadingProgressBar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import com.codepunk.core.BuildConfig.*
import com.codepunk.core.R
import com.codepunk.core.databinding.ActivityAuthenticateBinding
import com.codepunk.core.domain.model.Authentication
import com.codepunk.core.lib.AccountAuthenticatorAppCompatActivity
import com.codepunk.core.lib.addOrUpdateAccount
import com.codepunk.core.presentation.auth.LogInFragment.AuthenticationListener
import com.codepunk.core.presentation.base.ContentLoadingProgressBarOwner
import com.codepunk.core.presentation.base.FloatingActionButtonOwner
import com.codepunk.doofenschmirtz.util.loginator.FormattingLoginator
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

/**
 * An Activity that handles all actions relating to creating, selecting, and authenticating
 * an account.
 */
class AuthenticateActivity :
    AccountAuthenticatorAppCompatActivity(),
    HasSupportFragmentInjector,
    ContentLoadingProgressBarOwner,
    FloatingActionButtonOwner,
    AuthenticationListener {

    // region Implemented properties

    /**
     * Implementation of [ContentLoadingProgressBarOwner]. Returns the content loading progress bar.
     */
    override val contentLoadingProgressBar: ContentLoadingProgressBar by lazy {
        binding.loadingProgress
    }

    /**
     * Implementation of [FloatingActionButtonOwner]. Returns the floating action button.
     */
    override val floatingActionButton: FloatingActionButton by lazy {
        binding.fab
    }

    // endregion Implemented properties

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
     * The binding for this activity.
     */
    private lateinit var binding: ActivityAuthenticateBinding

    /**
     * The [NavController] for the activity.
     */
    private val navController: NavController by lazy {
        Navigation.findNavController(this, R.id.authenticate_nav_fragment).apply {
            addOnDestinationChangedListener { _, destination, _ ->
                title = destination.label
            }
        }
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

        // Prevent soft keyboard from auto-popping up
        //window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

        setSupportActionBar(binding.toolbar)

        if (savedInstanceState == null) {
            // If the supplied intent specifies it, navigate to an alternate destination
            // and pop up inclusive to that new destination
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.fragment_authenticate, true)
                .build()
            val navController =
                Navigation.findNavController(this, R.id.authenticate_nav_fragment)
            when {
                intent.categories.contains(CATEGORY_REGISTER) -> navController.navigate(
                    R.id.action_auth_to_register,
                    intent.extras,
                    navOptions
                )
                intent.categories.contains(CATEGORY_LOG_IN) -> navController.navigate(
                    R.id.action_auth_to_log_in,
                    intent.extras,
                    navOptions
                )
            }
        }
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
                contains(CATEGORY_REGISTER) ->
                    navController.navigate(R.id.action_auth_to_register, intent.extras)
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

    /**
     * Implementation of [AuthenticationListener]. Reacts to a successful authentication.
     */
    override fun onAuthenticated(authentication: Authentication?) {
        when (authentication) {
            null -> setResult(RESULT_CANCELED) // TODO ??
            else -> {
                // Set accountAuthenticatorResult so accountAuthenticatorResponse can react to it
                accountAuthenticatorResult = Bundle().apply {
                    putString(KEY_ACCOUNT_NAME, authentication.username)
                    putString(KEY_ACCOUNT_TYPE, AUTHENTICATOR_ACCOUNT_TYPE)
                    putString(KEY_AUTHTOKEN, authentication.authToken)
                    putString(KEY_PASSWORD, authentication.refreshToken)
                }

                val account = Account(authentication.username, AUTHENTICATOR_ACCOUNT_TYPE)
                accountManager.addOrUpdateAccount(account, authentication.refreshToken)

                sharedPreferences.edit()
                    .putString(PREF_KEY_CURRENT_ACCOUNT_NAME, authentication.username)
                    .apply()

                // Set the result to pass back to the calling activity
                val data = Intent()
                data.putExtra(KEY_ACCOUNT, account)
                setResult(RESULT_OK, data)
            }
        }
        finish()
    }

    // endregion Implemented methods

}
