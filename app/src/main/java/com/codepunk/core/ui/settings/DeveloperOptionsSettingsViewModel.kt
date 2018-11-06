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

import android.content.SharedPreferences
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.codepunk.core.BuildConfig.*
import com.codepunk.core.data.remote.RemoteEnvironment
import com.codepunk.core.util.getEnvironment
import javax.inject.Inject

/**
 * The [ViewModel] that stores developer options-related preference data.
 */
class DeveloperOptionsSettingsViewModel @Inject constructor(

    /**
     * The application shared preferences.
     */
    private val sharedPreferences: SharedPreferences

) : ViewModel(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    // region Properties

    /**
     * The current environment used to configure API calls.
     */
    var remoteEnvironmentData = MutableLiveData<RemoteEnvironment>()

    /**
     * The authenticated hashed developer password. This is set once the user has successfully
     * authenticated themselves as a developer.
     */
    private var developerOptionsAuthenticatedHash = MutableLiveData<String>()

    /**
     * Boolean value that indicates whether or not developer options have been unlocked, i.e.
     * whether or not the developer options preference will be visible to the user.
     */
    var developerOptionsUnlocked = MutableLiveData<Boolean>()

    /**
     * LiveData that tracks the current developer options state.
     */
    var developerOptionsState =
        MediatorLiveData<DeveloperOptionsState>().apply {
            addSource(developerOptionsUnlocked) { unlocked ->
                updateDeveloperOptionsState(
                    unlocked == true,
                    developerOptionsAuthenticatedHash.value
                )
            }
            addSource(developerOptionsAuthenticatedHash) { hash ->
                updateDeveloperOptionsState(
                    developerOptionsUnlocked.value == true,
                    hash
                )
            }
        }

    /**
     * Initializes the data maintained by this ViewModel.
     */
    init {
        with(sharedPreferences) {
            this.registerOnSharedPreferenceChangeListener(this@DeveloperOptionsSettingsViewModel)

            onSharedPreferenceChanged(
                this,
                PREF_KEY_REMOTE_ENVIRONMENT
            )

            onSharedPreferenceChanged(
                this,
                PREF_KEY_DEVELOPER_OPTIONS_AUTHENTICATED_HASH
            )

            /*
            remoteEnvironmentData.value = getRemoteEnvironmentData(
                BuildConfig.PREF_KEY_REMOTE_ENVIRONMENT,
                BuildConfig.DEFAULT_REMOTE_ENVIRONMENT
            )

            developerOptionsAuthenticatedHash.value =
                    getString(PREF_KEY_DEVELOPER_OPTIONS_AUTHENTICATED_HASH, null)
            */

            developerOptionsUnlocked.value = getBoolean(
                PREF_KEY_DEVELOPER_OPTIONS_UNLOCKED,
                false
            )

            updateDeveloperOptionsState(
                developerOptionsUnlocked.value == true,
                developerOptionsAuthenticatedHash.value
            )

            registerOnSharedPreferenceChangeListener(this@DeveloperOptionsSettingsViewModel)
        }

        // TODO Check if persisted developer hash (if any) is stale. If it is, I guess we need to
        // start the settings activity (if we haven't already)
    }

    // endregion Properties

    // region Inherited methods

    /**
     * Unregisters the shared preference change listener.
     */
    override fun onCleared() {
        super.onCleared()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    // endregion Inherited methods

    // region Implemented methods

    /**
     * Updates the LiveData with the latest values from the app's SharedPreferences.
     */
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        sharedPreferences?.apply {
            when (key) {
                PREF_KEY_REMOTE_ENVIRONMENT ->
                    remoteEnvironmentData.value = getEnvironment(key) ?: DEFAULT_REMOTE_ENVIRONMENT

                PREF_KEY_DEVELOPER_OPTIONS_AUTHENTICATED_HASH ->
                    developerOptionsAuthenticatedHash.value = getString(key, null)

                PREF_KEY_DEVELOPER_OPTIONS_UNLOCKED ->
                    developerOptionsUnlocked.value = getBoolean(key, false)
            }
        }
    }

    // endregion Implemented methods

    // region Methods

    /**
     * Updates whether the developer options preference has been unlocked as well as whether
     * the user has authenticated themselves as a developer. The [onSharedPreferenceChanged]
     * method will catch changes to these preferences and update the corresponding LiveData
     * accordingly.
     */
    fun updateDeveloperOptions(unlocked: Boolean, hash: String? = null) {
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
     * Updates the developer options state based on whether the developer options preference
     * has been unlocked and whether the user has authenticated themselves as a developer.
     */
    private fun updateDeveloperOptionsState(unlocked: Boolean, hash: String? = null) {
        val newValue = when {
            !unlocked -> DeveloperOptionsState.LOCKED
            hash == null -> DeveloperOptionsState.UNLOCKED
            else -> DeveloperOptionsState.ENABLED
        }
        if (developerOptionsState.value != newValue) {
            developerOptionsState.value = newValue
        }
    }

    // endregion Methods

    // region Nested classes

    /**
     * The possible states of the developer options preference.
     */
    enum class DeveloperOptionsState {
        /**
         * The developer options preference is invisible and therefore inaccessible.
         */
        LOCKED,

        /**
         * The developer options preference has been unlocked (and is therefore visible), but
         * the user has not yet authenticated themselves as a developer.
         */
        UNLOCKED,

        /**
         * The developer options preference has been unlocked and the user has authenticated
         * themselves as a developer.
         */
        ENABLED
    }

    // endregion Nested classes
}
