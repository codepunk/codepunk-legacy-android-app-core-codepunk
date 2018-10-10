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

package com.codepunk.core.data.repository

import android.os.AsyncTask
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.codepunk.core.data.*

/**
 * An implementation of [AsyncTask] that wraps [Progress] and [Result] in a [PublishedState]
 * sealed class and sets it to a [MutableLiveData] instance.
 */
abstract class LiveDataTask<Params, Progress, Result> :
    AsyncTask<Params, Progress, Result>() {

    /**
     * A [LiveData] that will contain progress, results, or exceptions related to this task.
     */
    private val liveData = MutableLiveData<PublishedState<Progress, Result>>().apply {
        value = PendingState()
    }

    /**
     * Any exceptions that were encountered while executing this task.
     */
    protected var e: Exception? = null

    /**
     * Publishes progress without any data. This will initialize the value in [liveData] to
     * an empty [RunningState] instance.
     */
    override fun onPreExecute() {
        publishProgress()
    }

    /**
     * Updates [liveData] with a [RunningState] instance describing this task's progress.
     */
    override fun onProgressUpdate(vararg values: Progress) {
        liveData.value = RunningState(values)
    }

    /**
     * Updates [liveData] with a [FinishedState] instance describing the result of this task.
     */
    override fun onPostExecute(result: Result) {
        liveData.value = FinishedState(result)
    }

    /**
     * Updates [liveData] with a [CancelledState] instance describing the reason(s) the task
     * failed or was cancelled.
     */
    override fun onCancelled(result: Result) {
        liveData.value = CancelledState(result, e)
    }

    /**
     * Returns the current state of this task wrapped in a [LiveData] instance.
     */
    fun asLiveData(): LiveData<PublishedState<Progress, Result>> = liveData

}
