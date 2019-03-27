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

package com.codepunk.core.util

import android.content.Context
import com.codepunk.core.R
import java.util.regex.Pattern

/**
 * A constant value that allows translated and non-translated strings to be modified in order
 * to determine translated vs. non-translated results.
 */
private const val DEBUG = false

/**
 * A utility class that attempts to translate messages, errors and other strings returned from the
 * network.
 */
class NetworkTranslator(private val context: Context) {

    // region Properties

    /**
     * An array of input regex pattern strings stored as resource IDs. Used for translation of
     * strings that contain arguments.
     */
    private val patternInputs: Array<Int> = arrayOf(
        R.string.translator_pattern_input_confirmed,
        R.string.translator_pattern_input_email,
        R.string.translator_pattern_input_min,
        R.string.translator_pattern_input_max,
        R.string.translator_pattern_input_regex,
        R.string.translator_pattern_input_required,
        R.string.translator_pattern_input_string,
        R.string.translator_pattern_input_unique,
        R.string.translator_pattern_input_username_regex
    )

    /**
     * An array of output format strings stored as resource IDs. Used for translation of strings
     * that contain arguments.
     */
    private val patternOutputs: Array<Int> = arrayOf(
        R.string.translator_pattern_output_confirmed,
        R.string.translator_pattern_output_email,
        R.string.translator_pattern_output_min,
        R.string.translator_pattern_output_max,
        R.string.translator_pattern_output_regex,
        R.string.translator_pattern_output_required,
        R.string.translator_pattern_output_string,
        R.string.translator_pattern_output_unique,
        R.string.translator_pattern_output_username_regex
    )

    /**
     * An array of input strings stored as resource IDs. Used for straight translation of strings
     * without arguments.
     */
    private val stringInputs: Array<Int> = arrayOf(
        R.string.translator_string_input_email,
        R.string.translator_string_input_invalid,
        R.string.translator_string_input_password,
        R.string.translator_string_input_sent_activation_code,
        R.string.translator_string_input_username
    )

    /**
     * An array of output strings stored as resource IDs. Used for straight translation of strings
     * without arguments.
     */
    private val stringOutputs: Array<Int> = arrayOf(
        R.string.translator_string_output_email,
        R.string.translator_string_output_invalid,
        R.string.translator_string_output_password,
        R.string.translator_string_output_sent_activation_code,
        R.string.translator_string_output_username
    )

    /**
     * A map of input [Pattern]s to their associated output string resource ID.
     */
    private val patternList = ArrayList<Pair<Pattern, Int>>(patternInputs.size).apply {
        try {
            patternInputs.forEachIndexed { index, resId ->
                add(Pair(Pattern.compile(context.getString(resId)), patternOutputs[index]))
            }
        } catch (e: IndexOutOfBoundsException) {
            throw IllegalStateException(
                "Number of pattern inputs does not equal number of pattern outputs",
                e
            )
        }
    }

    /**
     * A map of input strings to their associated output string resource ID.
     */
    private val stringMap = HashMap<String, Int>(stringInputs.size).apply {
        try {
            stringInputs.forEachIndexed { index, resId ->
                put(context.getString(resId), stringOutputs[index])
            }
        } catch (e: IndexOutOfBoundsException) {
            throw IllegalStateException(
                "Number of string inputs does not equal number of string outputs",
                e
            )
        }
    }

    // endregion Properties

    // region Methods

    /**
     * Attempts to translate a [string] based on maps of known strings and regex patterns. The
     * [stringMap] map is consulted first and if an exact match is found, returns the translated
     * string. Next, the [patternList] is traversed to find a matching pattern. If one is found,
     * then the translated string with mapped arguments is returned. Note that an attempt is
     * also made to translate the arguments themselves before insertion into the translated
     * string. If no matches are found, the original string is returned unchanged.
     */
    fun translate(string: String?): String? = when {
        string == null -> null
        stringMap.containsKey(string) -> stringMap[string]?.let { resId ->
            format(context.getString(resId), true)
        }
        else -> {
            val formatArgs = ArrayList<String?>()
            patternList.find { pair ->
                val matcher = pair.first.matcher(string)
                val matches = matcher.matches()
                if (matches) {
                    val groupCount = matcher.groupCount()
                    for (group in 1..groupCount) {
                        formatArgs.add(translate(matcher.group(group)))
                    }
                }
                matches
            }?.let { found ->
                format(context.getString(found.second, *formatArgs.toArray()), true)
            } ?: format(string, false)
        }
    }

    /**
     * Utility method that optionally modifies any successfully-translated string. This allows
     * for debugging to see what strings were translated and what strings were not translated.
     */
    private fun format(string: String, translated: Boolean): String = when {
        !DEBUG -> string
        translated  -> "[[$string]]"
        else -> "<<$string>>"
    }

    // endregion Methods

}
