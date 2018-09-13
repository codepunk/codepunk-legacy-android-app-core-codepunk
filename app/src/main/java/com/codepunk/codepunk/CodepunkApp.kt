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

package com.codepunk.codepunk

import android.app.Application
import android.util.Log
import com.codepunk.codepunk.di.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The main Codepunk [Application].
 */
@Suppress("unused")
class CodepunkApp : Application(), HasMainActivityComponentBuilder {

    // region Implemented properties

    /**
     * Implementation of [HasMainActivityComponentBuilder]. A [MainActivityComponent.Builder]
     * used to create instances of [MainActivityComponent].
     */
    @Inject
    override lateinit var mainActivityComponentBuilder: MainActivityComponent.Builder

    // endregion Implemented properties

    // region Properties

    /**
     * An [AppComponent] instance used to inject dependencies into this application.
     */
    @Suppress("weakerAccess")
    lateinit var appComponent: AppComponent

    /**
     * This is just a dependency injection test.
     */
    @Inject
    lateinit var applicationTestObject: ApplicationTestObject

    /**
     * This is just a dependency injection test.
     */
    @Singleton // This scope doesn't seem to matter, we always get a new instance.
    @Inject
    lateinit var singletonInjectedTestObject: SingletonInjectedTestObject

    /**
     * This is just a dependency injection test.
     */
    @Singleton // This scope doesn't seem to matter, we always get a new instance.
    @Inject
    lateinit var anotherSingletonInjectedTestObject: SingletonInjectedTestObject

    // endregion Properties

    // region Lifecycle methods

    /**
     * Sets up dependency injection for the application.
     */
    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.builder()
            .application(this)
            .build()
        appComponent.inject(this)
        Log.d("CodepunkApp", "onCreate")
    }

    // endregion Lifecycle methods

}
