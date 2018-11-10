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

package com.codepunk.core.ui.main

import android.accounts.Account
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.AsyncTask.Status
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.codepunk.core.BuildConfig
import com.codepunk.core.BuildConfig.KEY_INTENT
import com.codepunk.core.BuildConfig.PREF_KEY_CURRENT_ACCOUNT_NAME
import com.codepunk.core.R
import com.codepunk.core.databinding.FragmentMainBinding
import com.codepunk.core.lib.*
import com.codepunk.core.session.Session
import com.codepunk.core.session.SessionManager
import com.codepunk.core.ui.auth.AuthViewModel
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

/**
 * Request code for manually authenticating the user when [SessionManager] encounters a problem.
 */
const val AUTHENTICATE_REQUEST_CODE = 1

/**
 * A simple [Fragment] subclass.
 */
@Suppress("unused")
class MainFragment :
    Fragment(),
    View.OnClickListener {

    // region Properties

    /**
     * Performs dependency injection on fragments.
     */
    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    /**
     * The application [SharedPreferences].
     */
    @Inject
    lateinit var sharedPreferences: SharedPreferences

    /**
     * A [ViewModelProvider.Factory] for creating [ViewModel] instances.
     */
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    /**
     * The application [SessionManager].
     */
    @Inject
    lateinit var sessionManager: SessionManager

    /**
     * The binding for this fragment.
     */
    private lateinit var binding: FragmentMainBinding

    /**
     * An instance of [AuthViewModel] for managing account-related data.
     */
    private val mainViewModel: MainViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(MainViewModel::class.java)
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
            R.layout.fragment_main,
            container,
            false
        )
        return binding.root
    }

    // endregion Lifecycle methods

    // region Inherited methods

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            AUTHENTICATE_REQUEST_CODE -> when (resultCode) {
                AppCompatActivity.RESULT_OK -> {
                    val account: Account? = data?.getParcelableExtra(BuildConfig.KEY_ACCOUNT)
                    when (account) {
                        null -> {
                            // TODO Show error message then finish? OR show the msg in authenticate activity dismiss?
                            // requireActivity().finish()
                        }
                        else -> sessionManager.openSession(
                            true,
                            true
                        ) // TODO Is this duplicating a lot of logic?
                    }
                }
                Activity.RESULT_CANCELED -> updateUI(Status.PENDING)
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    /**
     * Updates the view.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.logInOutBtn.setOnClickListener(this)

        // If we have an account saved, try to silently log into it now
        if (savedInstanceState == null) {
            if (sharedPreferences.contains(PREF_KEY_CURRENT_ACCOUNT_NAME)) {
                sessionManager.openSession(true, true)
            }
        }

        sessionManager.observeSession(this, Observer { onSessionUpdate(it) })
    }

    // endregion Inherited methods

    // region Implemented methods

    override fun onClick(v: View?) {
        when (v) {
            binding.logInOutBtn -> {
                when (sessionManager.session) {
                    null -> sessionManager.openSession()
                    else -> sessionManager.closeSession(true)
                }
            }
        }
    }

    // endregion Implemented methods

    // region Methods

    private fun onSessionUpdate(update: DataUpdate<Void, Session>) {
        Log.d("MainFragment", "onSessionUpdate: update=$update")
        when (update) {
            is PendingUpdate -> updateUI(Status.PENDING)
            is ProgressUpdate -> updateUI(Status.RUNNING)
            is SuccessUpdate -> updateUI(Status.FINISHED, update.result?.user?.givenName)
            is FailureUpdate -> {
                val intent: Intent? = update.data?.getParcelable(KEY_INTENT) as? Intent
                intent?.run {
                    updateUI(Status.RUNNING)
                    startActivityForResult(intent, AUTHENTICATE_REQUEST_CODE)
                } ?: updateUI(Status.PENDING)
            }
        }
    }

    private fun updateUI(status: AsyncTask.Status, givenName: String? = null) {
        when (status) {
            Status.PENDING -> {
                binding.text1.setText(R.string.hello)
                binding.logInOutBtn.setText(R.string.main_log_in)
                binding.logInOutBtn.isEnabled = true
            }
            Status.RUNNING -> {
                binding.text1.setText(R.string.main_logging_in)
                binding.logInOutBtn.setText(R.string.main_log_in)
                binding.logInOutBtn.isEnabled = false
            }
            Status.FINISHED -> {
                binding.text1.text = when (givenName) {
                    null -> getString(R.string.hello)
                    else -> getString(R.string.hello_user, givenName)
                }
                binding.logInOutBtn.setText(R.string.main_log_out)
                binding.logInOutBtn.isEnabled = true
            }
        }
    }

    // endregion Methods

}
