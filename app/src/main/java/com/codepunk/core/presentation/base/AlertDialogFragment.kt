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

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener as DialogOnClickListener
import android.content.DialogInterface.BUTTON_NEGATIVE
import android.content.DialogInterface.BUTTON_NEUTRAL
import android.content.DialogInterface.BUTTON_POSITIVE
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.codepunk.core.BuildConfig.KEY_LISTENER_SOURCE
import com.codepunk.core.BuildConfig.KEY_REQUEST_CODE
import com.codepunk.core.R

// TODO Finish documentation

open class AlertDialogFragment :
    DialogFragment(),
    DialogOnClickListener {

    // region Properties

    private var listenerSource: ListenerSource = ListenerSource.NONE
        set(value) {
            if (field != value) {
                field = value
                _listener = when (field) {
                    ListenerSource.ACTIVITY -> activity as? AlertDialogFragmentListener
                    ListenerSource.TARGET_FRAGMENT -> targetFragment as? AlertDialogFragmentListener
                    else -> null
                }
            }
        }

    var _listener: AlertDialogFragmentListener? = null
    var listener: AlertDialogFragmentListener? = _listener
        get() = _listener
        set(value) {
            if (field != value) {
                field = value
                listenerSource = when (field) {
                    null -> ListenerSource.NONE
                    else -> ListenerSource.CUSTOM
                }
            }
        }

    private var _requestCode: Int = 0
    private var requestCode: Int
        get() = when (listenerSource) {
            ListenerSource.TARGET_FRAGMENT -> targetRequestCode
            else -> _requestCode
        }
        set(value) {
            _requestCode = value
        }

    private var resultCode: Int = 0

    var data: Intent? = null

    // endregion Properties

    // region Lifecycle methods

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (listenerSource ==  ListenerSource.ACTIVITY) {
            _listener = activity as? AlertDialogFragmentListener
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.apply {
            listenerSource = this.getSerializable(KEY_LISTENER_SOURCE) as ListenerSource
            _requestCode = this.getInt(KEY_REQUEST_CODE)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(KEY_LISTENER_SOURCE, listenerSource)
        outState.putInt(KEY_REQUEST_CODE, _requestCode)
    }

    // endregion Lifecycle methods

    // region Inherited methods

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        defaultAlertDialogBuilder().also { builder ->
            listener?.onBuildAlertDialog(requestCode, builder, this)
        }.create()

    override fun onCancel(dialog: DialogInterface?) {
        super.onCancel(dialog)
        resultCode = Activity.RESULT_CANCELED
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        // Don't notify listener if the dialog is not attached. This will catch the
        // difference between a dialog dismissing due to configuration change vs. a user action.
        if (isAdded) {
            listener?.onDialogResult(requestCode, resultCode, data)
        }
    }

    // endregion Inherited methods

    // region Implemented methods

    override fun onClick(dialog: DialogInterface?, which: Int) {
        resultCode = when (which) {
            BUTTON_POSITIVE -> RESULT_POSITIVE
            BUTTON_NEGATIVE -> RESULT_NEGATIVE
            BUTTON_NEUTRAL -> RESULT_NEUTRAL
            else -> return
        }
    }

    // endregion Implemented methods

    // region Methods

    protected fun defaultAlertDialogBuilder() =
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.app_name)
            .setMessage(R.string.alert_unknown_error_message)
            .setPositiveButton(android.R.string.ok, this)

    // endregion Methods

    // region Companion object

    companion object {

        /**
         * A result code corresponding to a positive button click.
         */
        const val RESULT_POSITIVE: Int = Activity.RESULT_OK

        /**
         * A result code corresponding to a negative button click.
         */
        const val RESULT_NEGATIVE: Int = Activity.RESULT_FIRST_USER

        /**
         * A result code corresponding to a neutral button click.
         */
        const val RESULT_NEUTRAL: Int = Activity.RESULT_FIRST_USER + 1

        /**
         * A result code corresponding to the dialog being canceled.
         */
        const val RESULT_CANCELED: Int = Activity.RESULT_CANCELED

        @JvmStatic
        fun showDialogFragmentForResult(
            targetFragment: Fragment,
            tag: String,
            requestCode: Int
        ): AlertDialogFragment =
            AlertDialogFragment().apply {
                setTargetFragment(targetFragment, requestCode)
                listenerSource = ListenerSource.TARGET_FRAGMENT
                show(targetFragment.requireFragmentManager(), tag)
            }

        @JvmStatic
        fun showDialogFragmentForResult(
            activity: FragmentActivity,
            tag: String,
            requestCode: Int
        ): AlertDialogFragment =
            AlertDialogFragment().apply {
                //setTargetFragment(targetFragment, requestCode)
                this.requestCode = requestCode
                listenerSource = ListenerSource.ACTIVITY
                show(activity.supportFragmentManager, tag)
            }

        @JvmStatic
        fun showDialogFragmentForResult(
            fragmentManager: FragmentManager,
            tag: String,
            requestCode: Int,
            listener: AlertDialogFragmentListener? = null
        ): AlertDialogFragment =
            AlertDialogFragment().apply {
                //setTargetFragment(targetFragment, requestCode)
                this.requestCode = requestCode
                this.listener = listener
                show(fragmentManager, tag)
            }

    }

    // endregion Companion object

    // region Nested/inner classes

    private enum class ListenerSource {
        NONE,
        ACTIVITY,
        TARGET_FRAGMENT,
        CUSTOM
    }

    interface AlertDialogFragmentListener {

        fun onBuildAlertDialog(
            requestCode: Int,
            builder: AlertDialog.Builder,
            onClickListener: DialogOnClickListener
        )

        fun onDialogResult(requestCode: Int, resultCode: Int, data: Intent?)

    }

    // endregion Nested/inner classes

}
