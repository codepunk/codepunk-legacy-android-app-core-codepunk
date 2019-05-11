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
import android.accounts.OnAccountsUpdateListener
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.ContentLoadingProgressBar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import com.codepunk.core.BuildConfig.*
import com.codepunk.core.R
import com.codepunk.core.data.remote.entity.RemoteErrorBody
import com.codepunk.core.data.remote.entity.RemoteErrorBody.Type.*
import com.codepunk.core.databinding.FragmentLogInBinding
import com.codepunk.core.domain.model.Authentication
import com.codepunk.core.domain.model.Message
import com.codepunk.core.lib.hideSoftKeyboard
import com.codepunk.core.lib.reset
import com.codepunk.core.presentation.base.AlertDialogFragment
import com.codepunk.core.presentation.base.ContentLoadingProgressBarOwner
import com.codepunk.core.presentation.base.FloatingActionButtonOwner
import com.codepunk.core.presentation.base.FloatingActionButtonOwner.FloatingActionButtonListener
import com.codepunk.core.util.DataUpdateResolver
import com.codepunk.core.util.NetworkTranslator
import com.codepunk.core.util.setSupportActionBarTitle
import com.codepunk.doofenschmirtz.util.loginator.FormattingLoginator
import com.codepunk.doofenschmirtz.util.taskinator.DataUpdate
import com.codepunk.doofenschmirtz.util.taskinator.FailureUpdate
import com.codepunk.doofenschmirtz.util.taskinator.ProgressUpdate
import com.codepunk.doofenschmirtz.util.taskinator.SuccessUpdate
import com.codepunk.punkubator.util.validatinator.Validatinator
import com.codepunk.punkubator.util.validatinator.Validatinator.Options
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

// region Constants

private const val INACTIVE_USER_REQUEST_CODE = 1

// endregion Constants

/**
 * A [Fragment] used to log into an existing account.
 */
