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

package com.codepunk.core.ui.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.codepunk.core.BuildConfig.*
import com.codepunk.core.R
import com.codepunk.core.ui.developer.DeveloperOptionsPasswordDialogFragment
import com.codepunk.core.ui.developer.DisableDeveloperOptionsDialogFragment
import com.codepunk.core.ui.settings.DeveloperOptionsSettingsViewModel.DeveloperOptionsState
import com.codepunk.core.ui.settings.DeveloperOptionsSettingsViewModel.DeveloperOptionsState.*
import com.codepunk.doofenschmirtz.preference.TwoTargetSwitchPreference
import com.codepunk.doofenschmirtz.util.startLaunchActivity
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

// region Constants

/**
 * The request code used by the developer password dialog fragment.
 */
private const val DEVELOPER_PASSWORD_REQUEST_CODE = 0

/**
 * The request code used by the disable developer options dialog fragment.
 */
private const val DISABLE_DEVELOPER_OPTIONS_REQUEST_CODE = 1

/**
 * The total number of clicks required to unlock developer options.
 */
private const val DEV_OPTS_CLICKS_TO_UNLOCK: Int = 7

/**
 * The number of clicks remaining at which to show a [Toast] message.
 */
private const val DEV_OPTS_CLICKS_REMAINING_TOAST = 3

/**
 * The save state key for storing clicks remaining to unlock developer options.
 */
private const val SAVE_STATE_CLICKS_REMAINING = "clicksRemaining"

// endregion Constants

/**
 * A preference fragment that displays the main settings available to the user.
 */
