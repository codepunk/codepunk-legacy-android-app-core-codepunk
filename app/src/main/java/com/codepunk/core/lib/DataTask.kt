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

package com.codepunk.core.lib

import android.os.AsyncTask
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import retrofit2.Response
import java.util.concurrent.Executor

/**
 * An implementation of [AsyncTask] that wraps [Progress] and [Result] in a [DataUpdate]
 * sealed class and sets it to a [MutableLiveData] instance.
 */
abstract class DataTask<Params, Progress, Result> :
    AsyncTask<Params, Progress?, Result?>() {

    // region Properties

    /**
     * A [LiveData] that will contain progress, results, or exceptions related to this task.
     */
    private val liveData = MutableLiveData<DataUpdate<Progress, Result>>()

    /**
     * Any exceptions that were encountered while executing this task.
     */
    private var response: Response<Result>? = null

    // endregion Properties

    // region Constructors

    /**
     * Sets liveData value to PendingUpdate by default. Most observers will choose to ignore this
     * state.
     */
    init {
        liveData.value = PendingUpdate()
    }

    // endregion Constructors

    // region Inherited methods

    /**
     * Publishes progress without any data. This will initialize the value in [liveData] to
     * an empty [LoadingUpdate] instance.
     */
    override fun onPreExecute() {
        publishProgress()
    }

    /**
     * Updates [liveData] with a [LoadingUpdate] instance describing this task's progress.
     */
    override fun onProgressUpdate(vararg values: Progress?) {
        liveData.value = LoadingUpdate(values)
    }

    /**
     * Updates [liveData] with a [SuccessUpdate] instance describing the result of this task.
     */
    override fun onPostExecute(result: Result?) {
        liveData.value = SuccessUpdate(result, response)
    }

    /**
     * Updates [liveData] with a [FailureUpdate] instance describing the reason(s) the task
     * failed or was cancelled.
     */
    override fun onCancelled(result: Result?) {
        liveData.value = FailureUpdate(result, response)
    }

    // endregion Inherited methods

    // region Methods

    /**
     * Convenience method for executing this task and getting the results as [LiveData]. Executes
     * this task with [params] and returns [liveData] for observation.
     */
    @Suppress("UNUSED")
    fun fetch(vararg params: Params): LiveData<DataUpdate<Progress, Result>> {
        execute(*params)
        return liveData
    }

    /**
     * Convenience method for executing this task and getting the results as [LiveData]. Executes
     * this task on the supplied [Executor] [exec] with [params] and returns [liveData] for
     * observation.
     */
    fun fetchOnExecutor(
        exec: Executor,
        vararg params: Params
    ): LiveData<DataUpdate<Progress, Result>> {
        executeOnExecutor(exec, *params)
        return liveData
    }

    /**
     * Convenience method for returning from [doInBackground]. Saves the [response] and returns
     * the supplied [result].
     *
     * This allows for concise code such as the following:
     *
     * ```kotlin
     * override fun doInBackground(vararg params: Void?): User? {
     *     return myWebservice.getMyData().execute().run {
     *         when {
     *             isSuccessful -> succeed(body(), this)
     *             else -> fail(null, this)
     *         }
     *     }
     * }
     * ```
     */
    fun succeed(result: Result?, response: Response<Result>? = null): Result? {
        this.response = response
        return result
    }

    /**
     * Convenience method for handling errors in [doInBackground]. Cancels the DataTask, optionally
     * saving the [response] and returning the supplied [result].
     *
     * This allows for concise code such as the following:
     *
     * ```kotlin
     * override fun doInBackground(vararg params: Void?): User? {
     *     return myWebservice.getMyData().execute().run {
     *         when {
     *             isSuccessful -> succeed(body(), this)
     *             else -> fail(null, this)
     *         }
     *     }
     * }
     * ```
     */
    fun fail(
        result: Result?,
        response: Response<Result>? = null,
        mayInterruptIfRunning: Boolean = true
    ): Result? {
        this.response = response
        cancel(mayInterruptIfRunning)
        return result
    }

    // endregion Methods

}
