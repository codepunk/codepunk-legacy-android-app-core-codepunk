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
import android.view.View
import com.codepunk.core.R
import com.codepunk.doofenschmirtz.util.resourceinator.*
import com.google.android.material.snackbar.Snackbar
import java.net.ConnectException
import java.net.SocketTimeoutException

/**
 * An abstract class that parses and resolves a [Resource], handling common errors when possible.
 */
abstract class ResourceResolver<Progress, Result>(

    /**
     * A [View] associated with this [ResourceResolver] for the purposes of showing [Snackbar]s.
     */
    var view: View,

    /**
     * A [Context] to associate with this ResourceResolver.
     */
    protected val context: Context = view.context

) : Snackbar.Callback() {

    // region Inherited methods

    /**
     * Implementation of [Snackbar.Callback]. Called when a snackbar is dismissed.
     */
    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
        // No op
    }

    // endregion Inherited methods

    // region Methods

    /**
     * Resolves the supplied [resource] by calling the appropriate method based on its type.
     */
    open fun resolve(resource: Resource<Progress, Result>) {
        when (resource) {
            is PendingResource -> onPending(resource)
            is ProgressResource -> onProgress(resource)
            is SuccessResource -> onSuccess(resource)
            is FailureResource -> if (!onFailure(resource)) onUnhandledFailure(resource)
        }
    }

    /**
     * Processes a resource of type [PendingResource].
     */
    open fun onPending(resource: PendingResource<Progress, Result>): Boolean = false

    /**
     * Processes a resource of type [ProgressResource].
     */
    open fun onProgress(resource: ProgressResource<Progress, Result>): Boolean = false

    /**
     * Processes a resource of type [SuccessResource].
     */
    open fun onSuccess(resource: SuccessResource<Progress, Result>): Boolean = false

    /**
     * Processes a resource of type [FailureResource].
     */
    open fun onFailure(resource: FailureResource<Progress, Result>): Boolean {
        return when (resource.e) {
            is ConnectException -> {
                Snackbar.make(
                    view,
                    R.string.alert_connect_exception_message,
                    Snackbar.LENGTH_LONG
                ).addCallback(this)
                    .show()
                true
            }
            is SocketTimeoutException -> {
                Snackbar.make(
                    view,
                    R.string.alert_timeout_exception_message,
                    Snackbar.LENGTH_LONG
                ).addCallback(this)
                    .show()
                true
            }
            else -> false
        }
    }

    /**
     * Handles any [resource] that remains unhandled after calling [onFailure].
     */
    open fun onUnhandledFailure(resource: FailureResource<Progress, Result>) {
        Snackbar.make(
            view,
            R.string.alert_unknown_error_message,
            Snackbar.LENGTH_LONG
        ).addCallback(this)
            .show()

        // endregion Methods

    }

}