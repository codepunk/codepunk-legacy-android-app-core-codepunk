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
import android.content.DialogInterface.OnClickListener as DialogOnClickListener
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import com.codepunk.core.R
import com.codepunk.core.databinding.FragmentCreateAccountBinding
import com.codepunk.core.domain.model.NetworkResponse
import com.codepunk.core.lib.hideSoftKeyboard
import com.codepunk.core.lib.reset
import com.codepunk.core.presentation.base.AlertDialogFragment
import com.codepunk.core.presentation.base.AlertDialogFragment.AlertDialogFragmentListener
import com.codepunk.core.presentation.base.AlertDialogFragment.Companion.RESULT_NEUTRAL
import com.codepunk.core.presentation.base.DialogHelper
import com.codepunk.core.presentation.base.DialogHelper.Companion.REQUEST_CODE_CONNECT_EXCEPTION
import com.codepunk.core.presentation.base.DialogHelper.Companion.REQUEST_CODE_FIRST_USER
import com.codepunk.core.presentation.base.ContentLoadingProgressBarOwner
import com.codepunk.core.presentation.base.FloatingActionButtonOwner
import com.codepunk.doofenschmirtz.util.loginator.FormattingLoginator
import com.codepunk.doofenschmirtz.util.taskinator.*
import com.codepunk.punkubator.util.validatinator.Validatinator
import com.codepunk.punkubator.util.validatinator.Validatinator.Options
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

/**
 * A [Fragment] used to create a new account.
 */
class CreateAccountFragment :
    Fragment(),
    OnClickListener,
    AlertDialogFragmentListener {

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
     * An [DialogHelper.Factory] to create an instance of [DialogHelper].
     */
    @Inject
    lateinit var dialogHelperFactory: DialogHelper.Factory

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

    /**
     * An [DialogHelper] to help build [AlertDialog]s.
     */
    private val dialogHelper: DialogHelper by lazy {
        dialogHelperFactory.create(requireContext()).apply {
            setDefaultTitle(R.string.authenticate_label_create_account)
        }
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
        binding.loginBtn.setOnClickListener(this)
        authViewModel.registerDataUpdate.observe(this, Observer { onRegisterUpdate(it) })
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

    /**
     * Implementation of [AlertDialogFragmentListener]. Binds alert dialogs presented by this
     * fragment.
     */
    override fun onBuildAlertDialog(
        requestCode: Int,
        builder: AlertDialog.Builder,
        onClickListener: DialogOnClickListener
    ) {
        builder.setTitle(R.string.authenticate_label_create_account)
        when (requestCode) {
            REQUEST_CODE_REGISTER_SUCCESS, REQUEST_CODE_REGISTER_FAILURE -> {
                val message =
                    (authViewModel.registerDataUpdate.value as? ResultUpdate)?.result?.message
                if (message != null) {
                    builder.setMessage(message)
                }
            }
            else -> dialogHelper.onBuildAlertDialog(
                requestCode,
                builder,
                onClickListener
            )
        }
    }

    /**
     * Implementation of [AlertDialogFragmentListener]. Processes results produced by
     * [AlertDialogFragment]s.
     */
    override fun onDialogResult(requestCode: Int, resultCode: Int, data: Intent?) {
        loginator.i("requestCode=$requestCode, resultCode=$resultCode, data=$data")
        when (requestCode) {
            REQUEST_CODE_CONNECT_EXCEPTION -> {
                authViewModel.registerDataUpdate.reset()
                when (resultCode) {
                    RESULT_NEUTRAL -> register()
                }
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

    private fun onRegisterUpdate(update: DataUpdate<Void, NetworkResponse>) {

        // TODO NEXT NEXT NEXT When ResultUpdate, send the update to DialogHelper.
        // MAYBE rename that to ResultUpdateHelper? Because it's all about that.
        // So helper can parse the ResultUpdate and display the appropriate dialog and/or
        // issue a callback?

        when (update) {
            // TODO Pending, Progress, etc.
            is PendingUpdate -> {
                // Enable everything
                // Remove dialogs?
                contentLoadingProgressBarOwner?.contentLoadingProgressBar?.hide()
            }
            is ProgressUpdate -> {
                // Disable everything
                // Show loading dots in button? Or elsewhere?
                contentLoadingProgressBarOwner?.contentLoadingProgressBar?.show()
            }
            is ResultUpdate -> {
                contentLoadingProgressBarOwner?.contentLoadingProgressBar?.hide()
                val dialogRequestCode: Int? = when (update) {
                    is SuccessUpdate -> REQUEST_CODE_REGISTER_SUCCESS
                    is FailureUpdate -> {
                        val error = update.result?.firstErrorOrNull()
                        when (error) {
                            null -> REQUEST_CODE_REGISTER_FAILURE
                            else -> {
                                when (error.first) {
                                    "username" -> binding.usernameLayout.error = error.second
                                    "email" -> binding.emailLayout.error = error.second
                                    "password" -> binding.passwordLayout.error = error.second
                                }
                                null
                            }
                        }
                    }
                    else -> null
                }
                dialogRequestCode?.also { requestCode ->
                    requireFragmentManager().findFragmentByTag(REGISTER_RESULT_FRAGMENT_TAG)
                        ?: AlertDialogFragment.showDialogFragmentForResult(
                            this,
                            REGISTER_RESULT_FRAGMENT_TAG,
                            requestCode
                        )
                }
            }
        }
    }

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

    // region Companion object

    companion object {

        // region Properties

        /**
         * A request code indicating a successful registration (i.e. account creation).
         */
        @JvmStatic
        private val REQUEST_CODE_REGISTER_SUCCESS = REQUEST_CODE_FIRST_USER

        /**
         * A request code indicating an unsuccessful registration.
         */
        @JvmStatic
        private val REQUEST_CODE_REGISTER_FAILURE = REQUEST_CODE_FIRST_USER + 1

        /**
         * The fragment tag to use for the register result dialog fragment.
         */
        @JvmStatic
        private val REGISTER_RESULT_FRAGMENT_TAG =
            LogInFragment::class.java.name + ".REGISTER_RESULT"

        // endregion Properties

    }

    // endregion Companion object

}
