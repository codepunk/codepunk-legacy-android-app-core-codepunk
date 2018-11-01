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
import com.codepunk.core.BuildConfig
import retrofit2.Call
import java.io.IOException
import java.util.concurrent.Executor

abstract class DataTask2<Params, Progress, Result> :
    AsyncTask<Params, Progress?, DataUpdate<Progress, Result>>() {

    // region Properties

    /**
     * A [LiveData] that will contain progress, results, or exceptions related to this task.
     */
    val liveData = MutableLiveData<DataUpdate<Progress, Result>>()

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
        onProgressUpdate()
    }

    /**
     * Updates [liveData] with a [LoadingUpdate] instance describing this task's progress.
     */
    override fun onProgressUpdate(vararg values: Progress?) {
        liveData.value = LoadingUpdate(values)
    }

    override fun onPostExecute(result: DataUpdate<Progress, Result>?) {
        liveData.value = result
    }

    override fun onCancelled(result: DataUpdate<Progress, Result>?) {
        liveData.value = result
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
    @Suppress("UNUSED")
    fun fetchOnExecutor(
        exec: Executor = AsyncTask.THREAD_POOL_EXECUTOR,
        vararg params: Params
    ): LiveData<DataUpdate<Progress, Result>> {
        executeOnExecutor(exec, *params)
        return liveData
    }

    /*
    fun success(result: Result?): DataUpdate<Progress, Result> {
        return SuccessUpdate(result)
    }

    fun failure(
        result: Result?,
        e: Exception = CancellationException(),
        mayInterruptIfRunning: Boolean
    ): DataUpdate<Progress, Result> {
        cancel(mayInterruptIfRunning)
        return FailureUpdate(result, null, e)
    }
    */

    // endregion Methods

    // region Companion object

    companion object {

        // region Methods

        fun <Progress, Result> generateUpdate(call: Call<Result>): DataUpdate<Progress, Result> {
            return try {
                val response = call.execute()
                when {
                    response.isSuccessful ->
                        SuccessUpdate<Progress, Result>(response.body(), response)
                    else -> FailureUpdate(
                        response.body(),
                        HttpStatusException(response.code()),
                        response
                    )
                }
            } catch (e: IOException) {
                FailureUpdate(null, e)
            }
        }

        // endregion Methods

    }

    // endregion Companion object

}
