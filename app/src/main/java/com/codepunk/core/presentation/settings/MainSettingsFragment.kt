/*
 * Copyright (C) 2018 Codepunk, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.codepunk.core.presentation.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.navigation.Navigation
import androidx.preference.Preference
import com.codepunk.core.BuildConfig.*
import com.codepunk.core.R
import com.codepunk.core.domain.model.User
import com.codepunk.core.domain.session.Session
import com.codepunk.core.domain.session.SessionManager
import com.codepunk.core.lib.AlertDialogFragment
import com.codepunk.core.lib.AlertDialogFragment.Companion.RESULT_POSITIVE
import com.codepunk.doofenschmirtz.preference.TwoTargetSwitchPreference
import com.codepunk.doofenschmirtz.util.resourceinator.Resource
import com.codepunk.doofenschmirtz.util.resourceinator.SuccessResource
import com.codepunk.doofenschmirtz.util.startLaunchActivity
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

// region Constants

/**
 * The request code used by the developer password dialog fragment.
 */
private const val DEVELOPER_PASSWORD_REQUEST_CODE = 0

/**
 * The request code used by the confirm log out dialog fragment.
 */
private const val CONFIRM_LOG_OUT_REQUEST_CODE = 1

/**
 * The request code used by the disable developer options dialog fragment.
 */
private const val DISABLE_DEVELOPER_OPTIONS_REQUEST_CODE = 2

/**
 * The total number of clicks required to unlock developer options.
 */
private const val DEV_OPTS_CLICKS_TO_UNLOCK: Int = 7

/**
 * The number of clicks remaining at which to show a [Toast] message.
 */
private const val DEV_OPTS_CLICKS_REMAINING_TOAST = 3

// endregion Constants

/**
 * A preference fragment that displays the main settings available to the user.
 */
