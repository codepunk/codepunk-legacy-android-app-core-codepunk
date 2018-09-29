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

package com.codepunk.codepunk.ui.main

import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.codepunk.codepunk.R
import com.codepunk.codepunk.di.*
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

/**
 * The main [Activity] for the Codepunk app.
 */
@Suppress("unused")
@ActivityScope
class MainActivity : AppCompatActivity(), HasSupportFragmentInjector {

    // region Properties

    /**
     * Performs dependency injection on fragments.
     */
    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    /**
     * A [MainActivityComponent] instance used to inject dependencies into this activity.
     */
    @Suppress("weakerAccess")
    lateinit var mainActivityComponent: MainActivityComponent

    /**
     * This is just a dependency injection test.
     */
    @Inject
    lateinit var sharedPreferences: SharedPreferences // TODO TEMP

    /**
     * This is just a dependency injection test.
     */
    @Inject
    lateinit var applicationTestObject: ApplicationTestObject // TODO TEMP

    /**
     * This is just a dependency injection test.
     */
    @ActivityScope // This scope doesn't seem to matter, we always get a new instance.
    @Inject
    lateinit var singletonInjectedTestObject: SingletonInjectedTestObject // TODO TEMP

    /**
     * This is just a dependency injection test.
     */
    @Inject
    lateinit var activityTestObject: ActivityTestObject // TODO TEMP

    // endregion Properties

    // region Lifecycle methods

    /**
     * Sets the content view for the activity.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    // endregion Lifecycle methods

    // region Implemented methods

    /**
     * Implementation of [HasSupportFragmentInjector]. Returns a
     * [DispatchingAndroidInjector] that injects dependencies into fragments.
     */
    override fun supportFragmentInjector(): AndroidInjector<Fragment> =
        fragmentDispatchingAndroidInjector

    // endregion Implemented methods

}
