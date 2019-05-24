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

package com.codepunk.core.lib.retrofit

import android.os.Bundle
import androidx.lifecycle.LiveData
import com.codepunk.doofenschmirtz.util.http.HttpStatusException
import com.codepunk.doofenschmirtz.util.resourceinator.FailureResource
import com.codepunk.doofenschmirtz.util.resourceinator.Resourceinator
import com.codepunk.doofenschmirtz.util.resourceinator.ResultResource
import com.codepunk.doofenschmirtz.util.resourceinator.SuccessResource
import retrofit2.Call
import retrofit2.Response
import java.io.IOException

/**
 * Extension function that takes a [Response] from executing a [Call] and converts it into a
 * [ResultResource].
 *
 * This allows for ultra-concise code, such as the following example in
 * [Resourceinator.doInBackground]:
 *
 * ```kotlin
 * override fun doInBackground(vararg params: Void?): Resource<Void, User>? =
 *     myWebservice.getMyData().getResultResource()
 * ```
 *
 * In the above example, doInBackground will return an appropriate instance of
 * Resource<Void, User>. This value will be stored in the Resourceinator's [LiveData], which can be
 * observed from an Activity or other observer.
 */
fun <Progress, Result> Call<Result>.getResultResource(data: Bundle? = null):
    ResultResource<Progress, Response<Result>> {
    return try {
        val response = execute()
        when {
            response.isSuccessful ->
                SuccessResource<Progress, Response<Result>>(
                    response
                ).apply {
                    this.data = data
                }
            else -> FailureResource<Progress, Response<Result>>(
                // TODO Try to process response.errorBody()?.string()? Or can I just do that in the observer?
                response,
                HttpStatusException(response.code())
            ).apply {
                this.data = data
            }
        }
    } catch (e: IOException) {
        FailureResource<Progress, Response<Result>>(
            null,
            e
        ).apply {
            this.data = data
        }
    }
}
