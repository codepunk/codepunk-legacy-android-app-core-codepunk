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

import android.app.AlertDialog
import android.content.DialogInterface
import android.view.View
import androidx.fragment.app.Fragment
import com.codepunk.core.R
import com.codepunk.core.presentation.base.AlertDialogFragment
import com.codepunk.doofenschmirtz.util.taskinator.*
import com.google.android.material.snackbar.Snackbar
import java.net.ConnectException
import java.util.concurrent.TimeoutException

abstract class DataUpdateResolver<Progress, Result> {

    // region Methods

    fun resolve(update: DataUpdate<Progress, Result>) {
        val requestCode = when (update) {
            is PendingUpdate -> onPending(update).let { request ->
                when (request) {
                    REQUEST_DEFAULT -> REQUEST_NONE
                    else -> request
                }
            }
            is ProgressUpdate -> onProgress(update).let { request ->
                when (request) {
                    REQUEST_DEFAULT -> REQUEST_NONE
                    else -> request
                }
            }
            is SuccessUpdate -> onSuccess(update).let { request ->
                when (request) {
                    REQUEST_DEFAULT -> REQUEST_SUCCESS
                    else -> request
                }
            }
            is FailureUpdate -> {
                val exceptionRequest = update.e?.let { e ->
                    resolveException(e, update)
                } ?: REQUEST_DEFAULT
                when (exceptionRequest) {
                    REQUEST_DEFAULT -> onFailure(update).let { request ->
                        when (request) {
                            REQUEST_DEFAULT -> REQUEST_FAILURE
                            else -> request
                        }
                    }
                    else -> exceptionRequest
                }
            }
            else -> REQUEST_NONE
        }

        if (requestCode != REQUEST_NONE) {
            onAction(update, requestCode)
        }
    }

    private fun resolveException(e: Exception, update: FailureUpdate<Progress, Result>): Int =
        onException(e, update).let { request ->
            when (request) {
                REQUEST_DEFAULT -> getRequestCode(e)
                else -> request
            }
        }

    abstract fun onAction(update: DataUpdate<Progress, Result>, action: Int)

    open fun onPending(update: PendingUpdate<Progress, Result>): Int = REQUEST_DEFAULT

    open fun onProgress(update: ProgressUpdate<Progress, Result>): Int = REQUEST_DEFAULT

    open fun onSuccess(update: SuccessUpdate<Progress, Result>): Int = REQUEST_DEFAULT

    open fun onFailure(update: FailureUpdate<Progress, Result>): Int = REQUEST_DEFAULT

    open fun onException(
        e: Exception,
        update: FailureUpdate<Progress, Result>
    ): Int = REQUEST_DEFAULT

    open fun showAlertDialog(fragment: Fragment, tag: String, requestCode: Int) =
        fragment.requireFragmentManager().findFragmentByTag(tag)
            ?: AlertDialogFragment.showDialogFragmentForResult(fragment, tag, requestCode)

    open fun showSnackbar(view: View, requestCode: Int) {
        Snackbar.make(
            view,
            "",
            Snackbar.LENGTH_LONG
        ).apply {
            onBuildSnackbar(requestCode, this)
        }.show()
    }

    open fun onBuildSnackbar(
        requestCode: Int,
        snackbar: Snackbar
    ) {
        // TODO Can I streamline this?
        when (requestCode) {
            REQUEST_CONNECT_EXCEPTION -> {
                snackbar.setText(R.string.alert_connect_exception_message)
                /*
                .setPositiveButton(android.R.string.ok, onClickListener)
                .setNeutralButton(R.string.alert_retry, onClickListener)
                */
            }
            REQUEST_TIMEOUT_EXCEPTION -> {
                snackbar.setText(R.string.alert_timeout_exception_message)
                /*
                .setPositiveButton(android.R.string.ok, onClickListener)
                .setNeutralButton(R.string.alert_retry, onClickListener)
                */
            }
            REQUEST_FAILURE -> {
                snackbar.setText(R.string.alert_unknown_error_message)
                /*
                .setPositiveButton(android.R.string.ok, onClickListener)
                */
            }
        }
    }

    open fun onBuildAlertDialog(
        requestCode: Int,
        builder: AlertDialog.Builder,
        onClickListener: DialogInterface.OnClickListener
    ) {
        when (requestCode) {
            REQUEST_CONNECT_EXCEPTION -> {
                builder.setTitle(R.string.alert_connect_exception_title)
                    .setMessage(R.string.alert_connect_exception_message)
                    .setPositiveButton(android.R.string.ok, onClickListener)
                    .setNeutralButton(R.string.alert_retry, onClickListener)
            }
            REQUEST_TIMEOUT_EXCEPTION -> {
                builder.setTitle(R.string.alert_error)
                    .setMessage(R.string.alert_timeout_exception_message)
                    .setPositiveButton(android.R.string.ok, onClickListener)
                    .setNeutralButton(R.string.alert_retry, onClickListener)
            }
            REQUEST_FAILURE -> {
                builder.setTitle(R.string.alert_error)
                    .setMessage(R.string.alert_unknown_error_message)
                    .setPositiveButton(android.R.string.ok, onClickListener)
            }
        }
    }

    // endregion Methods

    // region Companion object

    companion object {

        // region Properties

        const val REQUEST_NONE = Integer.MIN_VALUE

        private const val REQUEST_FIRST: Int = -0xDAADAE

        const val REQUEST_DEFAULT = REQUEST_FIRST

        const val REQUEST_SUCCESS = REQUEST_FIRST + 1

        const val REQUEST_FAILURE = REQUEST_FIRST + 2

        const val REQUEST_CONNECT_EXCEPTION = REQUEST_FIRST + 3

        const val REQUEST_TIMEOUT_EXCEPTION = REQUEST_FIRST + 4

        // endregion Properties

        // region Methods

        @JvmStatic
        private fun getRequestCode(e: Exception): Int = when (e) {
            is ConnectException -> REQUEST_CONNECT_EXCEPTION
            is TimeoutException -> REQUEST_TIMEOUT_EXCEPTION
            else -> REQUEST_DEFAULT
        }

        // endregion Methods

    }

    // endregion Companion object

}
