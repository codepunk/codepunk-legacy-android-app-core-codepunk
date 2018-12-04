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

package com.codepunk.core.lib

import android.os.Bundle
import java.lang.Exception
import java.util.*

/**
 * A sealed class representing the various possible updates from a [DataTask].
 */
@Suppress("UNUSED")
sealed class DataUpdate<Progress, Result>

/**
 * A [DataUpdate] representing a pending task (i.e. a task that has not been executed yet).
 */
class PendingUpdate<Progress, Result>(

    /**
     * Optional additional data to send along with the update.
     */
    var data: Bundle? = null

) : DataUpdate<Progress, Result>() {

    // region Inherited methods

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PendingUpdate<*, *>

        if (data != other.data) return false

        return true
    }

    override fun hashCode(): Int {
        return data?.hashCode() ?: 0
    }

    override fun toString(): String = "${javaClass.simpleName}(data=$data)"

    // endregion Inherited methods
}

/**
 * A [DataUpdate] representing a task that is currently in progress (i.e. a running task).
 */
class ProgressUpdate<Progress, Result>(

    /**
     * The values indicating progress of the task.
     */
    val progress: Array<out Progress?>,

    /**
     * Optional additional data to send along with the update.
     */
    var data: Bundle? = null

) : DataUpdate<Progress, Result>() {

    // region Inherited methods

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProgressUpdate<*, *>

        if (!progress.contentEquals(other.progress)) return false
        if (data != other.data) return false

        return true
    }

    override fun hashCode(): Int {
        var result = progress.contentHashCode()
        result = 31 * result + (data?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String = javaClass.simpleName +
            "(progress=${Arrays.toString(progress)}, data=$data)"

    // endregion Inherited methods

}

/**
 * An abstract class that represents a DataUpdate that contains a result (whether success
 * or failure).
 */
abstract class ResultUpdate<Progress, Result>(

    /**
     * The result of the operation computed by the task.
     */
    val result: Result?,

    /**
     * Optional additional data to send along with the update.
     */
    var data: Bundle?

) : DataUpdate<Progress, Result>()

/**
 * A [DataUpdate] representing a finished task (i.e. a task that has finished without being
 * cancelled).
 */
class SuccessUpdate<Progress, Result>(

    /**
     * The result of the operation computed by the task.
     */
    result: Result? = null,

    /**
     * Optional additional data to send along with the update.
     */
    data: Bundle? = null

) : ResultUpdate<Progress, Result>(result, data) {

    // region Inherited methods

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SuccessUpdate<*, *>

        if (result != other.result) return false
        if (data != other.data) return false

        return true
    }

    override fun hashCode(): Int {
        var result1 = result?.hashCode() ?: 0
        result1 = 31 * result1 + (data?.hashCode() ?: 0)
        return result1
    }

    override fun toString(): String =
        "${javaClass.simpleName}(result=$result, data=$data)"

    // endregion Inherited methods

}

/**
 * A [DataUpdate] representing a failed task (i.e. a task that was cancelled or experienced
 * some other sort of error during execution).
 */
class FailureUpdate<Progress, Result>(

    /**
     * The result, if any, computed by the task. Can be null.
     */
    result: Result? = null,

    /**
     * The [Exception] associated with this failure.
     */
    val e: Exception? = null,

    /**
     * Optional additional data to send along with the update.
     */
    data: Bundle? = null

) : ResultUpdate<Progress, Result>(result, data) {

    // region Inherited methods

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FailureUpdate<*, *>

        if (result != other.result) return false
        if (e != other.e) return false
        if (data != other.data) return false

        return true
    }

    override fun hashCode(): Int {
        var result1 = result?.hashCode() ?: 0
        result1 = 31 * result1 + (e?.hashCode() ?: 0)
        result1 = 31 * result1 + (data?.hashCode() ?: 0)
        return result1
    }

    override fun toString(): String =
        "${javaClass.simpleName}(result=$result, e=$e, data=$data)"

    // endregion Inherited methods

}
