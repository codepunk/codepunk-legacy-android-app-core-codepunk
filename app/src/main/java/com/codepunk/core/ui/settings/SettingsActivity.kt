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
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import androidx.preference.PreferenceDialogFragmentCompat
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceFragmentCompat.OnPreferenceDisplayDialogCallback
import com.codepunk.core.BuildConfig.CATEGORY_DEVELOPER
import com.codepunk.doofenschmirtz.preference.displayCustomPreferenceDialogFragment
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

/**
 * The [Activity] that will serve as the container for all settings-related fragments.
 */
class SettingsActivity : AppCompatActivity(),
    HasSupportFragmentInjector,
    OnPreferenceDisplayDialogCallback {

    // region Properties

    /**
     * Performs dependency injection on fragments.
     */
    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    // endregion Properties

    // region Lifecycle methods

    /**
     * Creates the appropriate preference fragment based on the category supplied
     * in the [Intent].
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (supportFragmentManager.findFragmentById(android.R.id.content) == null) {
            supportFragmentManager
                .beginTransaction()
                .add(android.R.id.content, createFragment(intent))
                .commit()
        }
    }

    // endregion Lifecycle methods

    // region Inherited methods

    /**
     * Changes the default "Up" behavior to always go "back" instead of "up".
     */
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> {
                // Always go back instead of "Up"
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // endregion Inherited methods

    // region Implemented methods

    /**
     * Implementation of [HasSupportFragmentInjector]. Returns a
     * [DispatchingAndroidInjector] that injects dependencies into fragments.
     */
    override fun supportFragmentInjector(): AndroidInjector<Fragment> =
        fragmentDispatchingAndroidInjector

    /**
     * Provides a way to display custom [PreferenceDialogFragmentCompat]s.
     */
    // PreferenceFragmentCompat.OnPreferenceDisplayDialogCallback
    override fun onPreferenceDisplayDialog(
        caller: PreferenceFragmentCompat,
        pref: Preference?
    ): Boolean {
        return caller.displayCustomPreferenceDialogFragment(pref)
    }

    // endregion Implemented methods

    // region Companion object

    companion object {

        // region Methods

        /**
         * Creates a [PreferenceFragmentCompat] associated with the category passed in the intent.
         */
        private fun createFragment(intent: Intent): PreferenceFragmentCompat {
            intent.categories?.run {
                for (category in this) {
                    when (category) {
                        CATEGORY_DEVELOPER -> return DeveloperOptionsSettingsFragment()
                    }
                }
            }
            return MainSettingsFragment()
        }

        // endregion Methods

    }

    // endregion Companion object
}
