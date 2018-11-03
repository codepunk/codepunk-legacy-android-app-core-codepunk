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
import android.accounts.AccountManager.KEY_ACCOUNT_NAME
import android.accounts.AccountManager.KEY_PASSWORD
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.navigation.ui.setupActionBarWithNavController
import com.codepunk.core.BuildConfig.*
import com.codepunk.core.R
import com.codepunk.core.data.model.auth.AccessToken
import com.codepunk.core.data.model.http.ResponseMessage
import com.codepunk.core.databinding.ActivityAuthenticateBinding
import com.codepunk.core.lib.*
import com.codepunk.core.util.EXTRA_AUTHENTICATOR_INITIAL_ACTION
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

private const val NO_ACTION: Int = -1

/**
 * An Activity that handles all actions relating to creating, selecting, and authenticating
 * an account.
 */
class AuthenticateActivity :
    AccountAuthenticatorAppCompatActivity(),
    HasSupportFragmentInjector {

    // region Properties


    /**
     * The Android account manage.
     */
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
    private lateinit var binding: ActivityAuthenticateBinding

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
    private var initialAction: Int = NO_ACTION

    // endregion Properties

    // region Lifecycle methods

    /**
     * Creates the content view and pulls values from the passed [Intent].
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_authenticate)

        initialAction = intent.getIntExtra(EXTRA_AUTHENTICATOR_INITIAL_ACTION, initialAction)
        val navController = Navigation.findNavController(this, R.id.account_nav_fragment)
        if (initialAction == NO_ACTION) {
            setupActionBarWithNavController(navController)
        } else {
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.fragment_authenticate, true)
                .build()
            navController.navigate(initialAction, null, navOptions)
            navController.addOnNavigatedListener { _, destination ->
                title = destination.label
            }
        }

        authViewModel.accessTokenDataUpdate.observe(this, Observer { onAccessTokenUpdate(it) })
    }

    // endregion Lifecycle methods

    // region Inherited methods

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // TODO
    }

    override fun onSupportNavigateUp(): Boolean {
        return Navigation.findNavController(this, R.id.account_nav_fragment).navigateUp()
    }

    // endregion Inherited methods

    // region Implemented methods

    /**
     * Implementation of [HasSupportFragmentInjector]. Returns a
     * [DispatchingAndroidInjector] that injects dependencies into fragments.
     */
    override fun supportFragmentInjector(): AndroidInjector<Fragment> =
        fragmentDispatchingAndroidInjector

    // endregion Implemented methods

    // region Methods

    fun onAccessTokenUpdate(update: DataUpdate<ResponseMessage, AccessToken>) {
        when (update) {
            is ProgressUpdate -> {
                // TODO Loading dialog (show and hide)
            }
            is SuccessUpdate -> {
                update.data?.also { data ->
                    accountAuthenticatorResult = data
                    val account = Account(
                        data.getString(KEY_ACCOUNT_NAME),
                        AUTHENTICATOR_ACCOUNT_TYPE
                    )
                    accountManager.addOrUpdateAccount(account, data.getString(KEY_PASSWORD))
                    setResult(
                        RESULT_OK,
                        Intent().apply {
                            putExtra(
                                KEY_ACCOUNT_AUTHENTICATOR_RESULT,
                                accountAuthenticatorResult
                            )
                            putExtra(KEY_ACCOUNT, account)
                        }
                    )
                } ?: setResult(Activity.RESULT_CANCELED)
                finish()
            }
            is FailureUpdate -> {
                // TODO Error message(s)
            }
        }

        val response = when (update) {
            is SuccessUpdate -> update.response
            is FailureUpdate -> update.response
            else -> null
        }
        val httpStatus = when (response) {
            null -> null
            else -> HttpStatus.lookup(response.code())
        }
        Log.d(
            "AuthenticateActivity",
            "onAccessTokenUpdate: httpStatus=$httpStatus, update=$update"
        )
    }

    // endregion Methods

}
