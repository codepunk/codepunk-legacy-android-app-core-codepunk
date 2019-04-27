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

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.Toast
import com.codepunk.core.R
import com.codepunk.doofenschmirtz.util.taskinator.*
import com.google.android.material.snackbar.Snackbar
import java.net.ConnectException
import java.net.SocketTimeoutException

abstract class DataUpdateResolver<Progress, Result> {

    // region Properties

    private val context: Context

    private var view: View? = null

    // endregion Properties

    // region Constructors

    constructor(activity: Activity) {
        context = activity
    }

    // endregion Constructors

    // region Methods

    fun with(view: View): DataUpdateResolver<Progress, Result> {
        this.view = view
        return this
    }

    open fun resolve(update: DataUpdate<Progress, Result>) {
        when (update) {
            is PendingUpdate -> onPending(update)
            is ProgressUpdate -> onProgress(update)
            is SuccessUpdate -> onSuccess(update)
            is FailureUpdate -> {
                when (update.e) {
                    is ConnectException,
                    is SocketTimeoutException -> onException(update)
                    else -> onFailure(update)
                }
            }
        }
    }

    open fun onPending(update: PendingUpdate<Progress, Result>) {}

    open fun onProgress(update: ProgressUpdate<Progress, Result>) {}

    open fun onSuccess(update: SuccessUpdate<Progress, Result>) {}

    open fun onFailure(update: FailureUpdate<Progress, Result>) {}

    open fun onException(update: FailureUpdate<Progress, Result>) {
        when (update.e) {
            is ConnectException -> {
                view?.also {
                    Snackbar.make(
                        it,
                        R.string.alert_connect_exception_message,
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
            is SocketTimeoutException -> {
                view?.also {
                    Snackbar.make(
                        it,
                        R.string.alert_timeout_exception_message,
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    // endregion Methods

}
