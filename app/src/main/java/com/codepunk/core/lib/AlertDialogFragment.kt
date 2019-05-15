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

package com.codepunk.core.lib

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.DialogInterface.BUTTON_NEGATIVE
import android.content.DialogInterface.BUTTON_NEUTRAL
import android.content.DialogInterface.BUTTON_POSITIVE
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.codepunk.core.BuildConfig.KEY_LISTENER_SOURCE
import com.codepunk.core.BuildConfig.KEY_REQUEST_CODE
import com.codepunk.core.R

/**
 * A generic [DialogFragment] that uses request codes and "showDialogFragmentForResult" methods
 * to show [Dialog]s in a manner consistent with [Activity.startActivityForResult].
 */
open class AlertDialogFragment :
    DialogFragment(),
    DialogInterface.OnClickListener,
    DialogInterface.OnMultiChoiceClickListener {

    // region Properties

    /**
     * The identity of the listener assigned to this [AlertDialogFragment]. Can be one of the
     * following:
     * - The [Activity] to which this fragment belongs
     * - The [Fragment] supplied as this fragment's target fragment
     * - Some other custom listener instance
     * - None, i.e. this dialog fragment has no listener
     */
    protected var listenerIdentity =
        ListenerIdentity.NONE
        set(value) {
            if (field != value) {
                field = value
                _listener = when (field) {
                    ListenerIdentity.ACTIVITY -> activity as? AlertDialogFragmentListener
                    ListenerIdentity.TARGET_FRAGMENT -> targetFragment as? AlertDialogFragmentListener
                    else -> null
                }
            }
        }

    /**
     * The backing field for the [listener] property.
     */
    private var _listener: AlertDialogFragmentListener? = null

    /**
     * The [AlertDialogFragmentListener] currently listening for events related to this fragment.
     */
    var listener: AlertDialogFragmentListener?
        get() = _listener
        set(value) {
            if (_listener != value) {
                listenerIdentity = when (_listener) {
                    null -> ListenerIdentity.NONE
                    else -> ListenerIdentity.CUSTOM
                }
                _listener = value
            }
        }

    /**
     * The backing field for the [requestCode] property.
     */
    private var _requestCode: Int = 0

    /**
     * The request code that this fragment will use to communicate with [listener].
     */
    protected var requestCode: Int
        get() = when (listenerIdentity) {
            ListenerIdentity.TARGET_FRAGMENT -> targetRequestCode
            else -> _requestCode
        }
        set(value) {
            _requestCode = value
        }

    /**
     * The result code that will be passed to [listener].
     */
    protected var resultCode: Int = 0

    /**
     * Any data that will be shared with [listener] via
     * [AlertDialogFragmentListener.onDialogResult].
     */
    var data: Intent? = null

    // endregion Properties

    // region Lifecycle methods

    /*
    /**
     * If the listener identity is this fragment's activity, capture it here.
     */
    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (listenerIdentity == ListenerIdentity.ACTIVITY) {
            _listener = activity as? AlertDialogFragmentListener
        }
    }
    */

    /**
     * Restores [listenerIdentity] and [requestCode] from [savedInstanceState] as appropriate.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.apply {
            listenerIdentity = this.getSerializable(KEY_LISTENER_SOURCE) as ListenerIdentity
            if (listenerIdentity != ListenerIdentity.TARGET_FRAGMENT) {
                requestCode = this.getInt(KEY_REQUEST_CODE)
            }
        }
    }

    /**
     * Saves [listenerIdentity] and [requestCode] to [outState] as appropriate.
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(KEY_LISTENER_SOURCE, listenerIdentity)
        if (listenerIdentity != ListenerIdentity.TARGET_FRAGMENT) {
            outState.putInt(KEY_REQUEST_CODE, requestCode)
        }
    }

    // endregion Lifecycle methods

    // region Inherited methods

    /**
     * Creates the [Dialog] for this fragment, calling
     * [AlertDialogFragmentListener.onBuildAlertDialog] on [listener] if one exists.
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        defaultAlertDialogBuilder().also { builder ->
            listener?.onBuildAlertDialog(this, requestCode, builder)
        }.create()

    /**
     * Sets [resultCode] to [RESULT_CANCELED] in preparation for calling
     * [AlertDialogFragmentListener.onDialogResult].
     */
    override fun onCancel(dialog: DialogInterface?) {
        super.onCancel(dialog)
        resultCode = RESULT_CANCELED
    }

    /**
     * Call [AlertDialogFragmentListener.onDialogResult] on [listener] if one exists.
     */
    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        // Don't notify listener if the dialog is not attached. This will catch the
        // difference between a dialog dismissing due to configuration change vs. a user action.
        if (isAdded) {
            listener?.onDialogResult(this, requestCode, resultCode, data)
        }
    }

    // endregion Inherited methods

    // region Implemented methods

    /**
     * Implementation of [DialogInterface.OnClickListener]. Sets [resultCode] to a value
     * appropriate to the clicked dialog button.
     */
    override fun onClick(dialog: DialogInterface?, which: Int) {
        resultCode = when (which) {
            BUTTON_POSITIVE -> RESULT_POSITIVE
            BUTTON_NEGATIVE -> RESULT_NEGATIVE
            BUTTON_NEUTRAL -> RESULT_NEUTRAL
            else -> return
        }
    }

    /**
     * Implementation of [DialogInterface.OnMultiChoiceClickListener].
     */
    override fun onClick(dialog: DialogInterface?, which: Int, isChecked: Boolean) {
        // No op
    }

    // endregion Implemented methods

    // region Methods

    /**
     * A default [AlertDialog.Builder] that displays a simple "An unknown error occurred"
     * dialog with a single "OK" button.
     */
    protected open fun defaultAlertDialogBuilder(): AlertDialog.Builder =
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.app_name)
            .setMessage(com.codepunk.doofenschmirtz.R.string.unknown_error_message)
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

        /**
         * Shows an [AlertDialogFragment] for which you would like a result when it dismisses
         * (for whatever reason). If it implements [AlertDialogFragmentListener], then
         * [targetFragment] will automatically be set as the result listener.
         */
        @JvmStatic
        fun showDialogFragmentForResult(
            targetFragment: Fragment,
            requestCode: Int,
            tag: String
        ) = AlertDialogFragment().apply {
            setTargetFragment(targetFragment, requestCode)
            listenerIdentity = ListenerIdentity.TARGET_FRAGMENT
            show(targetFragment.requireFragmentManager(), tag)
        }

        /**
         * Shows an [AlertDialogFragment] for which you would like a result when it dismisses
         * (for whatever reason). If it implements [AlertDialogFragmentListener], then
         * [activity] will automatically be set as the result listener.
         */
        @JvmStatic
        fun showDialogFragmentForResult(
            activity: FragmentActivity,
            requestCode: Int,
            tag: String
        ) = AlertDialogFragment().apply {
            this.requestCode = requestCode
            listenerIdentity = ListenerIdentity.ACTIVITY
            show(activity.supportFragmentManager, tag)
        }

        /**
         * Shows an [AlertDialogFragment] for which you would like a result when it dismisses
         * (for whatever reason). This version of [showDialogFragmentForResult] specifically
         * supplies a [listener] with the caveat that, after configuration change, a handle
         * to this fragment must be obtained again (i.e. via [FragmentManager.findFragmentByTag],
         * etc.) and the listener must manually be re-set.
         */
        @Suppress("UNUSED")
        @JvmStatic
        fun showDialogFragmentForResult(
            fragmentManager: FragmentManager,
            requestCode: Int,
            tag: String,
            listener: AlertDialogFragmentListener? = null
        ) = AlertDialogFragment().apply {
            this.requestCode = requestCode
            this.listener = listener
            show(fragmentManager, tag)
        }

    }

    // endregion Companion object

    // region Nested/inner classes

    /**
     * An enum class describing the identity of any [AlertDialogFragmentListener] set on this
     * fragment.
     */
    protected enum class ListenerIdentity {

        /**
         * A value indicating that this fragment has no [AlertDialogFragmentListener].
         */
        NONE,

        /**
         * A value indicating that this fragment's [AlertDialogFragmentListener] is potentially
         * its host [Activity].
         */
        ACTIVITY,

        /**
         * A value indicating that this fragment's [AlertDialogFragmentListener] is potentially
         * its target [Fragment].
         */
        TARGET_FRAGMENT,

        /**
         * A value indicating that this fragment's [AlertDialogFragmentListener] was specifically
         * set via [showDialogFragmentForResult].
         */
        CUSTOM
    }

    /**
     * An interface that classes can implement to listen for events related to showing and
     * responding to this [AlertDialogFragment].
     */
    interface AlertDialogFragmentListener {

        // region Methods

        /**
         * Called when [AlertDialogFragment] is building its [AlertDialog]. Listeners of this
         * event should use [requestCode] to differentiate what kind of alert dialog should be
         * built. When creating dialog buttons, it's best to use [fragment] as the
         * [DialogInterface.OnClickListener] for those buttons so the AlertDialogFragment can
         * respond appropriately and call [onDialogResult] with the correct result code.
         */
        fun onBuildAlertDialog(
            fragment: AlertDialogFragment,
            requestCode: Int,
            builder: AlertDialog.Builder
        )

        /**
         * Called when the [AlertDialogFragment] encounters a result, whether from the dialog
         * being dismissed in any way, or one of the dialog buttons being pressed. Listeners of
         * this event should use [requestCode] to differentiate what kind of alert dialog is
         * passing along its result. The [data] argument may optionally contain additional
         * information (for example, an item that was selected in a list, etc.)
         */
        fun onDialogResult(
            fragment: AlertDialogFragment,
            requestCode: Int,
            resultCode: Int,
            data: Intent?
        )

        // endregion Methods

    }

    // endregion Nested/inner classes

}
