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

package com.codepunk.core

import android.app.Activity
import android.app.Application
import android.app.Service
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.codepunk.core.BuildConfig.DEFAULT_REMOTE_ENVIRONMENT
import com.codepunk.core.BuildConfig.PREF_KEY_REMOTE_ENVIRONMENT
import com.codepunk.core.data.remote.RemoteEnvironment
import com.codepunk.core.di.component.DaggerAppComponent
import com.codepunk.core.util.getEnvironment
import com.codepunk.doofenschmirtz.util.Translatinator
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.HasServiceInjector
import javax.inject.Inject

/**
 * The main Codepunk [Application].
 */
@Suppress("unused")
class CodepunkApp :
    Application(),
    HasActivityInjector,
    HasServiceInjector,
    SharedPreferences.OnSharedPreferenceChangeListener {

    // region Properties

    /**
     * Performs dependency injection on activities.
     */
    @Inject
    lateinit var activityDispatchingAndroidInjector: DispatchingAndroidInjector<Activity>

    /**
     * Performs dependency injection on services.
     */
    @Inject
    lateinit var serviceDispatchingAndroidInjector: DispatchingAndroidInjector<Service>

    /**
     * The application [SharedPreferences]
     */
    @Inject
    lateinit var sharedPreferences: SharedPreferences

    // TODO TEMP
    @Inject
    lateinit var translatinator: Translatinator
    // END TEMP

    /**
     * The current [RemoteEnvironment] being used for remote API calls.
     */
    @Suppress("WEAKER_ACCESS")
    lateinit var remoteEnvironment: RemoteEnvironment
        private set

    // endregion Properties

    // region Lifecycle methods

    /**
     * Performs dependency injection for the application and establishes the remote environment
     * for API calls.
     */
    @Suppress("UNRESOLVED")
    override fun onCreate() {
        super.onCreate()
        DaggerAppComponent.builder()
            .create(this)
            .inject(this)

        remoteEnvironment = sharedPreferences.getEnvironment(PREF_KEY_REMOTE_ENVIRONMENT)
            ?: DEFAULT_REMOTE_ENVIRONMENT
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        PreferenceManager.setDefaultValues(this, R.xml.settings_main, false)
    }

    /**
     * Performs cleanup. Not totally necessary since the app is done when the Application object
     * is destroyed, but is it included here for completeness.
     */
    override fun onTerminate() {
        super.onTerminate()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    // endregion Lifecycle methods

    // region Implemented methods

    /**
     * Implementation of [HasActivityInjector]. Returns a
     * [DispatchingAndroidInjector] that injects dependencies into activities.
     */
    override fun activityInjector(): AndroidInjector<Activity> = activityDispatchingAndroidInjector

    /**
     * Implementation of [HasServiceInjector]. Returns a
     * [DispatchingAndroidInjector] that injects dependencies into services.
     */
    override fun serviceInjector(): AndroidInjector<Service> = serviceDispatchingAndroidInjector

    /**
     * Sets [remoteEnvironment] to the value referenced in [PREF_KEY_REMOTE_ENVIRONMENT].
     */
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        sharedPreferences?.apply {
            when (key) {
                PREF_KEY_REMOTE_ENVIRONMENT ->
                    remoteEnvironment =
                        sharedPreferences.getEnvironment(key) ?: DEFAULT_REMOTE_ENVIRONMENT
            }
        }
    }

    // endregion Implemented methods

    // region Methods


    // endregion Methods

}
