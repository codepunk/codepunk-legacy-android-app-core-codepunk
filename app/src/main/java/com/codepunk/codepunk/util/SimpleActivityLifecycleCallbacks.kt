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

package com.codepunk.codepunk.util

import android.app.Activity
import android.app.Application
import android.os.Bundle

/**
 * A convenience class to extend when you only want to listen for a subset of all the activity
 * lifecycle callbacks.
 *
 * Implementation of [Application.ActivityLifecycleCallbacks].
 */
abstract class SimpleActivityLifecycleCallbacks : Application.ActivityLifecycleCallbacks {

    // region Implemented methods

    /**
     * Called when the activity is starting.
     */
    override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {}

    /**
     * Called after [onActivityCreated]. It will be followed by [onActivityResumed].
     */
    override fun onActivityStarted(activity: Activity?) {}

    /**
     * Called after [onActivityPaused], for your activity to start interacting with the user.
     */
    override fun onActivityResumed(activity: Activity?) {}

    /**
     * Called as part of the activity lifecycle when an activity is going into the background,
     * but has not (yet) been killed. The counterpart to [onActivityResumed].
     */
    override fun onActivityPaused(activity: Activity?) {}

    /**
     * Called when the activity no longer visible to the user. You will next receive either
     * [onActivityDestroyed] or nothing, depending on later user activity.
     */
    override fun onActivityStopped(activity: Activity?) {}

    /**
     * Perform any final cleanup before an activity is destroyed. This can happen either because
     * the activity is finishing (someone called [Activity.finish] on it, or because the system is
     * temporarily destroying this instance of the activity to save space.
     */
    override fun onActivityDestroyed(activity: Activity?) {}

    /**
     * Called to retrieve per-instance state from an activity before being killed so that the state
     * can be restored in [onActivityCreated].
     */
    override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {}

    // endregion Implemented methods

}
