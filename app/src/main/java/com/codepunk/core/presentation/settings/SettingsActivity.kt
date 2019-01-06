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
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.preference.Preference
import androidx.preference.PreferenceDialogFragmentCompat
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceFragmentCompat.OnPreferenceDisplayDialogCallback
import com.codepunk.core.BuildConfig.CATEGORY_DEVELOPER
import com.codepunk.core.R
import com.codepunk.core.databinding.ActivitySettingsBinding
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

    /**
     * The binding for this activity.
     */
    private lateinit var binding: ActivitySettingsBinding

    /**
     * The navigation controller for the activity.
     */
    private val navController: NavController by lazy {
        Navigation.findNavController(this, R.id.settings_nav_fragment).apply {
            addOnDestinationChangedListener { _, destination, _ ->
                title = destination.label
            }
        }
    }

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

        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings)

        // TODO Must be a cleaner way to do this?
        if (intent.categories?.contains(CATEGORY_DEVELOPER) == true) {
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.fragment_main_settings, true)
                .build()
            navController.navigate(R.id.action_main_to_developer_options, null, navOptions)
        }
    }

    // endregion Lifecycle methods

    // region Inherited methods

    /**
     * Handles up navigation.
     */
    override fun onSupportNavigateUp(): Boolean {
        if (!navController.navigateUp()) {
            finish()
        }
        return true
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

}
