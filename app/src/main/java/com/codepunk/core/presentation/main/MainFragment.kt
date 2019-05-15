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
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import androidx.core.widget.ContentLoadingProgressBar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.codepunk.core.BuildConfig
import com.codepunk.core.BuildConfig.ACTION_SETTINGS
import com.codepunk.core.BuildConfig.KEY_INTENT
import com.codepunk.core.R
import com.codepunk.core.databinding.FragmentMainBinding
import com.codepunk.core.domain.model.User
import com.codepunk.core.domain.session.Session
import com.codepunk.core.domain.session.SessionManager
import com.codepunk.core.presentation.base.ContentLoadingProgressBarOwner
import com.codepunk.doofenschmirtz.util.loginator.FormattingLoginator
import com.codepunk.doofenschmirtz.util.resourceinator.*
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

// region Constants

/**
 * Request code for manually authenticating the user when [SessionManager] encounters a problem.
 */
private const val AUTHENTICATE_REQUEST_CODE = 1

// endregion Constants

/**
 * The main [Fragment] for the app.
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
     * The application [FormattingLoginator].
     */
    @Inject
    lateinit var loginator: FormattingLoginator

    /**
     * The binding for this fragment.
     */
    private lateinit var binding: FragmentMainBinding

    /**
     * The content loading [ContentLoadingProgressBar] belonging to this fragment's activity.
     */
    private val contentLoadingProgressBar: ContentLoadingProgressBar? by lazy {
        (activity as? ContentLoadingProgressBarOwner)?.contentLoadingProgressBar
    }

    private lateinit var sessionResolver: SessionResolver

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
                    when (data?.getParcelableExtra<Account>(BuildConfig.KEY_ACCOUNT)) {
                        null -> {
                            // TODO Show error message then finish? OR show the msg in openSession activity dismiss?
                            // requireActivity().finish()
                        }
                        else -> sessionManager.getSession(silentMode = false, refresh = true)
                    }
                }
                Activity.RESULT_CANCELED -> {
                    // TODO Not sure if this is absolutely necessary but without it, resolve
                    // doesn't seem to be called when coming back from AuthenticateActivity
                    sessionManager.sessionLiveResource.removeObservers(this)
                    sessionManager.sessionLiveResource.observe(
                        this,
                        Observer { sessionResolver.resolve(it) }
                    )
                }
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

        sessionResolver = SessionResolver(view)

        binding.logInOutBtn.setOnClickListener(this)

        sessionManager.sessionLiveResource.observe(
            this,
            Observer { sessionResolver.resolve(it) }
        )

        // Try to silently obtain a session now
        if (savedInstanceState == null) {
            sessionManager.getSession(silentMode = true, refresh = true)
        }
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
                    null -> sessionManager.getSession(silentMode = false, refresh = true)
                    else -> sessionManager.closeSession(true)
                }
            }
        }
    }

    // endregion Implemented methods

    // region Nested/inner classes

    private inner class SessionResolver(view: View) :
        ResourceResolvinator<User, Session>(view) {

        override fun resolve(resource: Resource<User, Session>) {
            when (resource) {
                is ProgressResource -> contentLoadingProgressBar?.show()
                else -> contentLoadingProgressBar?.hide()
            }
            super.resolve(resource)
        }

        override fun onPending(resource: PendingResource<User, Session>): Boolean {
            binding.text1.text = null
            binding.logInOutBtn.isEnabled = true
            binding.logInOutBtn.setText(R.string.main_log_in)
            binding.logInOutBtn.visibility = View.VISIBLE
            return true
        }

        override fun onProgress(resource: ProgressResource<User, Session>): Boolean {
            binding.text1.text = null
            binding.logInOutBtn.isEnabled = false
            binding.logInOutBtn.setText(R.string.main_logging_in)
            binding.logInOutBtn.visibility = View.VISIBLE
            return true
        }

        override fun onSuccess(resource: SuccessResource<User, Session>): Boolean {
            val user = resource.result?.user
            binding.text1.text = when (user) {
                null -> getString(R.string.hello)
                else -> getString(R.string.hello_user, user.username)
            }
            binding.logInOutBtn.isEnabled = true
            binding.logInOutBtn.setText(R.string.main_log_out)
            binding.logInOutBtn.visibility = View.INVISIBLE
            return true
        }

        override fun onFailure(resource: FailureResource<User, Session>): Boolean {
            var handled = super.onFailure(resource)
            if (!handled) {
                // Get (and remove) KEY_INTENT from data as we're "consuming" the intent here
                val data = resource.data
                val intent: Intent? = data?.getParcelable(KEY_INTENT)
                intent?.run {
                    data.remove(KEY_INTENT)
                    startActivityForResult(this, AUTHENTICATE_REQUEST_CODE)
                } ?: run {
                    binding.text1.text = null
                    binding.logInOutBtn.isEnabled = true
                    binding.logInOutBtn.setText(R.string.main_log_in)
                    binding.logInOutBtn.visibility = View.VISIBLE
                }
                handled = true
            }
            return handled
        }
    }

    // endregion Nested/inner classes

}
