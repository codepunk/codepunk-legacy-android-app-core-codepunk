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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import com.codepunk.core.BuildConfig.EXTRA_USERNAME
import com.codepunk.core.BuildConfig.KEY_RESPONSE_MESSAGE
import com.codepunk.core.R
import com.codepunk.core.data.remote.entity.RemoteAuthorization
import com.codepunk.core.data.remote.entity.RemoteNetworkResponse
import com.codepunk.doofenschmirtz.util.taskinator.DataUpdate
import com.codepunk.doofenschmirtz.util.taskinator.FailureUpdate
import com.codepunk.core.databinding.FragmentLogInBinding
import com.codepunk.core.lib.SimpleDialogFragment
import com.codepunk.core.lib.hideSoftKeyboard
import com.codepunk.doofenschmirtz.util.loginator.FormattingLoginator
import com.codepunk.punkubator.util.validatinator.Validatinator
import com.codepunk.punkubator.util.validatinator.Validatinator.Options
import dagger.android.support.AndroidSupportInjection
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

/**
 * A [Fragment] used to log into an existing account.
 */
class LogInFragment :
    Fragment(),
    View.OnClickListener {

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
    lateinit var validatinators: LogInValidatinators

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

        binding.loginBtn.setOnClickListener(this)
        binding.createBtn.setOnClickListener(this)

        arguments?.apply {
            if (containsKey(EXTRA_USERNAME)) {
                binding.usernameOrEmailEdit.setText(getString(EXTRA_USERNAME))
            }
        }

        authViewModel.authorizationDataUpdate.observe(
            this,
            Observer { onAuthorizationUpdate(it) }
        )
    }

    // region Implemented methods

    /**
     * Attempts to log in.
     */
    override fun onClick(v: View?) {
        with(binding) {
            when (v) {
                loginBtn -> {
                    v.hideSoftKeyboard()
                    if (validate()) {
                        authViewModel.authenticate(
                            usernameOrEmailEdit.text.toString(),
                            passwordEdit.text.toString()
                        )
                    }
                }
                createBtn -> {
                    Navigation.findNavController(v).navigate(R.id.action_log_in_to_create_account)
                }
            }
        }
    }

    // endregion Implemented methods

    // region Methods

    private fun onAuthorizationUpdate(update: DataUpdate<RemoteNetworkResponse, Response<RemoteAuthorization>>) {
        /*
        setControlsEnabled(update !is ProgressUpdate)
        */
        when (update) {
            is FailureUpdate -> {
                val remoteNetworkResponse: RemoteNetworkResponse? =
                    update.data?.getParcelable(KEY_RESPONSE_MESSAGE)
                if (loginator.isLoggable(Log.DEBUG)) {
                    loginator.d("remoteResponse=$remoteNetworkResponse")
                }

                // TODO Make this a snackbar (but only if IOException?)
                val message: CharSequence = when (update.e) {
                    is IOException -> "Could not connect to the network."
                    else -> getString(R.string.authenticator_error_invalid_credentials)
                }
                SimpleDialogFragment.Builder(requireContext())
                    .setTitle(R.string.account_label_log_in)
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok)
                    .build()
                    .show(requireFragmentManager(), AUTHENTICATION_FAILURE_DIALOG_FRAGMENT_TAG)
            }
        }
    }

    /**
     * Validates the form.
     */
    private fun validate(): Boolean {
        return validatinators.logInValidatinator.validate(
            binding,
            options.clear()
        )
    }

    // endregion Methods

    // region Companion object

    companion object {

        // region Properties

        /**
         * The fragment tag to use for the authentication failure dialog fragment.
         */
        @JvmStatic
        private val AUTHENTICATION_FAILURE_DIALOG_FRAGMENT_TAG =
            LogInFragment::class.java.name + ".AUTHENTICATION_FAILURE_DIALOG"

        // endregion Properties

    }

    // endregion Companion object

}
