/*
 * Copyright (C) 2018 Codepunk, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.codepunk.core.ui.settings

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.codepunk.core.di.qualifier.ApplicationContext
import javax.inject.Inject

/**
 * The [ViewModel] that stores developer options-related preference data.
 */
class MainSettingsViewModel @Inject constructor(

    /**
     * The application [Context].
     */
    @ApplicationContext val applicationContext: Context

) : ViewModel() {

    // region Properties

    /**
     * A string indicating the current version of this app.
     */
    var appVersion = MutableLiveData<String>()

    /**
     * Initializes the data maintained by this ViewModel.
     */
    init {
        appVersion.value = applicationContext
            .packageManager.getPackageInfo(applicationContext.packageName, 0)
            .versionName
    }

    // endregion Properties

}
