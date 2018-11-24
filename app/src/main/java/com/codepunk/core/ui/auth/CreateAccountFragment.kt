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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import com.codepunk.core.BuildConfig.KEY_RESPONSE_MESSAGE
import com.codepunk.core.R
import com.codepunk.core.data.model.auth.Authorization
import com.codepunk.core.data.model.http.ResponseMessage
import com.codepunk.core.databinding.FragmentCreateAccountBinding
import com.codepunk.core.lib.DataUpdate
import com.codepunk.core.lib.FailureUpdate
import com.codepunk.core.lib.SimpleDialogFragment
import com.codepunk.core.lib.hideSoftKeyboard
import com.codepunk.punkubator.util.validatinator.*
import com.codepunk.punkubator.util.validatinator.Validatinator.Options
import com.codepunk.punkubator.util.validatinatorold.TextInputLayoutValidatinator
import com.google.android.material.textfield.TextInputLayout
import dagger.android.support.AndroidSupportInjection
import java.io.IOException
import java.util.regex.Pattern
import javax.inject.Inject

/**
 * A [Fragment] used to add a new account.
 */
class CreateAccountFragment :
    Fragment(),
    View.OnClickListener {

    // region Properties

    /**
     * The injected [ViewModelProvider.Factory] that we will use to get an instance of
     * [AuthViewModel].
     */
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var authValidatinators: AuthValidatinators

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

    //    private val validatinatorMap = LinkedHashMap<TextInputLayout, Validatinator<CharSequence?>>()
    private val validatinatorMap = LinkedHashMap<TextInputLayout, TextInputLayoutValidatinator?>()

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

        /*
        validatinatorMap.put(binding.usernameLayout, authValidatinators.usernameValidatinator)
        validatinatorMap.put(binding.emailLayout, authValidatinators.emailValidatinator)
        */

        validatinatorMap[binding.usernameLayout] =
                authValidatinators.usernameTextInputLayoutValidatinator
        validatinatorMap[binding.emailLayout] =
                authValidatinators.emailTextInputLayoutValidatinator
        validatinatorMap[binding.givenNameLayout] = null
        validatinatorMap[binding.familyNameLayout] = null
        validatinatorMap[binding.passwordLayout] = null
        validatinatorMap[binding.confirmPasswordLayout] = null

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

        authViewModel.authorizationDataUpdate.observe(
            this,
            Observer { onAuthorizationUpdate(it) }
        )
    }

    /**
     * Validates the form.
     */
    private fun validate(): Boolean {

        val username = requireContext().getString(R.string.validation_attribute_username)

        val requiredValidatinator = RequiredValidatinator.Builder()
            .context(requireContext())
            .inputName(username)
            .build()

        val wordCharacterValidatinator =
            PatternValidatinator.Builder(Pattern.compile("\\w+"))
                .context(requireContext())
                .inputName(username)
                .invalidMessage { _, _ ->
                    resources.getString(R.string.validation_word_character_pattern, username)
                }
                .build()

        val maxLengthValidatinator: MaxLengthValidatinator =
            MaxLengthValidatinator.Builder(64)
                .context(requireContext())
                .inputName(username)
                .build()

        val usernameValidatinator: ValidatinatorSet<CharSequence?> =
            ValidatinatorSet.Builder<CharSequence?>()
                .context(requireContext())
                .inputName(username)
                .add(
                    requiredValidatinator,
                    wordCharacterValidatinator,
                    maxLengthValidatinator
                )
                .processAll(true)
                .build()

        val options = Options().apply {
            requestMessage = true
            requestTrace = true
        }
        val valid = usernameValidatinator.validate(binding.usernameEdit.text, options)

        binding.usernameLayout.error = when (valid) {
            true -> null
            false -> options.outMessage
        }

        return false // valid
    }

    // endregion Inherited methods

    // region Implemented methods

    /**
     * Submits the new account.
     */
    override fun onClick(v: View?) {
        with(binding) {
            when (v) {
                createBtn -> {
                    v.hideSoftKeyboard()
                    if (validate()) {
                        authViewModel.register(
                            usernameEdit.text.toString(),
                            emailEdit.text.toString(),
                            givenNameEdit.text.toString(),
                            familyNameEdit.text.toString(),
                            passwordEdit.text.toString(),
                            confirmPasswordEdit.text.toString()
                        )
                    }
                }
                loginBtn -> {
                    Navigation.findNavController(v).navigate(R.id.action_create_account_to_log_in)
                }
            }
        }
    }

    // endregion Implemented methods

    // region Methods

    private fun onAuthorizationUpdate(update: DataUpdate<ResponseMessage, Authorization>) {
        /*
        setControlsEnabled(update !is ProgressUpdate)
        */
        when (update) {
            is FailureUpdate -> {
                val responseMessage: ResponseMessage? =
                    update.data?.getParcelable(KEY_RESPONSE_MESSAGE)

                // TODO Make this a snackbar (but only if IOException?)
                val message: CharSequence = when (update.e) {
                    is IOException -> "Could not connect to the network."
                    else -> getString(R.string.authenticator_error_create_account)
                }
                SimpleDialogFragment.Builder(requireContext())
                    .setTitle(R.string.account_label_create_account)
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok)
                    .build()
                    .show(requireFragmentManager(), AUTHENTICATION_FAILURE_DIALOG_FRAGMENT_TAG)
            }
        }
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