class MainSettingsFragment :
    PreferenceFragmentCompat(),
    Preference.OnPreferenceChangeListener,
    Preference.OnPreferenceClickListener {

    // region Fields

    /**
     * The number of clicks remaining to unlock developer options.
     */
    private var clicksToUnlockDeveloperOptions = 0

    /**
     * A [ViewModelProvider.Factory] for creating [ViewModel] instances.
     */
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    /**
     * An instance of [MainSettingsViewModel] for managing developer options settings.
     */
    private val mainSettingsViewModel: MainSettingsViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory)
            .get(MainSettingsViewModel::class.java)
    }

    /**
     * The [ViewModel] that stores developer options-related preference data used by this fragment.
     */
    private val developerSettingsViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory)
            .get(DeveloperOptionsSettingsViewModel::class.java)
    }

    /**
     * The developer options preference.
     */
    private val developerOptionsPreference by lazy {
        findPreference(PREF_KEY_DEVELOPER_OPTIONS_ENABLED) as TwoTargetSwitchPreference
    }

    /**
     * The about preference.
     */
    private val aboutPreference by lazy {
        findPreference(PREF_KEY_ABOUT)
    }

    // endregion Fields

    // region Lifecycle methods

    /**
     * Injects dependencies into this fragment.
     */
    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    /**
     * Sets up this fragment.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        clicksToUnlockDeveloperOptions = when {
            savedInstanceState != null ->
                savedInstanceState.getInt(SAVE_STATE_CLICKS_REMAINING, DEV_OPTS_CLICKS_TO_UNLOCK)
            developerSettingsViewModel.developerOptionsUnlocked.value == true -> 0
            else -> DEV_OPTS_CLICKS_TO_UNLOCK
        }
    }

    /**
     * Saves this fragment's instance state.
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(SAVE_STATE_CLICKS_REMAINING, clicksToUnlockDeveloperOptions)
    }

    // endregion Lifecycle methods

    // region Inherited methods

    /**
     * Processes the results of dialogs launched by preferences in this fragment.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            DEVELOPER_PASSWORD_REQUEST_CODE -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        data?.run {
                            developerSettingsViewModel.updateDeveloperOptions(
                                true,
                                getStringExtra(EXTRA_DEVELOPER_OPTIONS_PASSWORD_HASH)
                            )
                        }
                    }
                }
            }
            DISABLE_DEVELOPER_OPTIONS_REQUEST_CODE -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        developerSettingsViewModel.updateDeveloperOptions(true)
                        // TODO Set remote environment to "Production"
                        requireContext().startLaunchActivity()
                    }
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    /**
     * Sets listeners and connects to the [ViewModel].
     */
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_main, rootKey)
        requireActivity().title = preferenceScreen.title
        aboutPreference.onPreferenceClickListener = this
        developerOptionsPreference.onPreferenceClickListener = this
        developerOptionsPreference.onPreferenceChangeListener = this

        with(mainSettingsViewModel) {
            appVersion.observe(
                this@MainSettingsFragment,
                Observer { version ->
                    aboutPreference.summary = getString(R.string.settings_about_summary, version)
                })
        }

        with(developerSettingsViewModel) {
            developerOptionsState.observe(
                this@MainSettingsFragment,
                Observer { state ->
                    onDeveloperOptionsStateChange(state ?: DeveloperOptionsState.LOCKED)
                })

            onDeveloperOptionsStateChange(
                developerOptionsState.value ?: DeveloperOptionsState.LOCKED
            )
        }
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
                    showDisableDeveloperOptionsDialogFragment()
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
                        developerSettingsViewModel.updateDeveloperOptions(true)
                    }
                    else -> onRedundantUnlockRequest()
                }
                false
            }
            developerOptionsPreference -> {
                when (developerSettingsViewModel.developerOptionsState.value) {
                    DeveloperOptionsState.ENABLED -> {
                        /*
                        startActivity(Intent(ACTION_SETTINGS).apply {
                            addCategory(CATEGORY_DEVELOPER)
                        })
                        */
                        val activity = requireActivity()
                        val navController =
                            Navigation.findNavController(activity, R.id.settings_nav_fragment)
                        navController.navigate(R.id.action_main_to_developer_options)
                        /*
                        if (activity.intent.categories?.contains(CATEGORY_DEVELOPER) == true) {
                            val navOptions = NavOptions.Builder()
                                .setPopUpTo(R.id.fragment_authenticate, true)
                                .build()
                            navController.navigate(
                                R.id.action_main_to_developer_options,
                                null,
                                navOptions
                            )
                            navController.addOnNavigatedListener { _, destination ->
                                activity.title = destination.label
                            }
                        }
                        */
                    }
                    DeveloperOptionsState.UNLOCKED ->
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

    // endregion Implemented methods

    // region Private methods

    /**
     * Updates the preference screen based on the state of developer options (i.e.
     * [LOCKED], [UNLOCKED], or [ENABLED].)
     */
    private fun onDeveloperOptionsStateChange(state: DeveloperOptionsState) {
        developerOptionsPreference.isChecked = (state == DeveloperOptionsState.ENABLED)
        when (state) {
            DeveloperOptionsState.LOCKED ->
                preferenceScreen.removePreference(developerOptionsPreference)
            else -> preferenceScreen.addPreference(developerOptionsPreference)
        }
    }

    /**
     * Handles when the about preference is clicked when developer options have already been
     * unlocked.
     */
    private fun onRedundantUnlockRequest() {
        Toast.makeText(
            context,
            R.string.settings_developer_options_redundant_show_request,
            Toast.LENGTH_SHORT
        )
            .show()
    }

    /**
     * Handles when the number of clicks remaining to unlock developer options changes.
     */
    private fun onStepsToUnlockDeveloperOptionsChange(steps: Int) {
        if (steps in 1..DEV_OPTS_CLICKS_REMAINING_TOAST) {
            Toast.makeText(
                context,
                getString(R.string.settings_developer_options_steps_from_unlocking, steps),
                Toast.LENGTH_SHORT
            )
                .show()
        }
    }

    /**
     * Shows the developer password dialog.
     */
    private fun showDeveloperPasswordDialogFragment() {
        with(requireFragmentManager()) {
            if (findFragmentByTag(DEVELOPER_PASSWORD_DIALOG_FRAGMENT_TAG) != null) {
                return
            }

            DeveloperOptionsPasswordDialogFragment.newInstance()
                .apply {
                    setTargetFragment(
                        this@MainSettingsFragment,
                        DEVELOPER_PASSWORD_REQUEST_CODE
                    )
                }
                .show(this, DEVELOPER_PASSWORD_DIALOG_FRAGMENT_TAG)
        }
    }

    /**
     * Shows an OK/Cancel dialog confirming that the user wishes to disable developer options.
     */
    private fun showDisableDeveloperOptionsDialogFragment() {
        with(requireFragmentManager()) {
            if (findFragmentByTag(DISABLE_DEVELOPER_OPTIONS_DIALOG_FRAGMENT_TAG) != null) {
                return
            }

            DisableDeveloperOptionsDialogFragment.newInstance()
                .apply {
                    setTargetFragment(
                        this@MainSettingsFragment,
                        DISABLE_DEVELOPER_OPTIONS_REQUEST_CODE
                    )
                }
                .show(this, DISABLE_DEVELOPER_OPTIONS_DIALOG_FRAGMENT_TAG)
        }
    }

    // endregion Private methods

    // region Companion object

    companion object {

        // region Properties

        /**
         * The fragment tag to use for the developer password dialog fragment.
         */
        private val DEVELOPER_PASSWORD_DIALOG_FRAGMENT_TAG =
            MainSettingsFragment::class.java.name + ".DEVELOPER_PASSWORD_DIALOG"

        /**
         * The fragment tag to use for the disable developer options dialog fragment.
         */
        private val DISABLE_DEVELOPER_OPTIONS_DIALOG_FRAGMENT_TAG =
            MainSettingsFragment::class.java.name + ".DISABLE_DEVELOPER_OPTIONS_DIALOG"

        // endregion Properties

    }

    // endregion Companion object
}
