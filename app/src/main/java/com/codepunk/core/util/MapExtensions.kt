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

package com.codepunk.core.util

import android.os.Bundle
import java.util.*

/**
 * Extension method that converts a map to a bundle.
 */
fun Map<String, Array<String>>?.toBundle(): Bundle? {
    return when {
        this == null -> null
        else -> Bundle().apply {
            mapKeys { entry -> putStringArray(entry.key, entry.value) }
        }
    }
}

/**
 * Extension method that converts a bundle to an unmodifiable map of strings to string arrays.
 */
fun toMap(bundle: Bundle?): Map<String, Array<String>>? {
    return when (bundle) {
        null -> null
        else -> {
            val keySet = bundle.keySet()
            Collections.unmodifiableMap(
                HashMap<String, Array<String>>(keySet.size).apply {
                    keySet.forEach { key ->
                        this[key] = bundle.getStringArray(key) ?: emptyArray()
                    }
                }
            )
        }
    }
}
