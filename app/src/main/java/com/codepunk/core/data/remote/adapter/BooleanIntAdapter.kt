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

import com.codepunk.core.data.remote.annotation.BooleanInt
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import javax.inject.Inject

/**
 * A JSON adapter that converts between integers and booleans.
 */
@Suppress("UNUSED")
class BooleanIntAdapter @Inject constructor() {

    // region Methods

    /**
     * Converts a boolean to a JSON integer.
     */
    @ToJson
    fun toJson(@BooleanInt value: Boolean): Int {
        return when (value) {
            true -> 1
            else -> 0
        }
    }

    /**
     * Converts a JSON integer to a boolean.
     */
    @FromJson
    @BooleanInt
    fun fromJson(value: Int): Boolean {
        return when (value) {
            0 -> false
            else -> true
        }
    }

    // endregion Methods

}
