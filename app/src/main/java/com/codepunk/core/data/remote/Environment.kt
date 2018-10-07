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

package com.codepunk.core.data.remote

import androidx.annotation.StringRes
import com.codepunk.core.R

/**
 * Enum class that specifies an environment used for making API calls. Each is initialized with
 * a [nameResId] that specifies a user-friendly name for the environment.
 */
enum class Environment(

    /**
     * A string resource pointing to a user-friendly name for the environment.
     */
    @StringRes val nameResId: Int

) {

    /**
     * The production API environment.
     */
    PRODUCTION(R.string.environment_production),

    /**
     * The development API environment.
     */
    DEVELOPMENT(R.string.environment_development),

    /**
     * The local API environment.
     */
    LOCAL(R.string.environment_local)

}
