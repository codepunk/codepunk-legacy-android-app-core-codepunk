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

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import com.codepunk.core.R
import com.codepunk.core.databinding.FragmentCreateAccountBinding
import com.codepunk.core.lib.hideSoftKeyboard
import com.codepunk.core.presentation.base.ContentLoadingProgressBarOwner
import com.codepunk.core.presentation.base.FloatingActionButtonOwner
import com.codepunk.doofenschmirtz.util.loginator.FormattingLoginator
import com.codepunk.punkubator.util.validatinator.Validatinator
import com.codepunk.punkubator.util.validatinator.Validatinator.Options
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject
import android.content.DialogInterface.OnClickListener as DialogOnClickListener

/**
 * A [Fragment] used to create a new account.
 */
class CreateAccountFragment :
    Fragment(),
    OnClickListener,
    AuthenticateActivity.AuthenticateActivityListener /*,
    AlertDialogFragmentListener */ {

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
     * This fragment's activity cast to a [ContentLoadingProgressBarOwner].
     */
    private val contentLoadingProgressBarOwner: ContentLoadingProgressBarOwner? by lazy {
        activity as? ContentLoadingProgressBarOwner
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

    /*
    private val registerResolver = RegisterResolver()
    */

    // endregion Properties

    // region Lifecycle methods

    /**
     * Injects dependencies into this fragment.
     */
    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
        (activity as? AuthenticateActivity)?.authenticateActivityListener = this
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

    override fun onDetach() {
        super.onDetach()
        (activity as? AuthenticateActivity)?.authenticateActivityListener = null
    }

    // endregion Lifecycle methods

    // region Inherited methods

    /**
     * Sets up form information and event listeners.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.loginBtn.setOnClickListener(this)
        // authViewModel.registerDataUpdate.observe(this, Observer { onRegisterUpdate(it) })
    }

    /**
     * Listens for floating action button click events.
     */
    override fun onResume() {
        super.onResume()
        floatingActionButtonOwner?.floatingActionButton?.setOnClickListener(this)
    }

    // endregion Inherited methods

    // region Implemented methods

    /**
     * Implementation of [OnClickListener]. Submits the new account.
     */
    override fun onClick(v: View?) {
        when (v) {
            floatingActionButtonOwner?.floatingActionButton -> register()
            binding.loginBtn -> Navigation.findNavController(v)
                .navigate(R.id.action_create_account_to_log_in)
        }
    }

    /*
    /**
     * Implementation of [AlertDialogFragmentListener]. Binds alert dialogs presented by this
     * fragment.
     */
    override fun onBuildAlertDialog(
        requestCode: Int,
        builder: AlertDialog.Builder,
        onClickListener: DialogOnClickListener
    ) {
        registerResolver.onBuildAlertDialog(requestCode, builder, onClickListener)
    }

    /**
     * Implementation of [AlertDialogFragmentListener]. Processes results produced by
     * [AlertDialogFragment]s.
     */
    override fun onDialogResult(requestCode: Int, resultCode: Int, data: Intent?) {
        registerResolver.onDialogResult(requestCode, resultCode, data)
    }
    */

    override fun onRegisterRetry() = register()

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

    /*
    private fun onRegisterUpdate(update: DataUpdate<Void, NetworkResponse>) =
        registerResolver.resolve(update)
    */

    /**
     * Validates the form.
     */
    private fun validate(): Boolean {
        binding.usernameLayout.error = null
        binding.emailLayout.error = null
        binding.passwordLayout.error = null
        binding.confirmPasswordLayout.error = null
        return true
        // return validatinators.createAccountValidatinator.validate(binding, options.clear())
    }

    // endregion Methods

    /*
    // region Nested/inner classes

    private inner class RegisterResolver : DataUpdateResolver<Void, NetworkResponse>() {

        // region Inherited methods

        override fun onPending(update: PendingUpdate<Void, NetworkResponse>): Int {
            contentLoadingProgressBarOwner?.contentLoadingProgressBar?.hide()
            return super.onPending(update)
        }

        override fun onProgress(update: ProgressUpdate<Void, NetworkResponse>): Int {
            contentLoadingProgressBarOwner?.contentLoadingProgressBar?.show()
            return super.onProgress(update)
        }

        override fun onSuccess(update: SuccessUpdate<Void, NetworkResponse>): Int {
            contentLoadingProgressBarOwner?.contentLoadingProgressBar?.hide()
            return super.onSuccess(update)
        }

        override fun onFailure(update: FailureUpdate<Void, NetworkResponse>): Int {
            contentLoadingProgressBarOwner?.contentLoadingProgressBar?.hide()
            update.result?.firstErrorOrNull()?.also { error ->
                view?.findViewWithTag<TextInputLayout>(error.first)?.also { layout ->
                    layout.error = error.second
                    return REQUEST_NONE
                }
            }
            return super.onFailure(update) //REQUEST_REGISTER_FAILURE
        }

        override fun onException(e: Exception, update: FailureUpdate<Void, NetworkResponse>): Int {
            contentLoadingProgressBarOwner?.contentLoadingProgressBar?.hide() // TODO Maybe?
            return super.onException(e, update)
        }

        override fun onAction(update: DataUpdate<Void, NetworkResponse>, action: Int) {
            view?.also {
                showSnackbar(it, action)
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
                    snackbar.setText(R.string.authenticate_label_create_account)
                    //    .setPositiveButton(android.R.string.ok, onClickListener)
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
                REQUEST_SUCCESS -> view?.also {
                    // Pop back to log in fragment (or first destination in the graph
                    // if not found)
                    val controller = Navigation.findNavController(it)
                    if (!controller.popBackStack(R.id.fragment_log_in, false)) {
                        controller.popBackStack(controller.graph.startDestination, false)
                    }
                }
                REQUEST_CONNECT_EXCEPTION,
                REQUEST_TIMEOUT_EXCEPTION -> {
                    when (resultCode) {
                        RESULT_NEUTRAL -> register()
                    }
                }
            }
        }

        // endregion Methods

    }

    // endregion Nested/inner classes

    // region Companion object

    companion object {

        // region Properties

        /**
         * The fragment tag to use for the register result dialog fragment.
         */
        @JvmStatic
        private val REGISTER_FRAGMENT_TAG =
            LogInFragment::class.java.name + ".REGISTER_RESULT"

        // endregion Properties

    }

    // endregion Companion object
    */

}
