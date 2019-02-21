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

package com.codepunk.core.presentation.main

import android.accounts.Account
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import com.codepunk.core.BuildConfig
import com.codepunk.core.BuildConfig.ACTION_SETTINGS
import com.codepunk.core.BuildConfig.KEY_INTENT
import com.codepunk.core.databinding.FragmentMainBinding
import com.codepunk.core.domain.model.User
import com.codepunk.core.domain.session.Session
import com.codepunk.core.domain.session.SessionManager
import com.codepunk.core.R
import com.codepunk.doofenschmirtz.app.AlertDialogFragment
import com.codepunk.doofenschmirtz.util.taskinator.*
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
 * A constant that indicates a user that has not been activated.
 */
private const val STATE_INACTIVE = 3

/**
 * A simple [Fragment] subclass.
 */
class MainFragment :
    Fragment(),
    View.OnClickListener,
    AlertDialogFragment.OnBuildAlertDialogListener,
    DialogInterface.OnClickListener {

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

    /**
     * The Requires Validation dialog fragment.
     */
    private var requiresValidationDialogFragment: AlertDialogFragment? = null

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
     * Indicates that this fragment has an options menu.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        requiresValidationDialogFragment = requireFragmentManager().findFragmentByTag(
            REQUIRES_ACTIVATION_DIALOG_FRAGMENT_TAG
        ) as? AlertDialogFragment
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
                            // TODO Show error message then finish? OR show the msg in openSession activity dismiss?
                            // requireActivity().finish()
                        }
                        else -> sessionManager.getSession(false, true)
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

//        sessionManager2.authenticateUser().observe(this, Observer { onUserUpdate(it) })
//        sessionManager2.openSession(false).observeForever {
//            Log.d("MainFragment", "update=$it")
//        }

        sessionManager.observeSession(this, Observer { onSessionUpdate(it) })

        // If we have an account saved, try to silently log into it now
        if (savedInstanceState == null) {
            sessionManager.getSession(true, true)
            /*
            if (sharedPreferences.contains(PREF_KEY_CURRENT_ACCOUNT_NAME)) {
                sessionManager.openSession(true).observe(this, Observer {
                    onSessionUpdate(it)
                })
//                sessionManager.openSession(true, true)
            }
            */
        }

//        sessionManager.observeSession(this, Observer { onSessionUpdate(it) })
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
                    null -> sessionManager.getSession(false, true)
                    else -> sessionManager.closeSession(true)
                }
            }
        }
    }

    /**
     * Supplies arguments to the Requires Activation dialog.
     */
    override fun onBuildAlertDialog(fragment: AlertDialogFragment, builder: AlertDialog.Builder) {
        // TODO Can I somehow figure out the difference between having just registered
        // ("We sent you an activation code! Please check your e-mail.") and logging in to
        // inactive account? ("You need to activate your account. We sent you an activation code when you registered. Please check your e-mail.")
        builder.setTitle(R.string.authenticator_log_in)
            .setMessage(R.string.authenticator_sent_email)
            .setPositiveButton(android.R.string.ok, null)
            .setNeutralButton(R.string.authenticator_send_again, this)
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        TODO("not implemented")
    }

    // endregion Implemented methods

    // region Methods

    private fun onUserUpdate(update: DataUpdate<User, User>) {

    }

    private fun onSessionUpdate(update: DataUpdate<User, Session>) {
        when (update) {
            is PendingUpdate -> updateUI(STATE_LOGGED_OUT)
            is ProgressUpdate -> updateUI(STATE_LOGGING_IN)
            is SuccessUpdate -> {
                val user = update.result?.user
                when {
                    user == null -> updateUI(STATE_LOGGED_OUT)
                    user.active -> updateUI(STATE_LOGGED_IN, user)
                    else -> {
                        sessionManager.closeSession(true)
                        updateUI(STATE_INACTIVE)
                    }
                }
            }
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
    private fun updateUI(state: Int, user: User? = null) {
        when (state) {
            STATE_LOGGING_IN -> {
                binding.text1.text = null
                binding.logInOutBtn.isEnabled = false
                binding.logInOutBtn.setText(R.string.main_logging_in)
                binding.logInOutBtn.visibility = View.VISIBLE
            }
            STATE_LOGGED_IN -> {
                binding.text1.text = when (user) {
                    null -> getString(R.string.hello)
                    else -> getString(R.string.hello_user, user.givenName)
                }
                binding.logInOutBtn.isEnabled = true
                binding.logInOutBtn.setText(R.string.main_log_out)
                binding.logInOutBtn.visibility = View.INVISIBLE
            }
            STATE_INACTIVE -> {
                binding.text1.text = null
                binding.logInOutBtn.isEnabled = true
                binding.logInOutBtn.setText(R.string.main_log_in)
                binding.logInOutBtn.visibility = View.VISIBLE
                if (requiresValidationDialogFragment == null) {
                    requiresValidationDialogFragment = AlertDialogFragment.show(
                        REQUIRES_ACTIVATION_DIALOG_FRAGMENT_TAG,
                        this
                    )
                }
            }
            else -> {
                binding.text1.text = null
                binding.logInOutBtn.isEnabled = true
                binding.logInOutBtn.setText(R.string.main_log_in)
                binding.logInOutBtn.visibility = View.VISIBLE
            }
        }

        /*
        with(binding) {
            text1.text = user?.givenName?.let {
                getString(R.string.hello_user, it)
            }?.let {
                getString(R.string.hello)
            }
            logInOutBtn.setText(
                if (state == STATE_LOGGED_IN) R.string.main_log_out else R.string.main_log_in
            )
            logInOutBtn.isEnabled = (state != STATE_LOGGING_IN)
            logInOutBtn.visibility = if (state == STATE_LOGGED_IN) View.INVISIBLE else View.VISIBLE
        }
        */
    }

    // endregion Methods

    // region Companion object

    companion object {

        // region Properties

        private val REQUIRES_ACTIVATION_DIALOG_FRAGMENT_TAG =
            MainFragment::class.java.name + ".REQUIRES_ACTIVATION_DIALOG"

        // endregion Properties

    }

    // endregion Companion object

}
