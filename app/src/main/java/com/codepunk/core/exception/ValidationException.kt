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

package com.codepunk.core.exception

import android.annotation.TargetApi
import android.os.Build
import java.lang.RuntimeException

class ValidationException : RuntimeException {

    // region Properties

    /**
     * Any errors (relating to specific keys) that were discovered during validation, localized
     * for the current locale.
     */
    val errors: Map<String, Array<String>>?

    // endregion Properties

    // region Constructors

    constructor(errors: Map<String, Array<String>>? = null) : super() {
        this.errors = errors
    }

    constructor(message: String?, errors: Map<String, Array<String>>? = null) : super(message) {
        this.errors = errors
    }

    constructor(
        message: String?,
        cause: Throwable?,
        errors: Map<String, Array<String>>? = null
    ) : super(message, cause) {
        this.errors = errors
    }

    constructor(cause: Throwable?, errors: Map<String, Array<String>>? = null) : super(cause) {
        this.errors = errors
    }

    @TargetApi(Build.VERSION_CODES.N)
    constructor(
        message: String?,
        cause: Throwable?,
        enableSuppression: Boolean,
        writableStackTrace: Boolean,
        errors: Map<String, Array<String>>? = null
    ) : super(message, cause, enableSuppression, writableStackTrace) {
        this.errors = errors
    }

    // endregion Constructors

    // region Methods

    fun firstErrorOrNull(): Pair<String, String>? {
        val entry = errors?.entries?.firstOrNull()
        return entry?.let {
            Pair(entry.key, it.value.getOrElse(0) { "" })
        }
    }

    // endregion Methods

}
