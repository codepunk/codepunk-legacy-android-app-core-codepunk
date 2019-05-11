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

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.core.widget.ContentLoadingProgressBar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import com.codepunk.core.BuildConfig.KEY_REMOTE_ERROR_BODY
import com.codepunk.core.R
import com.codepunk.core.data.remote.entity.RemoteErrorBody
import com.codepunk.core.databinding.FragmentRegisterBinding
import com.codepunk.core.domain.model.Message
import com.codepunk.core.lib.hideSoftKeyboard
import com.codepunk.core.lib.reset
import com.codepunk.core.presentation.base.ContentLoadingProgressBarOwner
import com.codepunk.core.presentation.base.FloatingActionButtonOwner
import com.codepunk.core.presentation.base.FloatingActionButtonOwner.FloatingActionButtonListener
import com.codepunk.core.util.DataUpdateResolver
import com.codepunk.core.util.NetworkTranslator
import com.codepunk.core.util.setSupportActionBarTitle
import com.codepunk.doofenschmirtz.util.http.HttpStatusException
import com.codepunk.doofenschmirtz.util.loginator.FormattingLoginator
import com.codepunk.doofenschmirtz.util.taskinator.DataUpdate
import com.codepunk.doofenschmirtz.util.taskinator.FailureUpdate
import com.codepunk.doofenschmirtz.util.taskinator.ProgressUpdate
import com.codepunk.doofenschmirtz.util.taskinator.SuccessUpdate
import com.codepunk.punkubator.util.validatinator.Validatinator
import com.codepunk.punkubator.util.validatinator.Validatinator.Options
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject
import android.content.DialogInterface.OnClickListener as DialogOnClickListener

/**
 * A [Fragment] used to create a new account.
 */
class RegisterFragment :
    Fragment(),
    OnClickListener,
    FloatingActionButtonListener {

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
     * A set of [Validatinator]s for validating the form.
     */
    @Inject
    lateinit var validatinators: RegisterValidatinators

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
    private lateinit var binding: FragmentRegisterBinding

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

    private lateinit var registerResolver: RegisterResolver

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
            R.layout.fragment_register,
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

        registerResolver = RegisterResolver(requireActivity(), view)

        authViewModel.registerDataUpdate.removeObservers(this)
        authViewModel.registerDataUpdate.observe(
            this,
            Observer { registerResolver.resolve(it) }
        )
    }

    /**
     * Listens for floating action button events.
     */
    override fun onResume() {
        super.onResume()
        setSupportActionBarTitle(R.string.authenticate_label_register)
        floatingActionButtonOwner?.floatingActionButtonListener = this
    }

    /**
     * Removes any associated listeners.
     */
    override fun onPause() {
        super.onPause()
        if (floatingActionButtonOwner?.floatingActionButtonListener == this) {
            floatingActionButtonOwner?.floatingActionButtonListener = null
        }
    }

    // endregion Inherited methods

    // region Implemented methods

    /**
     * Implementation of [OnClickListener]. Submits the new account.
     */
    override fun onClick(v: View?) {
        when (v) {
            binding.loginBtn -> {
                authViewModel.registerDataUpdate.reset()
                resetErrors()
                Navigation.findNavController(v)
                    .navigate(R.id.action_register_to_log_in)
            }
        }
    }

    override fun onFloatingActionButtonClick(owner: FloatingActionButtonOwner) {
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

    // endregion Implemented methods

    // region Methods

    /**
     * Validates the form.
     */
    private fun validate(): Boolean {
        resetErrors()
        // return validatinators.registerValidatinator.validate(binding, options.clear())
        return true
    }

    private fun disableView() {
        // TODO disable controls (or at least the button)
    }

    private fun enableView() {
        // TODO enable controls
    }

    private fun resetErrors() {
        binding.usernameLayout.error = null
        binding.emailLayout.error = null
        binding.passwordLayout.error = null
        binding.confirmPasswordLayout.error = null
    }

    private fun resetView() {
        binding.usernameEdit.text = null
        binding.emailEdit.text = null
        binding.passwordEdit.text = null
        binding.confirmPasswordEdit.text = null
    }

    // endregion Methods

    // region Nested/inner classes

    private inner class RegisterResolver(activity: Activity, view: View) :
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

        @SuppressLint("SwitchIntDef")
        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
            when (event) {
                DISMISS_EVENT_ACTION, DISMISS_EVENT_SWIPE, DISMISS_EVENT_TIMEOUT ->
                    authViewModel.registerDataUpdate.reset()
            }
        }

        override fun onSuccess(update: SuccessUpdate<Void, Message>): Boolean {
            resetView()
            Navigation.findNavController(view).navigate(R.id.action_register_to_log_in)
            return true
        }

        override fun onFailure(update: FailureUpdate<Void, Message>): Boolean {
            // TODO This is exactly the same as ForgotPasswordFragment
            var handled = super.onFailure(update)
            if (!handled) {
                val remoteErrorBody =
                    update.data?.getParcelable<RemoteErrorBody>(KEY_REMOTE_ERROR_BODY)
                remoteErrorBody?.errors?.also { errors ->
                    errors.entries.forEach { error ->
                        view.findViewWithTag<TextInputLayout>(error.key)?.also { layout ->
                            layout.post {
                                layout.error = networkTranslator.translate(error.value.first())
                            }
                            handled = true
                        }
                    }
                }
            }

            if (!handled) {
                when (val updateException = update.e) {
                    is HttpStatusException -> {
                        // ???
                        // handled = true
                    }
                }
            }

            return handled
        }

        // endregion Inherited methods

    }

    // endregion Nested/inner classes

}
