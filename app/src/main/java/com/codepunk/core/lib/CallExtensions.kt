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

import android.os.Bundle
import androidx.lifecycle.LiveData
import retrofit2.Call
import retrofit2.Response
import java.io.IOException

/**
 * Extension function that takes a [Response] from executing a [Call] and converts it into a
 * [DataUpdate].
 *
 * This allows for ultra-concise code, such as the following example in [DataTask.doInBackground]:
 *
 * ```kotlin
 * override fun doInBackground(vararg params: Void?): DataUpdate<Void, User>? =
 *     myWebservice.getMyData().toDataUpdate()
 * ```
 *
 * In the above example, doInBackground will return an appropriate instance of
 * DataUpdate<Void, User>. This value will be stored in the DataTask's [LiveData], which can be
 * observed from an Activity or other observer.
 */
fun <Progress, Result> Call<Result>.toDataUpdate(data: Bundle? = null):
        DataUpdate<Progress, Result> {
    return try {
        val response = execute()
        when {
            response.isSuccessful ->
                SuccessUpdate<Progress, Result>(response.body(), response)
            else -> FailureUpdate( // TODO Try to process response.errorBody()?.string()? Or can I just do that in the observer?
                response.body(),
                HttpStatusException(response.code()),
                response
            )
        }
    } catch (e: IOException) {
        FailureUpdate(null, e)
    }
}
