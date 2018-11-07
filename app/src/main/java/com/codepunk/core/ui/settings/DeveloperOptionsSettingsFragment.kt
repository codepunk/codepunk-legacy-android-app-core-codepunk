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

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.ListPreference
import com.codepunk.core.BuildConfig
import com.codepunk.core.BuildConfig.DEFAULT_REMOTE_ENVIRONMENT
import com.codepunk.core.R
import com.codepunk.core.data.remote.RemoteEnvironment
import com.codepunk.core.util.getEnvironment
import com.codepunk.doofenschmirtz.util.populate
import dagger.android.support.AndroidSupportInjection

/**
 * A preference fragment that displays developer options preferences to the user. By default,
 * developer options are not available to the user until they unlock the developer options
 * preference and authenticate themselves as a developer.
 */
class DeveloperOptionsSettingsFragment :
    BaseSettingsFragment() {

    // region Properties

    /**
     * The API environment preference.
     */
    private val remoteEnvironmentPreference by lazy {
        findPreference(BuildConfig.PREF_KEY_REMOTE_ENVIRONMENT) as ListPreference
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

    // endregion Lifecycle methods

    // region Inherited methods

    /**
     * Initializes the preference screen.
     */
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_developer_options, rootKey)
        requireActivity().title = preferenceScreen.title

        remoteEnvironmentPreference.populate(
            enumClass = RemoteEnvironment::class.java,
            entry = { requireContext().getString(it.nameResId) })
    }

    // endregion Inherited methods

    // region Implemented methods

    /**
     * Sets summary and other values in this fragment's preferences.
     */
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            BuildConfig.PREF_KEY_REMOTE_ENVIRONMENT -> {
                val environment =
                    sharedPreferences?.getEnvironment(key) ?: DEFAULT_REMOTE_ENVIRONMENT
                remoteEnvironmentPreference.summary = getString(environment.nameResId)
            }
        }
    }

    // endregion Implemented methods

}
