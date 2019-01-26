/*
 * Copyright (C) 2019 Codepunk, LLC
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

package com.codepunk.core.lib.room

import android.util.Log
import androidx.room.TypeConverter
import java.text.SimpleDateFormat
import java.util.*

/**
 * A [TypeConverter] that converts between [Date] and [String], using the format
 * "yyyy-MM-dd HH:mm:ss".
 */
class DateConverter {

    // region Methods

    /**
     * Converts a [String] to a [Date].
     */
    @TypeConverter
    fun toDate(string: String): Date {
        return try {
            dateFormat.parse(string)
        } catch (e: Exception) {
            // TODO This error is happening
            Log.e("DateConverter", "${e.message}; string=$string", e)
            Date()
        }
    }

    /**
     * Converts a [Date] to a [String].
     */
    @TypeConverter
    fun toString(date: Date): String = dateFormat.format(date)

    // endregion Methods

    // region Companion object

    companion object {

        // region Properties

        /**
         * The [SimpleDateFormat] to use when converting between [Date]s and [String]s.
         */
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

        // endregion Properties

    }

    // endregion Companion object}

}
