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

package com.codepunk.core.presentation.base

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import androidx.annotation.StringRes
import com.codepunk.core.R
import java.net.ConnectException
import javax.inject.Inject

/**
 * A helper class for [AlertDialogFragment] that builds common alert dialogs.
 */
open class AlertDialogFragmentHelper(private val context: Context) {

    // region Properties

    /**
     * A default title for all [AlertDialog]s created by this helper.
     */
    protected var _defaultTitle: String = context.getString(R.string.app_name)

    // endregion Properties

    // region Methods

    /**
     * Builds common [AlertDialog]s.
     */
    fun onBuildAlertDialog(
        requestCode: Int,
        builder: AlertDialog.Builder,
        defaultOnClickListener: DialogInterface.OnClickListener
    ) {
        builder.setTitle(_defaultTitle)
        when (requestCode) {
            REQUEST_CODE_CONNECT_EXCEPTION -> {
                builder.setMessage(R.string.alert_dialog_connect_exception_message)
                builder.setPositiveButton(android.R.string.ok, defaultOnClickListener)
                builder.setNeutralButton(R.string.alert_dialog_retry, defaultOnClickListener)
            }
        }
    }

    /**
     * Sets the default title displayed in [AlertDialog]s created by this helper using the given
     * [titleId].
     */
    fun setDefaultTitle(@StringRes titleId: Int) {
        _defaultTitle = context.getString(titleId)
    }

    /**
     * Sets the default [title] displayed in [AlertDialog]s created by this helper.
     */
    fun setDefaultTitle(title: String) {
        _defaultTitle = title
    }

    // endregion Methods

    // region Companion object

    companion object {

        /**
         * The first request code used by this helper. For a touch of randomness to avoid
         * clashes, it is set to the hash code of the name of this class.
         */
        @JvmStatic
        private val REQUEST_CODE_FIRST: Int =
            AlertDialogFragmentHelper::class.java.name.hashCode()

        /**
         * A request code corresponding to a [ConnectException].
         */
        @JvmStatic
        val REQUEST_CODE_CONNECT_EXCEPTION: Int = REQUEST_CODE_FIRST

        /**
         * The start of user-defined request codes.
         */
        @JvmStatic
        val REQUEST_CODE_FIRST_USER = REQUEST_CODE_CONNECT_EXCEPTION + 1

    }

    // endregion Companion object

    // region Nested/inner classes

    class Factory @Inject constructor() {

        // region Methods

        fun create(context: Context): AlertDialogFragmentHelper = AlertDialogFragmentHelper(context)

        // endregion Methods

    }

    // endregion Nested/inner classes

}
