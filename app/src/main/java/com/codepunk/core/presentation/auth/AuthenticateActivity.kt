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
import com.codepunk.core.BuildConfig.*
import com.codepunk.core.R
import com.codepunk.core.databinding.ActivityAuthenticateBinding
import com.codepunk.core.domain.model.Authorization
import com.codepunk.core.domain.model.NetworkResponse
import com.codepunk.core.lib.AccountAuthenticatorAppCompatActivity
import com.codepunk.core.lib.reset
import com.codepunk.core.presentation.base.AlertDialogFragment
import com.codepunk.core.presentation.base.ContentLoadingProgressBarOwner
import com.codepunk.core.presentation.base.FloatingActionButtonOwner
import com.codepunk.core.util.DataUpdateResolver
import com.codepunk.doofenschmirtz.util.loginator.FormattingLoginator
import com.codepunk.doofenschmirtz.util.taskinator.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
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

    /**
     * A [DataUpdateResolver] for handling authenticate updates.
     */
    private val authenticateResolver = AuthenticateResolver()

    /**
     * A [DataUpdateResolver] for handling register updates.
     */
    private val registerResolver = RegisterResolver()

    var authenticateActivityListener: AuthenticateActivityListener? = null

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
    ) = authenticateResolver.onBuildAlertDialog(requestCode, builder, onClickListener)

    override fun onDialogResult(requestCode: Int, resultCode: Int, data: Intent?) =
        registerResolver.onDialogResult(requestCode, resultCode, data)

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
        authenticateResolver.resolve(update)
        /*
        when (update) {
            is ProgressUpdate -> {
                // TODO Loading dialog (show and hide)
            }
            is SuccessUpdate -> {
                val result = update.data
                when (result) {
                    null -> setResult(RESULT_CANCELED)
                    else -> {
                        // TODO I need to either check the user here (for active) or change the
                        // endpoint to return something different if we log in and the user is NOT active

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
                val networkMessage =
                    update.data?.getParcelable(KEY_NETWORK_RESPONSE) as NetworkResponse?

                loginator.d("update=$update")

            }
        }

        val response = when (update) {
            is SuccessUpdate -> update.result
            is FailureUpdate -> update.result
            else -> null
        }
        */

        /*
        val httpStatus = when (response) {
            null -> null
            else -> HttpStatus.lookup(response.code())
        }
        */

        /*
        // TODO If I get here, I might have a ProgressUpdate with a RemoteNetworkResponse of
        // "We sent you an activation code! Please check your e-mail."
        // and null errors. How do I best "catch" it?
        loginator.d("httpStatus=$httpStatus, update=$update")
        */
    }

    private fun onRegisterUpdate(update: DataUpdate<Void, NetworkResponse>) {
        if (loginator.isLoggable(Log.INFO)) {
            loginator.i("update=$update")
        }
        registerResolver.resolve(update)
    }

    // endregion Methods

    // region Nested/inner classes

    private inner class AuthenticateResolver :
        DataUpdateResolver<NetworkResponse, Authorization>() {

        // region Inherited methods

        override fun onPending(update: PendingUpdate<NetworkResponse, Authorization>): Int {
            contentLoadingProgressBar.hide()
            return super.onPending(update)
        }

        override fun onProgress(update: ProgressUpdate<NetworkResponse, Authorization>): Int {
            contentLoadingProgressBar.show()
            return super.onProgress(update)
        }

        override fun onSuccess(update: SuccessUpdate<NetworkResponse, Authorization>): Int {
            contentLoadingProgressBar.hide()
            return super.onSuccess(update)
        }

        override fun onFailure(update: FailureUpdate<NetworkResponse, Authorization>): Int {
            contentLoadingProgressBar.hide()
            return super.onFailure(update)
        }

        override fun onException(
            e: Exception,
            update: FailureUpdate<NetworkResponse, Authorization>
        ): Int {
            contentLoadingProgressBar.hide() // TODO Maybe?
            return super.onException(e, update)
        }

        override fun onAction(update: DataUpdate<NetworkResponse, Authorization>, action: Int) {
            when (action) {
                REQUEST_FAILURE -> showAlertDialog(
                    this@AuthenticateActivity,
                    AUTHENTICATE_DIALOG_FRAGMENT_TAG,
                    action
                )
                else -> showSnackbar(binding.coordinatorLayout, action)
            }
        }

        override fun onBuildAlertDialog(
            requestCode: Int,
            builder: AlertDialog.Builder,
            onClickListener: DialogInterface.OnClickListener
        ) {
            when (requestCode) {
                REQUEST_FAILURE -> {
                    builder.setTitle(R.string.authenticate_label_create_account)
                        .setPositiveButton(android.R.string.ok, onClickListener)
                    val data = (authViewModel.authorizationDataUpdate.value as? ResultUpdate)?.data
                    val networkResponse =
                        data?.getParcelable<NetworkResponse>(KEY_NETWORK_RESPONSE)
                    networkResponse?.message?.also {
                        builder.setMessage(it)
                    } ?: also {
                        builder.setMessage(R.string.alert_error)
                    }
                }
                /*
                REQUEST_SUCCESS -> {
                    builder.setTitle(R.string.authenticate_label_create_account)
                        .setPositiveButton(android.R.string.ok, onClickListener)
                    val message =
                        (authViewModel.registerDataUpdate.value as? SuccessUpdate)?.result?.message
                    message?.also {
                        builder.setMessage(it)
                    } ?: also {
                        builder.setMessage(R.string.alert_success)
                    }
                }
                */
                else -> super.onBuildAlertDialog(requestCode, builder, onClickListener)
            }
        }

        // endregion Inherited methods

    }

    private inner class RegisterResolver : DataUpdateResolver<Void, NetworkResponse>() {

        // region Inherited methods

        override fun onPending(update: PendingUpdate<Void, NetworkResponse>): Int {
            contentLoadingProgressBar.hide()
            return super.onPending(update)
        }

        override fun onProgress(update: ProgressUpdate<Void, NetworkResponse>): Int {
            contentLoadingProgressBar.show()
            return super.onProgress(update)
        }

        override fun onSuccess(update: SuccessUpdate<Void, NetworkResponse>): Int {
            contentLoadingProgressBar.hide()
            return super.onSuccess(update)
        }

        override fun onFailure(update: FailureUpdate<Void, NetworkResponse>): Int {
            contentLoadingProgressBar.hide()
            update.result?.firstErrorOrNull()?.also { error ->
                binding.content.findViewWithTag<TextInputLayout>(error.first)?.also { layout ->
                    layout.error = error.second
                    return REQUEST_NONE
                }
            }
            return super.onFailure(update) //REQUEST_REGISTER_FAILURE
        }

        override fun onException(e: Exception, update: FailureUpdate<Void, NetworkResponse>): Int {
            contentLoadingProgressBar.hide() // TODO Maybe?
            return super.onException(e, update)
        }

        override fun onAction(update: DataUpdate<Void, NetworkResponse>, action: Int) {
            when (action) {
                REQUEST_SUCCESS -> {
                    // Pop back to log in fragment (or first destination in the graph
                    // if log in fragment not found)

                    // TODO NEXT: Only want to do this the first time! Not every time after rotation

                    val controller = Navigation.findNavController(
                        this@AuthenticateActivity,
                        R.id.authenticate_nav_fragment
                    )

                    val navOptions: NavOptions = NavOptions.Builder()
                        .setPopUpTo(R.id.navigation_account, true)
                        .build()

                    controller.navigate(
                        CreateAccountFragmentDirections.actionCreateAccountToLogIn(),
                        navOptions
                    )

                    showSnackbar(binding.coordinatorLayout, action)
                }
                else -> showSnackbar(binding.coordinatorLayout, action)
            }

            /*
            showAlertDialog(
                this@CreateAccountFragment,
                REGISTER_FRAGMENT_TAG,
                action
            )
            */
        }

        override fun onBuildSnackbar(requestCode: Int, snackbar: Snackbar) {
            when (requestCode) {
                REQUEST_SUCCESS -> {
                    snackbar/*.setText(R.string.authenticate_label_create_account)*/
                        .setAction(R.string.app_got_it, this@AuthenticateActivity)
                    //    .setPositiveButton(android.R.string.ok, onClickListener)
                    snackbar.duration = BaseTransientBottomBar.LENGTH_INDEFINITE
                    val message =
                        (authViewModel.registerDataUpdate.value as? SuccessUpdate)?.result?.message
                    message?.also {
                        snackbar.setText(it)
                    } ?: also {
                        snackbar.setText(R.string.alert_success)
                    }
                }
                else -> super.onBuildSnackbar(requestCode, snackbar)
            }
        }

        override fun onBuildAlertDialog(
            requestCode: Int,
            builder: AlertDialog.Builder,
            onClickListener: DialogInterface.OnClickListener
        ) {
            when (requestCode) {
                REQUEST_SUCCESS -> {
                    builder.setTitle(R.string.authenticate_label_create_account)
                        .setPositiveButton(android.R.string.ok, onClickListener)
                    val message =
                        (authViewModel.registerDataUpdate.value as? SuccessUpdate)?.result?.message
                    message?.also {
                        builder.setMessage(it)
                    } ?: also {
                        builder.setMessage(R.string.alert_success)
                    }
                }
                else -> super.onBuildAlertDialog(requestCode, builder, onClickListener)
            }
        }

        // endregion Inherited methods

        // region Methods

        fun onDialogResult(requestCode: Int, resultCode: Int, data: Intent?) {
            authViewModel.registerDataUpdate.reset()
            when (requestCode) {
                REQUEST_SUCCESS -> {
                    // Pop back to log in fragment (or first destination in the graph
                    // if not found)
                    val controller = Navigation.findNavController(
                        this@AuthenticateActivity,
                        R.id.authenticate_nav_fragment
                    )
                    if (!controller.popBackStack(R.id.fragment_log_in, false)) {
                        controller.popBackStack(controller.graph.startDestination, false)
                    }
                }
                REQUEST_CONNECT_EXCEPTION,
                REQUEST_TIMEOUT_EXCEPTION -> {
                    when (resultCode) {
                        AlertDialogFragment.RESULT_NEUTRAL ->
                            authenticateActivityListener?.onRegisterRetry()
                    }
                }
            }
        }

        // endregion Methods

    }

    interface AuthenticateActivityListener {

        // region Methods

        fun onRegisterRetry()

        // endregion Methods

    }

    // endregion Nested/inner classes

    // region Companion object

    companion object {

        // region Properties

        @JvmStatic
        private val AUTHENTICATE_DIALOG_FRAGMENT_TAG =
            AuthenticateActivity::class.java.name + ".AUTHENTICATE_DIALOG_FRAGMENT_TAG"

        // endregion Properties

    }

    // endregion Companion object

}
