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

package com.codepunk.codepunk.di

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks
import com.codepunk.codepunk.CodepunkApp
import com.codepunk.codepunk.util.SimpleActivityLifecycleCallbacks
import dagger.android.AndroidInjection
import dagger.android.support.AndroidSupportInjection

/**
 * Helper class that sets up Android dependency injection at the [Application], [Activity], and
 * [Fragment] levels.
 */
object AppInjector {

    // region Methods

    /**
     * Injects into the supplied [application] and listens for activity callbacks for
     * automatic dependency injection.
     */
    fun register(application: CodepunkApp) {
        DaggerAppComponent.builder()
            .application(application)
            .build()
            .inject(application)
        application.registerActivityLifecycleCallbacks(InjectionActivityLifecycleCallbacks)
    }

    // endregion Methods

    // region Nested/inner classes

    /**
     * Helper object for handling activity lifecycle callback methods.
     */
    object InjectionActivityLifecycleCallbacks : SimpleActivityLifecycleCallbacks() {

        // region Inherited methods

        /**
         * Automatically injects into [Activity] instances that implement the [Injectable]
         * interface.
         */
        override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
            if (activity is Injectable) {
                AndroidInjection.inject(activity)
                (activity as? FragmentActivity)
                    ?.supportFragmentManager
                    ?.registerFragmentLifecycleCallbacks(
                        InjectionFragmentLifecycleCallbacks,
                        true
                    )
            }
        }

        // endregion Inherited methods

    }

    /**
     * Helper object for handling fragment lifecycle callback methods.
     */
    object InjectionFragmentLifecycleCallbacks : FragmentLifecycleCallbacks() {

        // region Inherited methods

        /**
         * Automatically injects into [Fragment] instances that implement the [Injectable]
         * interface.
         */
        override fun onFragmentCreated(
            fm: FragmentManager,
            f: Fragment,
            savedInstanceState: Bundle?
        ) {
            if (f is Injectable) {
                AndroidSupportInjection.inject(f)
            }
        }

        // endregion Inherited methods

    }

    // endregion Nested/inner classes

}
