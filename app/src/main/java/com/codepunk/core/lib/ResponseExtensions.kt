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

import com.codepunk.core.data.remote.entity.RemoteNetworkResponse
import retrofit2.Response
import retrofit2.Retrofit

/**
 * Converts a [Response] containing a [RemoteNetworkResponse] into a RemoteNetworkResponse. This may sound
 * trivial but when a request comes back unsuccessful, the errorBody contains a JSON string
 * that represents a RemoteNetworkResponse, and that must be converted here.
 */
@Suppress("UNUSED")
fun Response<RemoteNetworkResponse>?.toRemoteNetworkResponse(retrofit: Retrofit): RemoteNetworkResponse? {
    return when {
        this == null -> null
        isSuccessful -> body()
        else -> errorBody()?.run {
            retrofit.responseBodyConverter<RemoteNetworkResponse>(
                RemoteNetworkResponse::class.java,
                arrayOf()
            ).convert(this)
        }
    }
}
