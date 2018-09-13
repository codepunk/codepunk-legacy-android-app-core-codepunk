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

import javax.inject.Inject

/**
 * A class for testing an application-level dependency injection.
 */
class ApplicationTestObject {

    /**
     * Sample text for this class.
     */
    val text: String = "ApplicationTestObject!"

}

/**
 * A class for testing a directly-injected/created object.
 */
class SingletonInjectedTestObject @Inject constructor() {

    /**
     * Sample text for this class.
     */
    val text: String = "SingletonInjectedTestObject!"

}

/**
 * A class for testing an activity-level dependency injection.
 */
@ActivityScope
class ActivityTestObject {

    /**
     * Sample text for this class.
     */
    val text: String = "ActivityTestObject!"

}

/**
 * A class for testing a fragment-level dependency injection.
 */
@FragmentScope
class FragmentTestObject {

    /**
     * Sample text for this class.
     */
    val text: String = "FragmentTestObject!"

}

/**
 * A class for testing a non-scoped dependency injection.
 */
@Suppress("unused")
class NonScopedTestObject {

    /**
     * Sample text for this class.
     */
    val text: String = "NonScopedTestObject!"

}
