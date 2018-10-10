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

package com.codepunk.core.data

import com.codepunk.core.data.repository.LiveDataTask

/**
 * A sealed class representing the possible published results from a [LiveDataTask].
 */
@Suppress("UNUSED")
sealed class PublishedState<Progress, Result>

/**
 * A [PublishedState] representing a pending task (i.e. a task that has not been executed yet).
 */
class PendingState<Progress, Result> : PublishedState<Progress, Result>()

/**
 * A [PublishedState] representing a running task (i.e. a task that is running).
 */
class RunningState<Progress, Result>(

    /**
     * The values indicating progress of the task.
     */
    val progress: Array<out Progress>? = null

) : PublishedState<Progress, Result>()

/**
 * A [PublishedState] representing a finished task (i.e. a task that has finished without being
 * cancelled).
 */
class FinishedState<Progress, Result>(

    /**
     * The result of the operation computed by the task.
     */
    val result: Result? = null

) : PublishedState<Progress, Result>()

/**
 * A [PublishedState] representing a cancelled task (i.e. a task that was cancelled or experienced
 * some other sort of error during execution).
 */
class CancelledState<Progress, Result>(

    /**
     * The result, if any, computed by the task. Can be null.
     */
    val result: Result? = null,

    /**
     * The exception, if any, describing the reason the task was cancelled. Can be null.
     */
    val e: Exception? = null

) : PublishedState<Progress, Result>()
