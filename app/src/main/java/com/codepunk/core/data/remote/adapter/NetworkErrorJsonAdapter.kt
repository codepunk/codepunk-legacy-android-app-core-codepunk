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

package com.codepunk.core.data.remote.adapter

import com.codepunk.core.domain.model.NetworkError
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A [JsonAdapter] that converts JSON date strings into [NetworkError] instances.
 */
@Singleton
class NetworkErrorJsonAdapter @Inject constructor() : JsonAdapter<NetworkError>() {

    // region Inherited methods

    /**
     * Converts a JSON date to a [NetworkError] instance.
     */
    override fun fromJson(reader: JsonReader): NetworkError? {
        val error = reader.nextString()
        return NetworkError.lookup(error)
    }

    /**
     * Converts a [NetworkError] to a JSON string.
     */
    override fun toJson(writer: JsonWriter, value: NetworkError?) {
        writer.value(value?.error)
    }

    // endregion Inherited methods

}
