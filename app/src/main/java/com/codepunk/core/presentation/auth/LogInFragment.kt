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
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.codepunk.core.BuildConfig.*
import com.codepunk.core.R
import com.codepunk.core.data.remote.entity.RemoteErrorBody
import com.codepunk.core.data.remote.entity.RemoteErrorBody.Type.*
import com.codepunk.core.databinding.FragmentLogInBinding
import com.codepunk.core.domain.model.Authentication
import com.codepunk.core.domain.model.Message
import com.codepunk.doofenschmirtz.app.AlertDialogFragment
import com.codepunk.doofenschmirtz.app.AlertDialogFragment.AlertDialogFragmentListener
import com.codepunk.doofenschmirtz.util.consume
import com.codepunk.doofenschmirtz.util.resourceinator.FailureResource
import com.codepunk.doofenschmirtz.util.resourceinator.Resource
import com.codepunk.doofenschmirtz.util.resourceinator.ResourceResolvinator
import com.codepunk.doofenschmirtz.util.resourceinator.SuccessResource
import com.codepunk.punkubator.util.validatinator.Validatinator
import com.codepunk.punkubator.util.validatinator.Validatinator.Options
import com.google.android.material.snackbar.Snackbar
import javax.inject.Inject

// region Constants

/**
 * A request code for an alert dialog notifying the user that the user account to which they are
 * attempting to log in is currently inactive.
 */
private const val INACTIVE_USER_REQUEST_CODE = 1

// endregion Constants

/**
 * A [Fragment] used to log into an existing account.
 */
