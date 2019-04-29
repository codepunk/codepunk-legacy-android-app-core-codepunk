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
import com.codepunk.core.BuildConfig.FRAGMENT_TAG_AUTH_DIALOG
import com.codepunk.core.R
import com.codepunk.core.databinding.ActivityAuthenticateBinding
import com.codepunk.core.domain.model.Authorization
import com.codepunk.core.domain.model.NetworkResponse
import com.codepunk.core.lib.AccountAuthenticatorAppCompatActivity
import com.codepunk.core.lib.exception.InactiveUserException
import com.codepunk.core.lib.exception.InvalidCredentialsException
import com.codepunk.core.lib.reset
import com.codepunk.core.presentation.base.AlertDialogFragment
import com.codepunk.core.presentation.base.AlertDialogFragment.AlertDialogFragmentListener
import com.codepunk.core.presentation.base.ContentLoadingProgressBarOwner
import com.codepunk.core.presentation.base.FloatingActionButtonOwner
import com.codepunk.core.util.DataUpdateResolver
import com.codepunk.doofenschmirtz.util.loginator.FormattingLoginator
import com.codepunk.doofenschmirtz.util.taskinator.DataUpdate
import com.codepunk.doofenschmirtz.util.taskinator.FailureUpdate
import com.codepunk.doofenschmirtz.util.taskinator.ProgressUpdate
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

// region Constants

private const val AUTH_DIALOG_REQUEST_CODE = 1

// endregion Constants

/**
 * An Activity that handles all actions relating to creating, selecting, and authenticating
 * an account.
 */
class AuthenticateActivity :
    AccountAuthenticatorAppCompatActivity(),
    HasSupportFragmentInjector,
    ContentLoadingProgressBarOwner,
    FloatingActionButtonOwner,
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

        // Prevent soft keyboard from auto-popping up
        //window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

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

        if (savedInstanceState != null) {
            authenticateResolver.restoreListeners()
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

    override fun onClick(v: View?) {
        loginator.i("onClick: v=$v")
    }

    // endregion Implemented methods

    // region Methods

    /**
     * Reacts to authorization data changes.
     */
    private fun onAuthorizationUpdate(update: DataUpdate<NetworkResponse, Authorization>) {
        authenticateResolver
            .with(binding.coordinatorLayout)
            .resolve(update)
    }

    /**
     * Reacts to register data changes.
     */
    private fun onRegisterUpdate(update: DataUpdate<Void, NetworkResponse>) {
        if (loginator.isLoggable(Log.INFO)) {
            loginator.i("update=$update")
        }
    }

    // endregion Methods

    // region Nested/inner classes

    inner class AuthenticateResolver :
        AlertDialogFragmentListener,
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
            update.e?.also { e ->
                when (e) {
                    is InactiveUserException -> {
                        supportFragmentManager.findFragmentByTag(
                            FRAGMENT_TAG_AUTH_DIALOG
                        ) ?: AlertDialogFragment.showDialogFragmentForResult(
                            supportFragmentManager,
                            FRAGMENT_TAG_AUTH_DIALOG,
                            AUTH_DIALOG_REQUEST_CODE,
                            this
                        )
                        return
                    }
                    is InvalidCredentialsException -> {
                        val text =
                            update.e?.localizedMessage
                                ?: getString(R.string.alert_unknown_error_message)
                        Snackbar.make(
                            binding.coordinatorLayout,
                            text,
                            Snackbar.LENGTH_LONG
                        ).apply {
                            addCallback(object : Snackbar.Callback() {
                                override fun onDismissed(
                                    transientBottomBar: Snackbar?,
                                    event: Int
                                ) {
                                    authViewModel.authorizationDataUpdate.reset()
                                }
                            })
                        }.show()
                        return

                    }
                }
            }

            // TODO Should all errors be dialog fragments? Or Snackbars?
            Toast.makeText(
                this@AuthenticateActivity,
                R.string.alert_unknown_error_message,
                Toast.LENGTH_LONG
            ).show()
        }

        // endregion Inherited methods

        // region Implemented methods

        override fun onBuildAlertDialog(
            requestCode: Int,
            builder: AlertDialog.Builder,
            onClickListener: DialogInterface.OnClickListener
        ) {
            val update =
                authViewModel.authorizationDataUpdate.value as
                        FailureUpdate<NetworkResponse, Authorization>?
            val message =
                update?.e?.localizedMessage ?: getString(R.string.alert_unknown_error_message)
            builder
                .setTitle(R.string.authenticate_label_log_in)
                .setMessage(message)
                .setPositiveButton(R.string.app_got_it, onClickListener)
        }

        override fun onDialogResult(requestCode: Int, resultCode: Int, data: Intent?) {
            authViewModel.authorizationDataUpdate.reset()
        }

        // endregion Implemented methods

        // region Methods

        /**
         * Restores listeners post-configuration change as needed.
         */
        fun restoreListeners() {
            (supportFragmentManager.findFragmentByTag(FRAGMENT_TAG_AUTH_DIALOG)
                    as? AlertDialogFragment)?.listener = this
        }

        // endregion Methods

    }

    interface AuthenticateActivityListener {

        // region Methods

        fun onRegisterRetry()

        // endregion Methods

    }

    // endregion Nested/inner classes

}
