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

package com.codepunk.core.ui.auth

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.codepunk.core.R
import com.codepunk.core.databinding.FragmentLogInBinding
import com.codepunk.core.ui.base.FormFragment
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

/**
 * A [Fragment] used to log into an existing account.
 */
class LogInFragment :
    FormFragment(),
    View.OnClickListener {

    // region Properties

    /**
     * The injected [ViewModelProvider.Factory] that we will use to get an instance of
     * [AuthViewModel].
     */
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

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
        with(binding) {
            addTextInputLayouts(emailLayout, passwordLayout)
            addRequiredFields(emailEdit, passwordEdit)
            loginBtn.setOnClickListener(this@LogInFragment)
        }
    }

    /**
     * Disables the create button when any required fields are missing.
     */
    override fun onRequiredFieldMissing(view: View) {
        binding.loginBtn.isEnabled = false
    }

    /**
     * Enables the create button when all required fields are filled in.
     */
    override fun onRequiredFieldsComplete() {
        binding.loginBtn.isEnabled = true
    }

    /**
     * Validates the form.
     */
    override fun validate(): Boolean {
        super.validate()
        with(binding) {
            return when {
                !Patterns.EMAIL_ADDRESS.matcher(emailEdit.text).matches() -> {
                    emailLayout.error = getString(R.string.authenticator_error_email)
                    false
                }
                TextUtils.isEmpty(passwordEdit.text) -> {
                    passwordLayout.error = getString(R.string.authenticator_error_password)
                    false
                }
                else -> true
            }
        }
    }
    // endregion Inherited methods

    // region Implemented methods

    /**
     * Attempts to log in.
     */
    override fun onClick(v: View?) {
        with(binding) {
            when (v) {
                loginBtn -> {
                    if (validate()) {
//                    authViewModel.username = usernameEdit.text.toString()
                        authViewModel.email = emailEdit.text.toString()
                        authViewModel.password = passwordEdit.text.toString()

                        authViewModel.authenticate(
                            emailEdit.text.toString(),
                            passwordEdit.text.toString()
                        )

                        /*
//                        if (validate()) {
                            authViewModel.authenticate(
//                                usernameEdit.text.toString(),
                                emailEdit.text.toString(),
                                passwordEdit.text.toString()
                            )
//                        }
                        */
                    }
                }
            }
        }
    }

    // endregion Implemented methods

}
