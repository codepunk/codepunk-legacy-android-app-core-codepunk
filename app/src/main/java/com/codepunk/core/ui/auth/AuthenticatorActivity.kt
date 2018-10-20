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

import android.accounts.Account
import android.accounts.AccountManager
import android.accounts.AccountManager.*
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import com.codepunk.core.BuildConfig
import com.codepunk.core.R
import com.codepunk.core.data.model.auth.AccessToken
import com.codepunk.core.data.model.http.ResponseMessage
import com.codepunk.core.databinding.ActivityAuthenticatorBinding
import com.codepunk.core.lib.*
import com.codepunk.core.util.EXTRA_AUTHENTICATOR_INITIAL_ACTION
import com.codepunk.core.util.EXTRA_CONFIRM_CREDENTIALS
import com.codepunk.core.util.EXTRA_USERNAME
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject


/**
 * An Activity that handles all actions relating to creating, selecting, and authenticating
 * an account.
 */
class AuthenticatorActivity :
    AccountAuthenticatorAppCompatActivity(),
    HasSupportFragmentInjector {

    // region Properties

    /**
     * The Android account manage.
     */
    @Suppress("UNUSED")
    @Inject
    lateinit var accountManager: AccountManager

    /**
     * Performs dependency injection on fragments.
     */
    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    /**
     * A [ViewModelProvider.Factory] for creating [ViewModel] instances.
     */
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    /**
     * The binding for this activity.
     */
    private lateinit var binding: ActivityAuthenticatorBinding

    /**
     * An instance of [AuthViewModel] for managing account-related data.
     */
    @Suppress("UNUSED")
    private val authViewModel: AuthViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(AuthViewModel::class.java)
    }

    /**
     * The current username.
     */
//    private var username: String? = null

    /**
     * The initial action to use for navigation.
     */
    private var initialAction: Int = R.id.action_authenticating_to_authentication_options

    /**
     * TODO
     */
    private var confirmCredentials: Boolean = false

    // endregion Properties

    // region Lifecycle methods

    /**
     * Creates the content view and pulls values from the passed [Intent].
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_authenticator)

// TODO        username = intent.getStringExtra(EXTRA_USERNAME)
        confirmCredentials = intent.getBooleanExtra(EXTRA_CONFIRM_CREDENTIALS, false)

        initialAction = intent.getIntExtra(EXTRA_AUTHENTICATOR_INITIAL_ACTION, initialAction)
        Navigation.findNavController(this, R.id.account_nav_fragment).navigate(initialAction)

        authViewModel.authData.observe(this, Observer { update -> onAuthUpdate(update) })
    }

    // endregion Lifecycle methods

    // region Implemented methods

    /**
     * Implementation of [HasSupportFragmentInjector]. Returns a
     * [DispatchingAndroidInjector] that injects dependencies into fragments.
     */
    override fun supportFragmentInjector(): AndroidInjector<Fragment> =
        fragmentDispatchingAndroidInjector

    // endregion Implemented methods

    // region Methods

    fun onAuthUpdate(update: DataUpdate<ResponseMessage, AccessToken>) {
        Log.d("AuthenticatorActivity", "update=$update")
        // TODO Tie back to AccountManager?
        when (update) {
            is LoadingUpdate -> {
                // TODO Show spinner
            }
            is SuccessUpdate -> {
                // TODO Hide spinner
                update.result?.also { accessToken ->
                    val account = Account(
                        authViewModel.username,
                        BuildConfig.AUTHENTICATOR_ACCOUNT_TYPE)
                    if (!accountManager.addAccountExplicitly(
                            account,
                            accessToken.refreshToken,
                            null
                        )
                    ) {
                        accountManager.setPassword(account, accessToken.refreshToken)
                    }

                    accountAuthenticatorResult = Bundle().apply {
                        putString(KEY_ACCOUNT_NAME, authViewModel.username)
                        putString(KEY_ACCOUNT_TYPE, BuildConfig.AUTHENTICATOR_ACCOUNT_TYPE)
                        putString(KEY_AUTHTOKEN, accessToken.accessToken)
                        putString(KEY_PASSWORD, accessToken.refreshToken)
                    }

                    setResult(RESULT_OK)
                    finish()
                }
            }
            is FailureUpdate -> {
                // TODO Hide spinner
                // TODO React to failure
            }
        }
    }

    // endregion Methods

}
