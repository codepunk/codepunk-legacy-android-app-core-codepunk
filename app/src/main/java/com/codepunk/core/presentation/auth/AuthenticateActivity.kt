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

import android.accounts.AccountManager
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.ContentLoadingProgressBar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import com.codepunk.core.BuildConfig.CATEGORY_CREATE_ACCOUNT
import com.codepunk.core.BuildConfig.CATEGORY_LOG_IN
import com.codepunk.core.R
import com.codepunk.core.databinding.ActivityAuthenticateBinding
import com.codepunk.core.domain.model.Authorization
import com.codepunk.core.domain.model.NetworkResponse
import com.codepunk.core.lib.AccountAuthenticatorAppCompatActivity
import com.codepunk.core.presentation.base.AlertDialogFragment
import com.codepunk.core.presentation.base.ContentLoadingProgressBarOwner
import com.codepunk.core.presentation.base.FloatingActionButtonOwner
import com.codepunk.core.util.DataUpdateResolver
import com.codepunk.doofenschmirtz.util.loginator.FormattingLoginator
import com.codepunk.doofenschmirtz.util.taskinator.*
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
    AlertDialogFragment.AlertDialogFragmentListener,
    View.OnClickListener {

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
     * The [NavController] for the activity.
     */
    private val navController: NavController by lazy {
        Navigation.findNavController(this, R.id.authenticate_nav_fragment).apply {
            addOnDestinationChangedListener { _, destination, _ ->
                title = destination.label
            }
        }
    }

    /**
     * The [NavHostFragment] for the activity.
     */
    private val navHostFragment: NavHostFragment by lazy {
        supportFragmentManager.findFragmentById(R.id.authenticate_nav_fragment) as NavHostFragment
    }

    var authenticateActivityListener: AuthenticateActivityListener? = null

    var authenticateResolver: AuthenticateResolver = AuthenticateResolver()

    // endregion Properties

    // region Lifecycle methods

    /**
     * Creates the content view and pulls values from the passed [Intent].
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_authenticate)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        if (savedInstanceState == null) {
            // If the supplied intent specifies it, navigate to an alternate destination
            // and pop up inclusive to that new destination
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.fragment_authenticate, true)
                .build()
            val navController =
                Navigation.findNavController(this, R.id.authenticate_nav_fragment)
            when {
                intent.categories.contains(CATEGORY_CREATE_ACCOUNT) -> navController.navigate(
                    R.id.action_auth_to_create_account,
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

        authViewModel.registerDataUpdate.observe(this, Observer { onRegisterUpdate(it) })
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

    // TODO Here's the thing. This method needs to know which resolver to call >:(
    override fun onBuildAlertDialog(
        requestCode: Int,
        builder: AlertDialog.Builder,
        onClickListener: DialogInterface.OnClickListener
    ) {

    }

    override fun onDialogResult(requestCode: Int, resultCode: Int, data: Intent?) {

    }

    override fun onClick(v: View?) {
        loginator.i("onClick: v=$v")
    }

    // endregion Implemented methods

    // region Methods

    /**
     * Reacts to authorization data changing.
     */
    private fun onAuthorizationUpdate(update: DataUpdate<NetworkResponse, Authorization>) {
        if (loginator.isLoggable(Log.INFO)) {
            loginator.i("update=$update")
        }

        authenticateResolver
            .with(binding.coordinatorLayout)
            .resolve(update)

        /*
        when (update) {
            is ProgressUpdate -> binding.loadingProgress.show() // TODO Tell fragments to enable/disable controls
            is SuccessUpdate -> {
                binding.loadingProgress.hide()
            }
            is FailureUpdate -> {
                binding.loadingProgress.hide()
                Toast.makeText(
                    this,
                    update.e?.localizedMessage ?: "",
                    Toast.LENGTH_LONG
                ).show()
            }
            else -> binding.loadingProgress.hide()
        }
        */
    }

    private fun onRegisterUpdate(update: DataUpdate<Void, NetworkResponse>) {
        if (loginator.isLoggable(Log.INFO)) {
            loginator.i("update=$update")
        }

        when (update) {
            is ProgressUpdate -> binding.loadingProgress.show()
            is SuccessUpdate -> binding.loadingProgress.hide()
            is FailureUpdate -> binding.loadingProgress.hide()
            else -> binding.loadingProgress.hide()
        }
    }

    // endregion Methods

    // region Nested/inner classes

    inner class AuthenticateResolver :
        DataUpdateResolver<NetworkResponse, Authorization>(this) {

        // region Inherited methods

        override fun resolve(update: DataUpdate<NetworkResponse, Authorization>) {
            val loading = (update is ProgressUpdate)
            if (loading) {
                binding.loadingProgress.show()
                // TODO disable controls (or at least the button)
            } else {
                binding.loadingProgress.hide()
                // TODO enable controls
            }
            super.resolve(update)
        }

        override fun onFailure(update: FailureUpdate<NetworkResponse, Authorization>) {
            // TODO Make this a DialogFragment
            val message =
                update.e?.localizedMessage ?: getString(R.string.alert_unknown_error_message)
            Toast.makeText(
                this@AuthenticateActivity,
                message,
                Toast.LENGTH_LONG
            ).show()
        }

        // endregion Inherited methods

    }

    interface AuthenticateActivityListener {

        // region Methods

        fun onRegisterRetry()

        // endregion Methods

    }

    // endregion Nested/inner classes

}
