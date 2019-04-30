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
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import com.codepunk.core.BuildConfig
import com.codepunk.core.BuildConfig.*
import com.codepunk.core.R
import com.codepunk.core.databinding.FragmentLogInBinding
import com.codepunk.core.domain.model.Authorization
import com.codepunk.core.domain.model.NetworkResponse
import com.codepunk.core.lib.hideSoftKeyboard
import com.codepunk.core.presentation.base.ContentLoadingProgressBarOwner
import com.codepunk.core.presentation.base.FloatingActionButtonOwner
import com.codepunk.core.util.NetworkTranslator
import com.codepunk.core.util.setSupportActionBarTitle
import com.codepunk.doofenschmirtz.util.loginator.FormattingLoginator
import com.codepunk.doofenschmirtz.util.taskinator.DataUpdate
import com.codepunk.punkubator.util.validatinator.Validatinator
import com.codepunk.punkubator.util.validatinator.Validatinator.Options
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

/**
 * A [Fragment] used to log into an existing account.
 */
class LogInFragment :
    Fragment(),
    View.OnClickListener,
    OnAccountsUpdateListener {

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
     * A set of authorization [Validatinator]s for validating the form.
     */
    @Inject
    lateinit var validatinators: LogInValidatinators

    /**
     * An instance of [NetworkTranslator] for translating messages from the network.
     */
    @Inject
    lateinit var networkTranslator: NetworkTranslator

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

    /**
     * Listens for floating action button click events.
     */
    override fun onResume() {
        super.onResume()
        setSupportActionBarTitle(R.string.authenticate_label_log_in)
        floatingActionButtonOwner?.floatingActionButton?.setOnClickListener(this)
        accountManager.addOnAccountsUpdatedListener(
            this,
            null,
            true
        )
    }

    override fun onPause() {
        super.onPause()
        accountManager.removeOnAccountsUpdatedListener(this)
    }

    // endregion Lifecycle methods

    // region Inherited methods

    /**
     * Sets up form information and event listeners.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.createBtn.setOnClickListener(this)

        arguments?.apply {
            // TODO This? Or Bundle Key? (See below)
            if (containsKey(EXTRA_USERNAME)) {
                binding.usernameOrEmailEdit.setText(getString(EXTRA_USERNAME))
            }

            if (containsKey(KEY_USERNAME)) {
                binding.usernameOrEmailEdit.setText(getString(KEY_USERNAME))
            }
        }

        authViewModel.authorizationDataUpdate.observe(
            this,
            Observer { update -> onAuthorizationUpdate(update) }
        )

        // If we've arrive here from a successful registration and we passed a network response
        // argument, show a snackbar with the associated message
        val networkResponse: NetworkResponse? = arguments?.getParcelable(KEY_NETWORK_RESPONSE)
        networkResponse?.also {
            val text = it.localizedMessage ?: getString(R.string.alert_success_exclam)
            Snackbar.make(view, text, Snackbar.LENGTH_INDEFINITE).apply {
                setAction(R.string.app_got_it) {
                    arguments?.remove(KEY_NETWORK_RESPONSE)
                }
            }.show()
        }
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
                    kotlin.Comparator { account1, account2 ->
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
     * Attempts to log in.
     */
    override fun onClick(v: View?) {
        when (v) {
            floatingActionButtonOwner?.floatingActionButton -> login()
            binding.createBtn ->
                Navigation.findNavController(v).navigate(R.id.action_log_in_to_create_account)
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

    // endregion Implemented methods

    // region Methods

    private fun login() {
        view?.hideSoftKeyboard()
        if (validate()) {
            authViewModel.authenticate(
                binding.usernameOrEmailEdit.text.toString(),
                binding.passwordEdit.text.toString()
            )
        }
    }

    private fun onAuthorizationUpdate(update: DataUpdate<NetworkResponse, Authorization>) {
        if (loginator.isLoggable(Log.DEBUG)) {
            loginator.d("update=$update")
        }
    }

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

    // endregion Methods

    // region Nested/inner classes

    // TODO TEMP

    // endregion Nested/inner classes

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
