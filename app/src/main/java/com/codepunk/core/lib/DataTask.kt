/*
 * Copyright (C) 2018 Codepunk, LLC
 * Author(s): Scott Slater
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
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.concurrent.Executor

/**
 * An implementation of [AsyncTask] that wraps [Progress] and [Result] in a [DataUpdate]
 * sealed class and sets it to a [MutableLiveData] instance.
 */
abstract class DataTask<Params, Progress, Result>(
    data: Bundle? = null
) : AsyncTask<Params, Progress, ResultUpdate<Progress, Result>>() {

    // region Properties

    /**
     * An optional [Bundle] for holding additional data. This allows, for example, not only
     * progress publication using [Progress], but also additional information along with
     * that progress by putting the desired information into [data] prior to calling
     * [publishProgress].
     */
    private var _data: Bundle? = data

    /**
     * A public, non-nullable wrapper for [_data] that will self-initialize upon first reference.
     */
    val data: Bundle
        get() = _data ?: Bundle().apply { _data = this }

    /**
     * A [LiveData] that will contain progress, results, or exceptions related to this task.
     */
    @Suppress("WEAKER_ACCESS")
    val liveData = MutableLiveData<DataUpdate<Progress, Result>>()
        .apply {
            value = PendingUpdate(_data)
        }

    // endregion Properties

    // region Inherited methods

    /**
     * Publishes progress without any data. This will initialize the value in [liveData] to
     * an empty [ProgressUpdate] instance.
     */
    override fun onPreExecute() {
        onProgressUpdate()
    }

    /**
     * Updates [liveData] with a [ProgressUpdate] instance describing this task's progress.
     */
    override fun onProgressUpdate(vararg values: Progress?) {
        liveData.value = ProgressUpdate(values, _data)
    }

    /**
     * Updates [liveData] with the result from [doInBackground].
     */
    override fun onPostExecute(result: ResultUpdate<Progress, Result>?) {
        liveData.value = result
    }

    /**
     * Updates [liveData] with the result from [doInBackground] if the task was cancelled.
     */
    override fun onCancelled(result: ResultUpdate<Progress, Result>?) {
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

    // endregion Methods

}
