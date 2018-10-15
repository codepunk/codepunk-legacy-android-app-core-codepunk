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

package com.codepunk.core.data.repository

import retrofit2.Response
import java.util.*

/**
 * A sealed class representing the possible published results from a [DataTask].
 */
@Suppress("UNUSED")
sealed class DataUpdate<Progress, Result>

/**
 * A [DataUpdate] representing a pending task (i.e. a task that has not been executed yet).
 */
class PendingUpdate<Progress, Result> : DataUpdate<Progress, Result>()

/**
 * A [DataUpdate] representing a running task (i.e. a task that is running).
 */
data class LoadingUpdate<Progress, Result>(

    /**
     * The values indicating progress of the task.
     */
    val progress: Array<out Progress?>

) : DataUpdate<Progress, Result>() {

    // region Inherited methods

    /**
     * Ensures equality of [progress] in testing for equality.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LoadingUpdate<*, *>

        if (!Arrays.equals(progress, other.progress)) return false

        return true
    }

    /**
     * Includes [progress] in the computation of the hash code.
     */
    override fun hashCode(): Int {
        return Arrays.hashCode(progress)
    }

    // endregion Inherited methods

}

/**
 * A [DataUpdate] representing a finished task (i.e. a task that has finished without being
 * cancelled).
 */
data class SuccessUpdate<Progress, Result>(

    /**
     * The result of the operation computed by the task.
     */
    val result: Result? = null,

    /**
     * The [Response] that resulted in this update.
     */
    val response: Response<Result>? = null

) : DataUpdate<Progress, Result>()

/**
 * A [DataUpdate] representing a cancelled task (i.e. a task that was cancelled or experienced
 * some other sort of error during execution).
 */
data class FailureUpdate<Progress, Result>(

    /**
     * The result, if any, computed by the task. Can be null.
     */
    val result: Result? = null,

    /**
     * The [Response] that resulted in this update.
     */
    val response: Response<Result>? = null

) : DataUpdate<Progress, Result>()