class MainSettingsFragment :
    AbsSettingsFragment(),
    Preference.OnPreferenceChangeListener,
    Preference.OnPreferenceClickListener,
    AlertDialogFragment.AlertDialogFragmentListener {

    // region Properties

    /**
     * The [SessionManager] for tracking user session.
     */
    @Suppress("UNUSED")
    @Inject
    lateinit var sessionManager: SessionManager

    /**
     * The developer options preference.
     */
    private val developerOptionsPreference by lazy {
        findPreference(PREF_KEY_DEVELOPER_OPTIONS_ENABLED) as TwoTargetSwitchPreference
    }

    /**
     * The log out preference.
     */
    private val logOutPreference by lazy {
        findPreference(PREF_KEY_LOG_OUT)
    }

    /**
     * The about preference.
     */
    private val aboutPreference by lazy {
        findPreference(PREF_KEY_ABOUT)
    }

    /**
     * The number of clicks remaining to unlock developer options.
     */
    private var clicksToUnlockDeveloperOptions = 0

    /**
     * Indicates whether developer options are enabled.
     */
    private var isDeveloperOptionsEnabled: Boolean = false

    /**
     * Indicates whether developer options are unlocked (i.e. visible).
     */
    private var isDeveloperOptionsUnlocked: Boolean = false

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
     * Saves this fragment's instance state.
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_DEVELOPER_OPTIONS_CLICKS_REMAINING, clicksToUnlockDeveloperOptions)
    }

    // endregion Lifecycle methods

    // region Inherited methods

    /**
     * Called when the value of shared preferences changes.
     */
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            PREF_KEY_DEVELOPER_OPTIONS_UNLOCKED -> updatePreferenceScreen()
            PREF_KEY_DEVELOPER_OPTIONS_AUTHENTICATED_HASH -> updatePreferenceScreen()
            PREF_KEY_CURRENT_ACCOUNT_NAME -> updatePreferenceScreen()
        }
    }

    /*
    /**
     * Processes the results of dialogs launched by preferences in this fragment.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            DEVELOPER_PASSWORD_REQUEST_CODE -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        data?.run {
                            updateDeveloperOptions(
                                true,
                                getStringExtra(EXTRA_DEVELOPER_OPTIONS_PASSWORD_HASH)
                            )
                        }
                        Toast.makeText(
                            context,
                            R.string.settings_developer_options_now_a_developer,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }
    */

    /**
     * Sets listeners and connects to the [ViewModel].
     */
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_main, rootKey)
        requireActivity().title = preferenceScreen.title
        aboutPreference.onPreferenceClickListener = this
        logOutPreference.onPreferenceClickListener = this
        developerOptionsPreference.onPreferenceClickListener = this
        developerOptionsPreference.onPreferenceChangeListener = this

        val versionName = context
            ?.packageManager
            ?.getPackageInfo(requireContext().packageName, 0)
            ?.versionName
        aboutPreference.summary = when (versionName) {
            null -> ""
            else -> getString(R.string.settings_about_summary, versionName)
        }

        clicksToUnlockDeveloperOptions = when {
            savedInstanceState != null ->
                savedInstanceState.getInt(
                    KEY_DEVELOPER_OPTIONS_CLICKS_REMAINING,
                    DEV_OPTS_CLICKS_TO_UNLOCK
                )
            sharedPreferences.getBoolean(PREF_KEY_DEVELOPER_OPTIONS_UNLOCKED, false) -> 0
            else -> DEV_OPTS_CLICKS_TO_UNLOCK
        }

        // Set initial preference visibility
        updatePreferenceScreen()
        if (sessionManager.session == null) {
            preferenceScreen.removePreference(logOutPreference)
        }

        sessionManager.sessionLiveResource.observe(this, Observer { onSession(it) })
    }

    // endregion Inherited methods

    // region Implemented methods

    /**
     * Implementation of [Preference.OnPreferenceChangeListener]. Delays turning on/off of
     * developer options until the result of associated dialogs.
     */
    override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
        return when (preference) {
            developerOptionsPreference -> {
                val enabled = newValue as Boolean
                if (enabled) {
                    showDeveloperPasswordDialogFragment()
                    false
                } else {
                    requireFragmentManager().findFragmentByTag(CONFIRM_LOG_OUT_FRAGMENT_TAG)
                        ?: AlertDialogFragment.showDialogFragmentForResult(
                            this,
                            DISABLE_DEVELOPER_OPTIONS_REQUEST_CODE,
                            DISABLE_DEVELOPER_OPTIONS_DIALOG_FRAGMENT_TAG
                        )
                    false
                }
            }
            else -> {
                true
            }
        }
    }

    /**
     * Implementation of [Preference.OnPreferenceClickListener]. Handles preference click events.
     */
    override fun onPreferenceClick(preference: Preference?): Boolean {
        return when (preference) {
            aboutPreference -> {
                when {
                    clicksToUnlockDeveloperOptions > 1 -> {
                        clicksToUnlockDeveloperOptions--
                        onStepsToUnlockDeveloperOptionsChange(clicksToUnlockDeveloperOptions)
                    }
                    clicksToUnlockDeveloperOptions == 1 -> {
                        clicksToUnlockDeveloperOptions = 0
                        updateDeveloperOptions(true)
                    }
                    else -> onRedundantUnlockRequest()
                }
                false
            }
            logOutPreference -> {
                requireFragmentManager().findFragmentByTag(CONFIRM_LOG_OUT_FRAGMENT_TAG)
                    ?: AlertDialogFragment.showDialogFragmentForResult(
                        this,
                        CONFIRM_LOG_OUT_REQUEST_CODE,
                        CONFIRM_LOG_OUT_FRAGMENT_TAG
                    )
                true
            }
            developerOptionsPreference -> {
                when {
                    isDeveloperOptionsEnabled -> {
                        val activity = requireActivity()
                        val navController =
                            Navigation.findNavController(activity, R.id.settings_nav_fragment)
                        navController.navigate(R.id.action_main_to_developer_options)
                    }
                    isDeveloperOptionsUnlocked ->
                        showDeveloperPasswordDialogFragment()
                    else -> { /* No action */
                    }
                }
                true
            }
            else -> {
                false
            }
        }
    }

    override fun onBuildAlertDialog(
        fragment: AlertDialogFragment,
        requestCode: Int,
        builder: AlertDialog.Builder
    ) {
        when (requestCode) {
            CONFIRM_LOG_OUT_REQUEST_CODE -> {
                val appName = getString(R.string.app_name)
                builder
                    .setTitle(R.string.settings_log_out_dialog_title)
                    .setMessage(getString(R.string.settings_log_out_dialog_message, appName))
                    .setPositiveButton(android.R.string.ok, fragment)
                    .setNegativeButton(android.R.string.cancel, fragment)
            }
            DISABLE_DEVELOPER_OPTIONS_REQUEST_CODE -> {
                builder
                    .setTitle(R.string.settings_developer_options_disable_dialog_title)
                    .setMessage(R.string.settings_developer_options_disable_dialog_message)
                    .setPositiveButton(android.R.string.ok, fragment)
                    .setNegativeButton(android.R.string.cancel, fragment)
            }
        }
    }

    override fun onDialogResult(
        fragment: AlertDialogFragment,
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        when (requestCode) {
            CONFIRM_LOG_OUT_REQUEST_CODE -> {
                when (resultCode) {
                    RESULT_POSITIVE -> sessionManager.closeSession(true)
                }
            }
            DEVELOPER_PASSWORD_REQUEST_CODE -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        data?.also {
                            updateDeveloperOptions(
                                true,
                                it.getStringExtra(EXTRA_DEVELOPER_OPTIONS_PASSWORD_HASH)
                            )
                        }
                        Toast.makeText(
                            context,
                            R.string.settings_developer_options_now_a_developer,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            DISABLE_DEVELOPER_OPTIONS_REQUEST_CODE -> {
                when (resultCode) {
                    RESULT_POSITIVE -> {
                        updateDeveloperOptions(true)
                        // TODO Set remote environment to "Production"
                        requireContext().startLaunchActivity()
                    }
                }
            }
        }
    }

    // endregion Implemented methods

    // region Methods

    /**
     * Handles when the about preference is clicked when developer options have already been
     * unlocked.
     */
    private fun onRedundantUnlockRequest() {
        Toast.makeText(
            context,
            R.string.settings_developer_options_redundant_show_request,
            Toast.LENGTH_SHORT
        ).show()
    }

    /**
     * Handles when the number of clicks remaining to unlock developer options changes.
     */
    private fun onStepsToUnlockDeveloperOptionsChange(steps: Int) {
        if (steps in 1..DEV_OPTS_CLICKS_REMAINING_TOAST) {
            Toast.makeText(
                context,
                resources.getQuantityString(
                    R.plurals.settings_developer_options_steps_from_unlocking,
                    steps,
                    steps
                ),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // TODO Consolidate these yes/no dialogs

    /**
     * Shows the developer password dialog.
     */
    private fun showDeveloperPasswordDialogFragment() {
        requireFragmentManager().findFragmentByTag(DEVELOPER_PASSWORD_DIALOG_FRAGMENT_TAG)
            ?: DeveloperOptionsPasswordDialogFragment.showDialogFragmentForResult(
                this,
                DEVELOPER_PASSWORD_REQUEST_CODE,
                DEVELOPER_PASSWORD_DIALOG_FRAGMENT_TAG
            )
    }

    /**
     * Updates whether the developer options preference has been unlocked as well as whether
     * the user has authenticated themselves as a developer. The [onSharedPreferenceChanged]
     * method will catch changes to these preferences and update the corresponding LiveData
     * accordingly.
     */
    private fun updateDeveloperOptions(unlocked: Boolean, hash: String? = null) {
        val enabled: Boolean = (unlocked && hash != null)
        sharedPreferences
            .edit()
            .putBoolean(PREF_KEY_DEVELOPER_OPTIONS_UNLOCKED, unlocked)
            .putBoolean(PREF_KEY_DEVELOPER_OPTIONS_ENABLED, enabled)
            .putString(
                PREF_KEY_DEVELOPER_OPTIONS_AUTHENTICATED_HASH,
                if (unlocked) hash else null
            )
            .apply()
    }

    /**
     * Updates the preference screen based on the developer options state.
     */
    private fun updatePreferenceScreen() {
        isDeveloperOptionsUnlocked =
            sharedPreferences.getBoolean(PREF_KEY_DEVELOPER_OPTIONS_UNLOCKED, false)
        isDeveloperOptionsEnabled = when {
            !isDeveloperOptionsUnlocked -> false
            sharedPreferences.getString(
                PREF_KEY_DEVELOPER_OPTIONS_AUTHENTICATED_HASH,
                null
            ) == null -> false
            else -> true
        }
        developerOptionsPreference.isChecked =
            isDeveloperOptionsUnlocked && isDeveloperOptionsEnabled
        if (isDeveloperOptionsUnlocked) {
            preferenceScreen.addPreference(developerOptionsPreference)
        } else {
            preferenceScreen.removePreference(developerOptionsPreference)
        }

        sessionManager.session?.run {
            logOutPreference.summary = accountName
            preferenceScreen.addPreference(logOutPreference)
        } ?: preferenceScreen.removePreference(logOutPreference)
    }

    /**
     * Adds or removes the log out preference based on session state.
     */
    private fun onSession(resource: Resource<User, Session>) {
        when (resource) {
            is SuccessResource -> {
                logOutPreference.summary = resource.result?.accountName
                preferenceScreen.addPreference(logOutPreference)
            }
            else -> preferenceScreen.removePreference(logOutPreference)
        }
    }

    // endregion Methods

    // region Companion object

    companion object {

        // region Properties

        /**
         * The fragment tag to use for the confirm log out dialog fragment.
         */
        @JvmStatic
        private val CONFIRM_LOG_OUT_FRAGMENT_TAG =
            MainSettingsFragment::class.java.name + ".CONFIRM_LOG_OUT_DIALOG"

        /**
         * The fragment tag to use for the developer password dialog fragment.
         */
        @JvmStatic
        private val DEVELOPER_PASSWORD_DIALOG_FRAGMENT_TAG =
            MainSettingsFragment::class.java.name + ".DEVELOPER_PASSWORD_DIALOG"

        /**
         * The fragment tag to use for the disable developer options dialog fragment.
         */
        @JvmStatic
        private val DISABLE_DEVELOPER_OPTIONS_DIALOG_FRAGMENT_TAG =
            MainSettingsFragment::class.java.name + ".DISABLE_DEVELOPER_OPTIONS_DIALOG"

        // endregion Properties

    }

    // endregion Companion object

}
