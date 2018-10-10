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
import java.util.concurrent.Executor

/**
 * An implementation of [AsyncTask] that wraps [Progress] and [Result] in a [OperationStatus]
 * sealed class and sets it to a [MutableLiveData] instance.
 */
abstract class DataOperation<Params, Progress, Result> :
    AsyncTask<Params, Progress, Result>() {

    // region Properties

    /**
     * A [LiveData] that will contain progress, results, or exceptions related to this task.
     */
    private val liveData = MutableLiveData<OperationStatus<Progress, Result>>()

    /**
     * Any exceptions that were encountered while executing this task.
     */
    protected var e: Exception? = null

    // endregion Properties

    // region Constructors

    /**
     * Sets liveData value to PendingState by default. Most observers will choose to ignore this
     * state.
     */
    init {
        liveData.value = PendingState()
    }

    // endregion Constructors

    // region Inherited methods

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

    // endregion Inherited methods

    // region Methods

    /**
     * Returns the current state of this task wrapped in a [LiveData] instance.
     */
    fun asLiveData(): LiveData<OperationStatus<Progress, Result>> = liveData

    /**
     * Executes this operation with [params] and returns [liveData] for observation.
     */
    @Suppress("UNUSED")
    fun compute(vararg params: Params): LiveData<OperationStatus<Progress, Result>> {
        execute(*params)
        return liveData
    }

    /**
     * Executes this operation on [exec] with [params] and returns [liveData] for observation.
     */
    fun computeOnExecutor(
        exec: Executor,
        vararg params: Params
    ): LiveData<OperationStatus<Progress, Result>> {
        executeOnExecutor(exec, *params)
        return liveData
    }

    // endregion Methods

}
