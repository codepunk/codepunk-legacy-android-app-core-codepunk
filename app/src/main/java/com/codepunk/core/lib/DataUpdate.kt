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

import retrofit2.Response
import java.lang.Exception
import java.util.*

/**
 * A sealed class representing the various possible updates from a [DataTask].
 */
@Suppress("UNUSED")
sealed class DataUpdate<Progress, Result> : HashMap<String, Any>()

/**
 * A [DataUpdate] representing a pending task (i.e. a task that has not been executed yet).
 */
class PendingUpdate<Progress, Result> : DataUpdate<Progress, Result>() {

    // region Inherited methods

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int = javaClass.hashCode()

    override fun toString(): String = "${javaClass.simpleName}(map=${super.toString()})"

    // endregion Inherited methods
}

/**
 * A [DataUpdate] representing a running task (i.e. a task that is running).
 */
class LoadingUpdate<Progress, Result>(

    /**
     * The values indicating progress of the task.
     */
    val progress: Array<out Progress?>

) : DataUpdate<Progress, Result>() {

    // region Inherited methods

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LoadingUpdate<*, *>

        if (!Arrays.equals(progress, other.progress)) return false

        return true
    }

    override fun hashCode(): Int = Arrays.hashCode(progress)

    override fun toString(): String = javaClass.simpleName +
            "(progress=${Arrays.toString(progress)}, map=${super.toString()})"

    // endregion Inherited methods

}

/**
 * A [DataUpdate] representing a finished task (i.e. a task that has finished without being
 * cancelled).
 */
class SuccessUpdate<Progress, Result>(

    /**
     * The result of the operation computed by the task.
     */
    val result: Result? = null,

    /**
     * The [Response] that resulted in this update.
     */
    val response: Response<Result>? = null

) : DataUpdate<Progress, Result>() {

    // region Inherited methods

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as SuccessUpdate<*, *>

        if (result != other.result) return false
        if (response != other.response) return false

        return true
    }

    override fun hashCode(): Int {
        var result1 = super.hashCode()
        result1 = 31 * result1 + (result?.hashCode() ?: 0)
        result1 = 31 * result1 + (response?.hashCode() ?: 0)
        return result1
    }

    override fun toString(): String =
        "${javaClass.simpleName}(result=$result, response=$response, map=${super.toString()})"

    // endregion Inherited methods

}

/**
 * A [DataUpdate] representing a cancelled task (i.e. a task that was cancelled or experienced
 * some other sort of error during execution).
 */
class FailureUpdate<Progress, Result>(

    /**
     * The result, if any, computed by the task. Can be null.
     */
    val result: Result? = null,

    /**
     * The [Exception] associated with this failure.
     */
    val e: Exception? = null,

    /**
     * The [Response] that resulted in this update.
     */
    val response: Response<Result>? = null

) : DataUpdate<Progress, Result>() {

    // region Inherited methods

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as FailureUpdate<*, *>

        if (result != other.result) return false
        if (e != other.e) return false
        if (response != other.response) return false

        return true
    }

    override fun hashCode(): Int {
        var result1 = super.hashCode()
        result1 = 31 * result1 + (result?.hashCode() ?: 0)
        result1 = 31 * result1 + (e?.hashCode() ?: 0)
        result1 = 31 * result1 + (response?.hashCode() ?: 0)
        return result1
    }

    override fun toString(): String =
        "${javaClass.simpleName}(result=$result, response=$response, e=$e, map=${super.toString()})"

    // endregion Inherited methods

}
