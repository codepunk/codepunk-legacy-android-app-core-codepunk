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

package com.codepunk.core.ui.main

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.codepunk.core.BuildConfig
import com.codepunk.core.BuildConfig.PREF_KEY_CURRENT_ACCOUNT
import com.codepunk.core.R
import com.codepunk.core.di.scope.ActivityScope
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

private const val ACCOUNT_REQUIRED_REQUEST_CODE = 1

/**
 * The main [Activity] for the Codepunk app.
 */
@ActivityScope
class MainActivity :
    AppCompatActivity(),
    HasSupportFragmentInjector {

    // region Properties

    /**
     * Performs dependency injection on fragments.
     */
    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    /**
     * The application [SharedPreferences].
     */
    @Inject
    lateinit var sharedPreferences: SharedPreferences

    // endregion Properties

    // region Lifecycle methods

    /**
     * Sets the content view for the activity.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        when (savedInstanceState) {
            null -> {
                // TODO I might be able to use SessionManager for this
                if (!sharedPreferences.contains(PREF_KEY_CURRENT_ACCOUNT)) {
                    startActivityForResult(
                        Intent(BuildConfig.ACTION_ACCOUNT),
                        ACCOUNT_REQUIRED_REQUEST_CODE
                    )
                }
            }
        }

        setContentView(R.layout.activity_main)
    }

    // endregion Lifecycle methods

    // region Inherited methods

    /**
     * Called when an [Activity] launched with [Activity.startActivityForResult] exits.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            ACCOUNT_REQUIRED_REQUEST_CODE -> { /* No op */
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
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

    // endregion Implemented methods

}