class LogInFragment :
    AbsAuthFragment(),
    AlertDialogFragmentListener {

    // region Properties

    /**
     * The system [AccountManager].
     */
    @Inject
    lateinit var accountManager: AccountManager

    /**
     * A set of [Validatinator]s for validating the form.
     */
    @Suppress("UNUSED")
    @Inject
    lateinit var validatinators: LogInValidatinators

    /**
     * The binding for this fragment.
     */
    private lateinit var binding: FragmentLogInBinding

    /**
     * The default [Options] used to validate the form.
     */
    @Suppress("UNUSED")
    private val options = Options().apply {
        requestMessage = true
    }

    /**
     * An instance of [AuthResolvinator] for resolving authorization-related resources.
     */
    private lateinit var authResolvinator: AuthResolvinator

    /**
     * An instance of [RegisterResolvinator] for resolving registration-related resources.
     */
    private lateinit var registerResolvinator: RegisterResolvinator

    /**
     * An instance of [SendEmailResolvinator] for resolving resources related to email requests.
     */
    private lateinit var sendEmailResolvinator: SendEmailResolvinator

    /**
     * The current [AlertDialogFragment] instance (if any) showing any authorization-related
     * messages.
     */
    val authDialogFragment: AlertDialogFragment?
        get() = requireFragmentManager().findFragmentByTag(AUTH_DIALOG_FRAGMENT_TAG)
            as AlertDialogFragment?

    // endregion Properties

    // region Lifecycle methods

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

    // endregion Lifecycle methods

    // region Inherited methods

    /**
     * Sets up form information and event listeners.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authResolvinator = AuthResolvinator(view)
        registerResolvinator = RegisterResolvinator(view)
        sendEmailResolvinator = SendEmailResolvinator(view)

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

        authViewModel.authLiveResource.removeObservers(this)
        authViewModel.authLiveResource.observe(
            this,
            Observer { authResolvinator.resolve(it) }
        )

        authViewModel.registerLiveResource.removeObservers(this)
        authViewModel.registerLiveResource.observe(
            this,
            Observer { registerResolvinator.resolve(it) }
        )

        authViewModel.sendActivationLiveResource.removeObservers(this)
        authViewModel.sendActivationLiveResource.observe(
            this,
            Observer { sendEmailResolvinator.resolve(it) }
        )

        authViewModel.sendPasswordResetLiveResource.removeObservers(this)
        authViewModel.sendPasswordResetLiveResource.observe(
            this,
            Observer { sendEmailResolvinator.resolve(it) }
        )
    }

    /**
     * Responds to click events.
     */
    override fun onClick(v: View?) {
        super.onClick(v)
        when (v) {
            binding.createBtn -> {
                authViewModel.authLiveResource.consume()
                authViewModel.registerLiveResource.consume()
                clearErrors()
                Navigation.findNavController(v).navigate(R.id.action_log_in_to_register)
            }
            binding.forgotPasswordBtn -> {
                authViewModel.authLiveResource.consume()
                authViewModel.registerLiveResource.consume()
                clearErrors()
                Navigation.findNavController(v).navigate(R.id.action_log_in_to_forgot_password)
            }
            floatingActionButton -> if (validate()) {
                authViewModel.authenticate(
                    binding.usernameOrEmailEdit.text.toString(),
                    binding.passwordEdit.text.toString()
                )
            }
            /*
            else -> when (v?.id) {
                R.id.account_item -> {
                    val account = v.getTag(R.id.account) as? Account
                    account?.also {
                        onAccountClick(it)
                    }
                }
            }
            */
        }
    }

    override fun clearErrors() {
        binding.usernameOrEmailLayout.error = null
        binding.passwordLayout.error = null
    }

    override fun resetView() {
        binding.usernameOrEmailEdit.text = null
        binding.passwordEdit.text = null
    }

    /**
     * Validates the form.
     */
    @Suppress("REDUNDANT_OVERRIDING_METHOD")
    override fun validate(): Boolean = super.validate()

    // region Implemented methods

    /*
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
                        // val accountImage: AppCompatImageView = findViewById(R.id.account_image)
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
    */

    /**
     * Builds the alert dialog associated with [requestCode], using [fragment] and [builder]
     * as appropriate.
     */
    override fun onBuildAlertDialog(
        fragment: AlertDialogFragment,
        requestCode: Int,
        builder: AlertDialog.Builder,
        savedInstanceState: Bundle?
    ) {
        when (requestCode) {
            INACTIVE_USER_REQUEST_CODE -> {
                when (val resource = authViewModel.authLiveResource.value) {
                    is FailureResource -> {
                        val remoteErrorBody =
                            resource.data?.getParcelable<RemoteErrorBody>(KEY_REMOTE_ERROR_BODY)
                        val message: String = remoteErrorBody?.message?.let {
                            translatinator.translate(it)
                        } ?: getString(com.codepunk.doofenschmirtz.R.string.unknown_error_message)
                        builder
                            .setTitle(R.string.auth_title_activation_required)
                            .setMessage(message)
                            .setPositiveButton(R.string.app_got_it, fragment)
                            .setNeutralButton(R.string.auth_label_send_again, fragment)
                    }
                }
            }
        }
    }

    /**
     * Responds to the result of the alert dialog associated with [requestCode].
     */
    override fun onDialogResult(
        fragment: AlertDialogFragment,
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        when (requestCode) {
            INACTIVE_USER_REQUEST_CODE -> {
                when (resultCode) {
                    AlertDialogFragment.RESULT_NEUTRAL -> {
                        val resource: FailureResource<Void, Authentication>? =
                            authViewModel.authLiveResource.value as? FailureResource
                        val remoteErrorBody =
                            resource?.data?.getParcelable<RemoteErrorBody>(KEY_REMOTE_ERROR_BODY)
                        remoteErrorBody?.hint?.also { email ->
                            authViewModel.sendActivationLink(email)
                        }
                    }
                }
                authViewModel.authLiveResource.consume()
            }
        }
    }

    // endregion Implemented methods

    // region Methods

    /*
    /**
     * Responds to an account being clicked.
     */
    private fun onAccountClick(account: Account) {
        if (loginator.isLoggable(Log.DEBUG)) {
            loginator.d("account=$account")
        }
    }
    */

    // endregion Methods

    // region Nested/inner classes

    /**
     * A [ResourceResolvinator] that resolves authorization-related [Resource]s.
     */
    private inner class AuthResolvinator(view: View) :
        AbsAuthResolvinator<Void, Authentication>(view) {

        // region Inherited methods

        @SuppressLint("SwitchIntDef")
        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
            when (event) {
                DISMISS_EVENT_ACTION, DISMISS_EVENT_SWIPE, DISMISS_EVENT_TIMEOUT ->
                    authViewModel.authLiveResource.consume()
            }
        }

        override fun onFailure(resource: FailureResource<Void, Authentication>): Boolean {
            var handled = super.onFailure(resource)
            if (!handled) {
                val remoteErrorBody =
                    resource.data?.getParcelable<RemoteErrorBody>(KEY_REMOTE_ERROR_BODY)
                when (remoteErrorBody?.type) {
                    INACTIVE_USER -> {
                        authDialogFragment ?: AlertDialogFragment.showDialogFragmentForResult(
                            this@LogInFragment,
                            INACTIVE_USER_REQUEST_CODE,
                            AUTH_DIALOG_FRAGMENT_TAG
                        )
                        handled = true
                    }
                    INVALID_CREDENTIALS -> {
                        val text: String = remoteErrorBody.message?.let {
                            translatinator.translate(it)
                        } ?: getString(com.codepunk.doofenschmirtz.R.string.unknown_error_message)
                        Snackbar.make(view, text, Snackbar.LENGTH_LONG)
                            .addCallback(this)
                            .show()
                        handled = true
                    }
                    INVALID_REQUEST -> {
                        Snackbar.make(
                            view,
                            R.string.trans_output_user_credentials_incorrect,
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

    /**
     * A [ResourceResolvinator] that resolves registration-related [Resource]s.
     */
    private inner class RegisterResolvinator(view: View) :
        AbsAuthResolvinator<Void, Message>(view) {

        // region Inherited methods

        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
            @SuppressLint("SwitchIntDef")
            when (event) {
                DISMISS_EVENT_ACTION, DISMISS_EVENT_SWIPE, DISMISS_EVENT_TIMEOUT ->
                    authViewModel.registerLiveResource.consume()
            }
        }

        override fun onSuccess(resource: SuccessResource<Void, Message>): Boolean {
            resource.result?.localizedMessage?.run {
                Snackbar.make(view, this, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.app_got_it) {}
                    .addCallback(this@RegisterResolvinator)
                    .show()
            } ?: authViewModel.registerLiveResource.consume()
            return true
        }

        // endregion Inherited methods

    }

    /**
     * A [ResourceResolvinator] that resolves [Resource]s generated by requests for emails (i.e.
     * authorization link, password reset link, etc.).
     */
    private inner class SendEmailResolvinator(view: View) :
        AbsAuthResolvinator<Void, Message>(view) {

        // region Inherited methods

        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
            @SuppressLint("SwitchIntDef")
            when (event) {
                DISMISS_EVENT_ACTION, DISMISS_EVENT_SWIPE, DISMISS_EVENT_TIMEOUT -> {
                    authViewModel.sendActivationLiveResource.consume()
                    authViewModel.sendPasswordResetLiveResource.consume()
                }
            }
        }

        override fun onSuccess(resource: SuccessResource<Void, Message>): Boolean {
            resource.result?.localizedMessage?.run {
                Snackbar.make(view, this, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.app_got_it) {}
                    .addCallback(this@SendEmailResolvinator)
                    .show()
            } ?: authViewModel.registerLiveResource.consume()
            return true
        }

        // endregion Inherited methods

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
