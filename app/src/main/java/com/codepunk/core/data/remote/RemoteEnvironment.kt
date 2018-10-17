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
import com.codepunk.core.BuildConfig
import com.codepunk.core.R

/**
 * Enum class that specifies an environment used for making API calls. Each is initialized with
 * a [nameResId] that specifies a user-friendly name for the environment.
 */
@Suppress("UNUSED")
enum class RemoteEnvironment(

    /**
     * A string resource pointing to a user-friendly name for the environment.
     */
    @StringRes val nameResId: Int,

    /**
     * The client ID for this environment.
     */
    val clientId: String,

    /**
     * The client secret for this environment.
     */
    val clientSecret: String

) {

    // region Values

    /**
     * The production API environment.
     */
    PRODUCTION(
        R.string.environment_production,
        BuildConfig.CODEPUNK_PROD_CLIENT_ID,
        BuildConfig.CODEPUNK_PROD_CLIENT_SECRET
    ),

    /**
     * The development API environment.
     */
    DEVELOPMENT(
        R.string.environment_development,
        BuildConfig.CODEPUNK_DEV_CLIENT_ID,
        BuildConfig.CODEPUNK_DEV_CLIENT_SECRET
    ),

    /**
     * The local API environment.
     */
    LOCAL(
        R.string.environment_local,
        BuildConfig.CODEPUNK_LOCAL_CLIENT_ID,
        BuildConfig.CODEPUNK_LOCAL_CLIENT_SECRET
    )

    // endregion Values

}
