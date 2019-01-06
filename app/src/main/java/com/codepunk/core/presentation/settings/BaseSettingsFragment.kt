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

package com.codepunk.core.presentation.settings

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import javax.inject.Inject

/*
 * Author(s): Scott Slater
 */

/**
 * A base settings fragment that registers for shared preference change listeners and initializes
 * persistable preferences.
 */
abstract class BaseSettingsFragment :
    PreferenceFragmentCompat(),
    OnSharedPreferenceChangeListener {

    // region Properties

    /**
     * The application [SharedPreferences].
     */
    @Inject
    open lateinit var sharedPreferences: SharedPreferences

    // endregion Properties

    // region Lifecycle methods

    /**
     * Registers to listen for shared preference changes.
     */
    override fun onStart() {
        super.onStart()
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    /**
     * Unregisters from listening for shared preference changes.
     */
    override fun onStop() {
        super.onStop()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    // endregion Lifecycle methods

    // region Inherited methods

    /**
     * Initializes persistable preferences with the current values in [sharedPreferences].
     */
    override fun setPreferenceScreen(preferenceScreen: PreferenceScreen?) {
        super.setPreferenceScreen(preferenceScreen)
        preferenceScreen?.run {
            for (i in 0 until preferenceCount) {
                val preference = getPreference(i)
                if (preference.hasKey() && preference.isPersistent) {
                    onSharedPreferenceChanged(sharedPreferences, preference.key)
                }
            }
        }
    }

    // endregion Inherited methods

    // region Implemented methods

    /**
     * Implementation of [OnSharedPreferenceChangeListener]. Called when a shared preference
     * changes.
     */
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        // No op
    }

    // endregion Implemented methods

}
