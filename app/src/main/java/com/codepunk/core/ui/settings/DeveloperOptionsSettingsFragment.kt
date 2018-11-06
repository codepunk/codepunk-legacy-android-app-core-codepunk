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
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import com.codepunk.core.BuildConfig
import com.codepunk.core.R
import com.codepunk.core.data.remote.RemoteEnvironment
import com.codepunk.doofenschmirtz.util.populate
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

/**
 * A preference fragment that displays developer options preferences to the user. By default,
 * developer options are not available to the user until they unlock the developer options
 * preference and authenticate themselves as a developer.
 */
class DeveloperOptionsSettingsFragment :
    PreferenceFragmentCompat() {

    // region Properties

    /**
     * A [ViewModelProvider.Factory] for creating [ViewModel] instances.
     */
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    /**
     * An instance of [DeveloperOptionsSettingsViewModel] for managing developer options settings.
     */
    private val developerPreferencesViewModel: DeveloperOptionsSettingsViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory)
            .get(DeveloperOptionsSettingsViewModel::class.java)
    }

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

        with(developerPreferencesViewModel) {
            remoteEnvironmentData.observe(this@DeveloperOptionsSettingsFragment,
                Observer { env ->
                    remoteEnvironmentPreference.summary =
                            env?.let { getString(it.nameResId) } ?: ""
                })
        }
    }

    // endregion Inherited methods
}
