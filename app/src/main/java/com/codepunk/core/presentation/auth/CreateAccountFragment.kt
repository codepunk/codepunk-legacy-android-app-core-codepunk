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

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import com.codepunk.core.R
import com.codepunk.core.databinding.FragmentCreateAccountBinding
import com.codepunk.core.domain.model.NetworkResponse
import com.codepunk.core.lib.hideSoftKeyboard
import com.codepunk.core.lib.reset
import com.codepunk.doofenschmirtz.app.AlertDialogFragment
import com.codepunk.doofenschmirtz.util.loginator.FormattingLoginator
import com.codepunk.doofenschmirtz.util.taskinator.DataUpdate
import com.codepunk.doofenschmirtz.util.taskinator.FailureUpdate
import com.codepunk.doofenschmirtz.util.taskinator.ResultUpdate
import com.codepunk.doofenschmirtz.util.taskinator.SuccessUpdate
import com.codepunk.punkubator.util.validatinator.Validatinator
import com.codepunk.punkubator.util.validatinator.Validatinator.Options
import dagger.android.support.AndroidSupportInjection
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeoutException
import javax.inject.Inject

// TODO NEXT NEXT NEXT
// xxxxx 1. Save/restore alertDialogMessage in instance state
// xxxxx 1.5. Save/restore requestCode in AlertDialogFragment
// 2. Create appropriate alert dialog request code(s)
// xxxxx 3. In onRegisterUpdate, only show (new) alert dialog if it doesn't already exist
// 4. Navigate to login fragment when success alert closes
// 5. Handle failures, including recognizing Network error(s)
// xxxxx 6. If you've already dismissed the dialog, don't show it again after orientation. (i.e. maybe only listen after click/attempt)

// TODO How do I genericize error reporting?
// It comes down to a DataUpdate.
// processUpdate(update)?
// Can I have different responses (alert dialog, snackbar, loading indicator etc.) depending on
// the types of updates?
// i.e. ProgressUpdate might show ProgressBar, etc.
// Maybe what I need is something that ties a data update to a request code
// A DataUpdateHandler could respond to DataUpdates generically
//

private const val REGISTER_REQUEST_CODE = 1

/**
 * A [Fragment] used to create a new account.
 */
class CreateAccountFragment :
    Fragment(),
    View.OnClickListener,
    AlertDialogFragment.OnBuildAlertDialogListener {

    // region Properties

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
     * A set of authorization [Validatinator]s for validating the form.
     */
    @Inject
    lateinit var validatinators: CreateAccountValidatinators

    /**
     * The binding for this fragment.
     */
    private lateinit var binding: FragmentCreateAccountBinding

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

    // endregion Properties

    // region Lifecycle methods

    /**
     * Injects dependencies into this fragment.
     */
    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
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
            R.layout.fragment_create_account,
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

        binding.createBtn.setOnClickListener(this)
        binding.loginBtn.setOnClickListener(this)

        authViewModel.registerDataUpdate.observe(this, Observer { onRegisterUpdate(it) })
    }

    // endregion Inherited methods

    // region Implemented methods

    /**
     * Submits the new account.
     */
    override fun onClick(v: View?) {
        with(binding) {
            when (v) {
                createBtn -> register()
                loginBtn -> Navigation.findNavController(v)
                    .navigate(R.id.action_create_account_to_log_in)
            }
        }
    }

    // TODO Figure out what can be genericized in this method (if anything)
    override fun onBuildAlertDialog(requestCode: Int, builder: AlertDialog.Builder) {
        builder.setTitle(R.string.account_label_create_account)
        when (requestCode) {
            REGISTER_REQUEST_CODE -> {
                val update = authViewModel.registerDataUpdate.value
                when (update) {
                    is FailureUpdate -> {
                        when (update.e) {
                            is ConnectException -> {
                                builder
                                    .setMessage(R.string.alert_dialog_connect_exception_message)
                                    .setPositiveButton(android.R.string.ok, null)
                                    .setNeutralButton(R.string.alert_dialog_connect_exception_neutral_button) { _, _ ->
                                        view?.post { register() }
                                    }
                                return
                            }
                            is SocketTimeoutException -> { // TODO Is this only because mail didn't work?
                                builder
                                    .setMessage(R.string.alert_dialog_timeout_exception_message)
                                    .setPositiveButton(android.R.string.ok, null)
                                    .setNeutralButton(R.string.alert_dialog_connect_exception_neutral_button) { _, _ ->
                                        view?.post { register() }
                                    }
                                return
                            }
                        }
                    }
                    is SuccessUpdate -> {
                        update.result?.message?.also {
                            builder.setMessage(it)
                        } ?: also {
                            builder
                                .setMessage(R.string.alert_dialog_unknown_message)
                                .setPositiveButton(android.R.string.ok) { _, _ ->
                                    Navigation.findNavController(view!!)
                                        .navigate(R.id.action_create_account_to_log_in)
                                }
                        }
                        return
                    }
                }
                // TODO Since we're configuring the builder/dialog here, we don't need onAlertDialogResult.
                // Set set cancel/button listeners here in this fragment. BUT THEN how do we know
                // which dialog it is? Maybe just inline them all.
                // TODO SuccessUpdate (!!)
                builder.setMessage("Success message?")
                builder.setPositiveButton(android.R.string.ok, null)
            }
        }
    }

    override fun onAlertDialogResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REGISTER_REQUEST_CODE -> {
                authViewModel.registerDataUpdate.reset()
            }
        }
    }

    // endregion Implemented methods

    // region Methods

    private fun register() {
        view?.hideSoftKeyboard()
        if (validate()) {
            authViewModel.register(
                binding.usernameEdit.text.toString(),
                binding.emailEdit.text.toString(),
                binding.passwordEdit.text.toString(),
                binding.confirmPasswordEdit.text.toString()
            )
        }
    }

    private fun onRegisterUpdate(
        update: DataUpdate<Void, NetworkResponse>
    ) {
        when (update) {
            is ResultUpdate -> {
                AlertDialogFragment.show(
                    this,
                    REGISTER_RESULT_DIALOG_FRAGMENT_TAG,
                    REGISTER_REQUEST_CODE
                )
            }
        }
    }

    /**
     * Validates the form.
     */
    private fun validate(): Boolean = validatinators.createAccountValidatinator.validate(
        binding,
        options.clear()
    )

    // endregion Methods

    // region Companion object

    companion object {

        // region Properties

        /**
         * The fragment tag to use for the register result dialog fragment.
         */
        @JvmStatic
        private val REGISTER_RESULT_DIALOG_FRAGMENT_TAG =
            LogInFragment::class.java.name + ".REGISTER_RESULT"

        // endregion Properties

    }

    // endregion Companion object

}
