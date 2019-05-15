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
import android.view.View
import android.view.View.OnClickListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.codepunk.core.BuildConfig.*
import com.codepunk.core.R
import com.codepunk.core.databinding.ActivityAuthenticateBinding
import com.codepunk.core.domain.model.Authentication
import com.codepunk.core.lib.AccountAuthenticatorAppCompatActivity
import com.codepunk.core.lib.addOrUpdateAccount
import com.codepunk.core.presentation.base.FloatingActionButtonOwner
import com.codepunk.doofenschmirtz.util.loginator.FormattingLoginator
import com.codepunk.doofenschmirtz.util.resourceinator.ProgressResource
import com.codepunk.doofenschmirtz.util.resourceinator.Resource
import com.codepunk.doofenschmirtz.util.resourceinator.SuccessResource
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
    OnClickListener,
    FloatingActionButtonOwner {

    // region Implemented properties

    /**
     * Implementation of [FloatingActionButtonOwner]. An optional listener for
     * floating action button-related events.
     */
    override var floatingActionButtonListener:
        FloatingActionButtonOwner.FloatingActionButtonListener? = null

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
     * The injected [ViewModelProvider.Factory] that we will use to get an instance of
     * [AuthViewModel].
     */
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

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
     * The [AuthViewModel] instance backing this fragment.
     */
    private val authViewModel: AuthViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(AuthViewModel::class.java)
    }

    /**
     * The [NavController] for the activity.
     */
    private val navController: NavController by lazy {
        Navigation.findNavController(this, R.id.authenticate_nav_fragment)
    }

    /**
     * The app bar configuration for the activity.
     */
    private val appBarConfiguration: AppBarConfiguration by lazy {
        AppBarConfiguration(navController.graph)
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
        setSupportActionBar(binding.toolbar)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener(this)

        authViewModel.authLiveResource.observe(this, Observer { onAuthResource(it) })
        authViewModel.registerLiveResource.observe(this, Observer { onResource(it) })
        authViewModel.sendActivationLiveResource.observe(this, Observer { onResource(it) })
        authViewModel.sendPasswordResetLiveResource.observe(this, Observer { onResource(it) })

        if (savedInstanceState == null) {
            // If the supplied intent specifies it, navigate to an alternate destination
            // and pop up inclusive to that new destination
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.fragment_authenticate, true)
                .build()
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
    override fun onSupportNavigateUp(): Boolean =
        navController.navigateUp() || super.onSupportNavigateUp()

    // endregion Inherited methods

    // region Implemented methods

    /**
     * Implementation of [HasSupportFragmentInjector]. Returns a
     * [DispatchingAndroidInjector] that injects dependencies into fragments.
     */
    override fun supportFragmentInjector(): AndroidInjector<Fragment> =
        fragmentDispatchingAndroidInjector

    /**
     * Responds to click events.
     */
    override fun onClick(v: View?) {
        when (v) {
            binding.fab -> floatingActionButtonListener?.onFloatingActionButtonClick(this)
        }
    }

    // endregion Implemented methods

    // region Methods

    /**
     * Reacts to the state of the supplied [resource].
     */
    private fun onResource(resource: Resource<*, *>) {
        when (resource) {
            is ProgressResource -> binding.loadingProgress.show()
            else -> binding.loadingProgress.hide()
        }
    }

    /**
     * Reacts to the state of the supplied [resource].
     */
    private fun onAuthResource(resource: Resource<Void, Authentication>) {
        when (resource) {
            is ProgressResource -> binding.loadingProgress.show()
            is SuccessResource -> {
                binding.loadingProgress.hide()
                when (val authentication = resource.result) {
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
            else -> binding.loadingProgress.hide()
        }
    }

    // endregion Methods

}
