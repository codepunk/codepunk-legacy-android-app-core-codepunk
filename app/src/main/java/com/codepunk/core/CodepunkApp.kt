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
import android.util.Log
import com.codepunk.core.di.component.DaggerAppComponent
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import javax.inject.Inject

/**
 * The main Codepunk [Application].
 */
@Suppress("unused")
class CodepunkApp : Application(), HasActivityInjector {

    // region Properties

    /**
     * Performs dependency injection on activities.
     */
    @Inject
    lateinit var activityDispatchingAndroidInjector: DispatchingAndroidInjector<Activity>

    // endregion Properties

    // region Lifecycle methods

    /**
     * Performs dependency injection for the application.
     */
    override fun onCreate() {
        super.onCreate()
        DaggerAppComponent.builder()
            .create(this)
            .inject(this)
        Log.d("CodepunkApp", "onCreate")
    }

    // endregion Lifecycle methods

    // region Implemented methods

    /**
     * Implementation of [HasActivityInjector]. Returns a
     * [DispatchingAndroidInjector] that injects dependencies into activities.
     */
    override fun activityInjector(): AndroidInjector<Activity> = activityDispatchingAndroidInjector

    // endregion Implemented methods

}
