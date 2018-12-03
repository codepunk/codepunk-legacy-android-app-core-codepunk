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
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import com.codepunk.core.BuildConfig
import com.codepunk.core.BuildConfig.*
import com.codepunk.core.R
import com.codepunk.core.data.task.*
import com.codepunk.core.databinding.FragmentMainBinding
import com.codepunk.core.lib.*
import com.codepunk.core.session.Session
import com.codepunk.core.session.SessionManager
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

/**
 * Request code for manually authenticating the user when [SessionManager] encounters a problem.
 */
const val AUTHENTICATE_REQUEST_CODE = 1

/**
 * A constant that indicates logged out state.
 */
private const val STATE_LOGGED_OUT = 0

/**
 * A constant that indicates logging in state.
 */
private const val STATE_LOGGING_IN = 1

/**
 * A constant that indicates logged in state.
 */
private const val STATE_LOGGED_IN = 2

/**
 * A simple [Fragment] subclass.
 */
class MainFragment :
    Fragment(),
    View.OnClickListener {

    // region Properties

    /**
     * The application [SharedPreferences].
     */
    @Inject
    lateinit var sharedPreferences: SharedPreferences

    /**
     * The application [SessionManager].
     */
    @Inject
    lateinit var sessionManager: SessionManager

    /**
     * The binding for this fragment.
     */
    private lateinit var binding: FragmentMainBinding

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

    /**
     * Reacts to the result from AuthenticateActivity.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            AUTHENTICATE_REQUEST_CODE -> when (resultCode) {
                Activity.RESULT_OK -> {
                    val account: Account? = data?.getParcelableExtra(BuildConfig.KEY_ACCOUNT)
                    when (account) {
                        null -> {
                            // TODO Show error message then finish? OR show the msg in authenticate activity dismiss?
                            // requireActivity().finish()
                        }
                        else -> sessionManager.openSession(true, true)
                    }
                }
                Activity.RESULT_CANCELED -> updateUI(STATE_LOGGED_OUT)
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    /**
     * Creates the options menu.
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu, inflater)
    }

    /**
     * Handles menu selections.
     */
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.menu_settings -> {
                startActivity(Intent(ACTION_SETTINGS))
                true
            }
            else -> super.onOptionsItemSelected(item)
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

    /**
     * Handles click events.
     */
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
        when (update) {
            is PendingUpdate -> updateUI(STATE_LOGGED_OUT)
            is ProgressUpdate -> updateUI(STATE_LOGGING_IN)
            is SuccessUpdate -> updateUI(STATE_LOGGED_IN, update.result?.user?.givenName)
            is FailureUpdate -> {
                if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    val intent: Intent? = update.data?.getParcelable(KEY_INTENT) as? Intent
                    intent?.run {
                        updateUI(STATE_LOGGING_IN)
                        startActivityForResult(intent, AUTHENTICATE_REQUEST_CODE)
                    } ?: updateUI(STATE_LOGGED_OUT)
                }
            }
        }
    }

    /**
     * Updates the UI.
     */
    private fun updateUI(state: Int, givenName: String? = null) {
        with(binding) {
            text1.text = if (givenName.isNullOrEmpty()) getString(R.string.hello)
            else getString(R.string.hello_user, givenName)
            logInOutBtn.setText(
                if (state == STATE_LOGGED_IN) R.string.main_log_out else R.string.main_log_in
            )
            logInOutBtn.isEnabled = (state != STATE_LOGGING_IN)
        }
    }

    // endregion Methods

}