class LogInFragment :
    Fragment(),
    OnClickListener,
    OnAccountsUpdateListener,
    FloatingActionButtonListener,
    AlertDialogFragment.AlertDialogFragmentListener {

    // region Properties

    /**
     * The system [AccountManager].
     */
    @Inject
    lateinit var accountManager: AccountManager

    /**
     * A [FormattingLoginator] for writing log messages.
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
     * A set of [Validatinator]s for validating the form.
     */
    @Inject
    lateinit var validatinators: LogInValidatinators

    /**
     * The [NetworkTranslator] for translating messages from the network.
     */
    @Inject
    lateinit var networkTranslator: NetworkTranslator

    /**
     * The content loading [ContentLoadingProgressBar] belonging to this fragment's activity.
     */
    private val contentLoadingProgressBar: ContentLoadingProgressBar? by lazy {
        (activity as? ContentLoadingProgressBarOwner)?.contentLoadingProgressBar
    }

    /**
     * This fragment's activity cast to a [FloatingActionButtonOwner].
     */
    private val floatingActionButtonOwner: FloatingActionButtonOwner? by lazy {
        activity as? FloatingActionButtonOwner
    }

    /**
     * The binding for this fragment.
     */
    private lateinit var binding: FragmentLogInBinding

    /**
     * The [AuthViewModel] instance backing this fragment.
     */
    private val authViewModel: AuthViewModel by lazy {
        ViewModelProviders.of(requireActivity(), viewModelFactory)
            .get(AuthViewModel::class.java)
    }

    /**
     * The default [Options] used to validate the form.
     */
    private val options = Options().apply {
        requestMessage = true
    }

    private lateinit var authResolver: AuthResolver

    private lateinit var registerResolver: RegisterResolver

    private lateinit var sendActivationResolver: SendActivationResolver

    private var authenticationListener: AuthenticationListener? = null

    val authDialogFragment: AlertDialogFragment?
        get() = requireFragmentManager().findFragmentByTag(AUTH_DIALOG_FRAGMENT_TAG)
            as AlertDialogFragment?

    // endregion Properties

    // region Lifecycle methods

    /**
     * Injects dependencies into this fragment.
     */
    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
        authenticationListener = (activity as? AuthenticationListener)
    }

    /**
     * Inflates the view.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_log_in,
            container,
            false
        )
        return binding.root
    }

    /**
     * Listens for appropriate events.
     */
    override fun onResume() {
        super.onResume()
        setSupportActionBarTitle(R.string.authenticate_label_log_in)
        floatingActionButtonOwner?.floatingActionButtonListener = this
        accountManager.addOnAccountsUpdatedListener(
            this,
            null,
            true
        )
    }

    /**
     * Removes any associated listeners.
     */
    override fun onPause() {
        super.onPause()
        accountManager.removeOnAccountsUpdatedListener(this)
        if (floatingActionButtonOwner?.floatingActionButtonListener == this) {
            floatingActionButtonOwner?.floatingActionButtonListener = null
        }
    }

    // endregion Lifecycle methods

    // region Inherited methods

    /**
     * Sets up form information and event listeners.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authResolver = AuthResolver(requireActivity(), view)
        registerResolver = RegisterResolver(requireActivity(), view)
        sendActivationResolver = SendActivationResolver(requireActivity(), view)

        binding.createBtn.setOnClickListener(this)
        binding.forgotPasswordBtn.setOnClickListener(this)

        arguments?.apply {
            // TODO This? Or Bundle Key? (See below)
            if (containsKey(EXTRA_USERNAME)) {
                binding.usernameOrEmailEdit.setText(getString(EXTRA_USERNAME))
            }

            if (containsKey(KEY_USERNAME)) {
                binding.usernameOrEmailEdit.setText(getString(KEY_USERNAME))
            }
        }

        authViewModel.authDataUpdate.removeObservers(this)
        authViewModel.authDataUpdate.observe(
            this,
            Observer { authResolver.resolve(it) }
        )

        authViewModel.registerDataUpdate.removeObservers(this)
        authViewModel.registerDataUpdate.observe(
            this,
            Observer { registerResolver.resolve(it) }
        )

        authViewModel.sendActivationDataUpdate.removeObservers(this)
        authViewModel.sendActivationDataUpdate.observe(
            this,
            Observer { sendActivationResolver.resolve(it) }
        )
    }

    // region Implemented methods

    /**
     * Listens to account updates and updates the list accordingly.
     */
    override fun onAccountsUpdated(accounts: Array<out Account>?) {
        when (accounts?.size) {
            null -> binding.chooseAccountLayout.visibility = View.INVISIBLE
            0 -> binding.chooseAccountLayout.visibility = View.INVISIBLE
            else -> {
                binding.chooseAccountLayout.visibility = View.VISIBLE
                binding.accountLayout.removeAllViews()
                accounts.sortedWith(
                    Comparator { account1, account2 ->
                        when {
                            account1.name < account2.name -> -1
                            account1.name > account2.name -> 1
                            else -> 0
                        }
                    }
                ).forEach { account ->
                    layoutInflater.inflate(
                        R.layout.item_account,
                        binding.accountLayout,
                        false
                    ).apply {
                        val accountImage: AppCompatImageView = findViewById(R.id.account_image)
                        // TODO Set image

                        val usernameText: AppCompatTextView = findViewById(R.id.username_text)
                        usernameText.text = account.name

                        val emailText: AppCompatTextView = findViewById(R.id.email_text)
                        // TODO Do something with email?
                        emailText.visibility = View.GONE

                        setTag(R.id.account, account)
                        setOnClickListener(this@LogInFragment)
                        binding.accountLayout.addView(this)
                    }
                }
            }
        }
    }

    /**
     * Responds to click events.
     */
    override fun onClick(v: View?) {
        when (v) {
            binding.createBtn -> {
                authViewModel.authDataUpdate.reset()
                authViewModel.registerDataUpdate.reset()
                resetErrors()
                Navigation.findNavController(v).navigate(R.id.action_log_in_to_register)
            }
            binding.forgotPasswordBtn -> {
                authViewModel.authDataUpdate.reset()
                authViewModel.registerDataUpdate.reset()
                resetErrors()
                Navigation.findNavController(v).navigate(R.id.action_log_in_to_forgot_password)
            }
            else -> when (v?.id) {
                R.id.account_item -> {
                    val account = v.getTag(R.id.account) as? Account
                    account?.also {
                        onAccountClick(it)
                    }
                }
            }
        }
    }

    override fun onFloatingActionButtonClick(owner: FloatingActionButtonOwner) {
        view?.hideSoftKeyboard()
        if (validate()) {
            authViewModel.authenticate(
                binding.usernameOrEmailEdit.text.toString(),
                binding.passwordEdit.text.toString()
            )
        }
    }

    override fun onBuildAlertDialog(
        requestCode: Int,
        builder: AlertDialog.Builder,
        onClickListener: DialogInterface.OnClickListener
    ) {
        when (requestCode) {
            INACTIVE_USER_REQUEST_CODE -> {
                when (val update = authViewModel.authDataUpdate.value) {
                    is FailureUpdate -> {
                        val remoteErrorBody =
                            update.data?.getParcelable<RemoteErrorBody>(KEY_REMOTE_ERROR_BODY)
                        val message: String = remoteErrorBody?.message?.let {
                            networkTranslator.translate(it)
                        } ?: getString(R.string.alert_unknown_error_message)
                        builder
                            .setTitle(R.string.authenticate_label_log_in)
                            .setMessage(message)
                            .setPositiveButton(R.string.app_got_it, onClickListener)
                            .setNeutralButton(
                                R.string.authenticator_send_again,
                                onClickListener
                            )
                    }
                }
            }
        }
    }

    override fun onDialogResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            INACTIVE_USER_REQUEST_CODE -> {
                when (resultCode) {
                    AlertDialogFragment.RESULT_NEUTRAL -> {
                        val update: FailureUpdate<Void, Authentication>? =
                            authViewModel.authDataUpdate.value as? FailureUpdate
                        val remoteErrorBody=
                            update?.data?.getParcelable<RemoteErrorBody>(KEY_REMOTE_ERROR_BODY)
                        remoteErrorBody?.hint?.also { email ->
                            authViewModel.sendActivationCode(email)
                        }
                    }
                }
                authViewModel.authDataUpdate.reset()
            }
        }
    }

    // endregion Implemented methods

    // region Methods

    /**
     * Validates the form.
     */
    private fun validate(): Boolean {
        return true
        /*
        return validatinators.logInValidatinator.validate(
            binding,
            options.clear()
        )
        */
    }

    private fun onAccountClick(account: Account) {
        if (loginator.isLoggable(Log.DEBUG)) {
            loginator.d("account=$account")
        }
    }

    private fun disableView() {
        // TODO disable controls (or at least the button)
    }

    private fun enableView() {
        // TODO enable controls
    }

    private fun resetErrors() {
        binding.usernameOrEmailLayout.error = null
        binding.passwordLayout.error = null
    }

    private fun resetView() {
        binding.usernameOrEmailEdit.text = null
        binding.passwordEdit.text = null
    }

    // endregion Methods

    // region Nested/inner classes

    private inner class AuthResolver(activity: Activity, view: View) :
        DataUpdateResolver<Void, Authentication>(activity, view) {

        // region Inherited methods

        override fun resolve(update: DataUpdate<Void, Authentication>) {
            // TODO This is exactly the same as the other fragments in this activity
            when (update) {
                is ProgressUpdate -> {
                    contentLoadingProgressBar?.show()
                    disableView()
                }
                else -> {
                    contentLoadingProgressBar?.hide()
                    enableView()
                }
            }
            super.resolve(update)
        }

        @SuppressLint("SwitchIntDef")
        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
            when (event) {
                DISMISS_EVENT_ACTION, DISMISS_EVENT_SWIPE, DISMISS_EVENT_TIMEOUT ->
                    authViewModel.authDataUpdate.reset()
            }
        }

        override fun onSuccess(update: SuccessUpdate<Void, Authentication>): Boolean {
            authenticationListener?.onAuthenticated(update.result)
            return true
        }

        override fun onFailure(update: FailureUpdate<Void, Authentication>): Boolean {
            var handled = super.onFailure(update)
            if (!handled) {
                val remoteErrorBody =
                    update.data?.getParcelable<RemoteErrorBody>(KEY_REMOTE_ERROR_BODY)
                when (remoteErrorBody?.type) {
                    INACTIVE_USER -> {
                        authDialogFragment ?: AlertDialogFragment.showDialogFragmentForResult(
                            this@LogInFragment,
                            AUTH_DIALOG_FRAGMENT_TAG,
                            INACTIVE_USER_REQUEST_CODE
                        )
                        handled = true
                    }
                    INVALID_CREDENTIALS -> {
                        val text: String = remoteErrorBody?.message?.let {
                            networkTranslator.translate(it)
                        } ?: getString(R.string.alert_unknown_error_message)
                        Snackbar.make(view, text, Snackbar.LENGTH_LONG)
                            .addCallback(this)
                            .show()
                        handled = true
                    }
                    INVALID_REQUEST -> {
                        Snackbar.make(
                            view,
                            R.string.translator_string_output_user_credentials_incorrect,
                            Snackbar.LENGTH_LONG
                        ).addCallback(this)
                            .show()
                        handled = true
                    }
                }
            }
            return handled
        }

        // endregion Inherited methods

    }

    private inner class RegisterResolver(activity: Activity, view: View) :
        DataUpdateResolver<Void, Message>(activity, view) {

        // region Properties

        // region Inherited methods

        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
            @SuppressLint("SwitchIntDef")
            when (event) {
                DISMISS_EVENT_ACTION, DISMISS_EVENT_SWIPE, DISMISS_EVENT_TIMEOUT ->
                    authViewModel.registerDataUpdate.reset()
            }
        }

        override fun onSuccess(update: SuccessUpdate<Void, Message>): Boolean {
            update.result?.localizedMessage?.run {
                Snackbar.make(view, this, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.app_got_it) {}
                    .addCallback(this@RegisterResolver)
                    .show()
            } ?: authViewModel.registerDataUpdate.reset()
            return true
        }

        // endregion Inherited methods

    }

    private inner class SendActivationResolver(activity: Activity, view: View) :
        DataUpdateResolver<Void, Message>(activity, view) {

        // region Inherited methods

        override fun resolve(update: DataUpdate<Void, Message>) {
            // TODO This is exactly the same as the other fragments in this activity
            when (update) {
                is ProgressUpdate -> {
                    contentLoadingProgressBar?.show()
                    disableView()
                }
                else -> {
                    contentLoadingProgressBar?.hide()
                    enableView()
                }
            }
            super.resolve(update)
        }

        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
            @SuppressLint("SwitchIntDef")
            when (event) {
                DISMISS_EVENT_ACTION, DISMISS_EVENT_SWIPE, DISMISS_EVENT_TIMEOUT ->
                    authViewModel.sendActivationDataUpdate.reset()
            }
        }

        override fun onSuccess(update: SuccessUpdate<Void, Message>): Boolean {
            update.result?.localizedMessage?.run {
                Snackbar.make(view, this, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.app_got_it) {}
                    .addCallback(this@SendActivationResolver)
                    .show()
            } ?: authViewModel.registerDataUpdate.reset()
            return true
        }

        // endregion Inherited methods

    }

    interface AuthenticationListener {

        // region Methods

        fun onAuthenticated(authentication: Authentication?)

        // endregion Methods

    }

    // endregion Nested/inner classes

    // region Companion object

    companion object {

        // region Properties

        /**
         * The fragment tag to use for the authentication failure dialog fragment.
         */
        @JvmStatic
        private val AUTH_DIALOG_FRAGMENT_TAG =
            LogInFragment::class.java.name + ".AUTH_DIALOG"

        // endregion Properties

    }

    // endregion Companion object

}
