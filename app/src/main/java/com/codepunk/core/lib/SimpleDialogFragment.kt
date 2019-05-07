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

package com.codepunk.core.lib

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment

// TODO Kill this class

/*
 * Author(s): Scott Slater
 */

/**
 * A generic dialog fragment for displaying information to the user and/or getting a simple
 * response.
 */
class SimpleDialogFragment :
    AppCompatDialogFragment(),
    DialogInterface.OnClickListener {

    // region Inherited methods

    /**
     * Builds the [Dialog] that confirms that the user wishes to disable developer options.
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val title = arguments?.getCharSequence(KEY_TITLE)
        val message = arguments?.getCharSequence(KEY_MESSAGE)
        val positiveButton = arguments?.getCharSequence(KEY_POSITIVE_BUTTON)
        val negativeButton = arguments?.getCharSequence(KEY_NEGATIVE_BUTTON)
        val neutralButton = arguments?.getCharSequence(KEY_NEUTRAL_BUTTON)
        val builder = AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
        positiveButton?.also { builder.setPositiveButton(it, this) }
        negativeButton?.also { builder.setNegativeButton(it, this) }
        neutralButton?.also { builder.setNeutralButton(it, this) }
        return builder.create()
    }

    // endregion Inherited methods

    // region Implemented methods

    /**
     * Returns the result of the user's choice back to the target fragment.
     */
    override fun onClick(dialog: DialogInterface?, which: Int) {
        val resultCode = when (which) {
            Dialog.BUTTON_POSITIVE -> RESULT_POSITIVE
            Dialog.BUTTON_NEGATIVE -> RESULT_NEGATIVE
            Dialog.BUTTON_NEUTRAL -> RESULT_NEUTRAL
            else -> Activity.RESULT_CANCELED
        }
        targetFragment?.onActivityResult(targetRequestCode, resultCode, null)
    }

    // endregion Implemented methods

    // region Companion object

    companion object {

        // region Properties

        /**
         * A bundle key specifying the dialog title.
         */
        @JvmStatic
        private val KEY_TITLE = "${SimpleDialogFragment::class.java.name}.TITLE"

        /**
         * A bundle key specifying the dialog message.
         */
        @JvmStatic
        private val KEY_MESSAGE = "${SimpleDialogFragment::class.java.name}.MESSAGE"

        /**
         * A bundle key specifying the positive button text (or null for no positive button).
         */
        @JvmStatic
        private val KEY_POSITIVE_BUTTON = "${SimpleDialogFragment::class.java.name}.POSITIVE_BUTTON"

        /**
         * A bundle key specifying the negative button text (or null for no negative button).
         */
        @JvmStatic
        private val KEY_NEGATIVE_BUTTON = "${SimpleDialogFragment::class.java.name}.NEGATIVE_BUTTON"

        /**
         * A bundle key specifying the neutral button text (or null for no neutral button).
         */
        @JvmStatic
        private val KEY_NEUTRAL_BUTTON = "${SimpleDialogFragment::class.java.name}.NEUTRAL_BUTTON"

        /**
         * An activity result indicating that the positive button was clicked.
         */
        const val RESULT_POSITIVE = Activity.RESULT_FIRST_USER

        /**
         * An activity result indicating that the negative button was clicked.
         */
        const val RESULT_NEGATIVE = Activity.RESULT_FIRST_USER + 1

        /**
         * An activity result indicating that the neutral button was clicked.
         */
        const val RESULT_NEUTRAL = Activity.RESULT_FIRST_USER + 2

        // endregion Properties

    }

    // endregion Companion object

    // region Nested/inner classes

    /**
     * A convenience class for constructing a [SimpleDialogFragment].
     */
    class Builder(private val context: Context) {

        /**
         * The title of the dialog.
         */
        private var title: CharSequence? = null

        /**
         * The dialog message.
         */
        private var message: CharSequence? = null

        /**
         * The text of the positive button (or null for no positive button).
         */
        private var positiveButton: CharSequence? = null

        /**
         * The text of the negative button (or null for no negative button).
         */
        private var neutralButton: CharSequence? = null

        /**
         * The text of the neutral button (or null for no neutral button).
         */
        private var negativeButton: CharSequence? = null

        /**
         * Sets the title using the given resource id.
         */
        @Suppress("UNUSED")
        fun setTitle(@StringRes titleId: Int): Builder {
            title = context.getString(titleId)
            return this
        }

        /**
         * Sets the title displayed in the dialog.
         */
        @Suppress("UNUSED")
        fun setTitle(title: CharSequence?): Builder {
            this.title = title
            return this
        }

        /**
         * Sets the message to display using the given resource id.
         */
        @Suppress("UNUSED")
        fun setMessage(@StringRes messageId: Int): Builder {
            message = context.getString(messageId)
            return this
        }

        /**
         * Sets the message to display.
         */
        @Suppress("UNUSED")
        fun setMessage(message: CharSequence?): Builder {
            this.message = message
            return this
        }

        /**
         * Sets the text of the positive button using the given resource id.
         */
        @Suppress("UNUSED")
        fun setPositiveButton(@StringRes textId: Int): Builder {
            positiveButton = context.getString(textId)
            return this
        }

        /**
         * Sets the text of the positive button.
         */
        @Suppress("UNUSED")
        fun setPositiveButton(text: CharSequence?): Builder {
            positiveButton = text
            return this
        }

        /**
         * Sets the text of the negative button using the given resource id.
         */
        @Suppress("UNUSED")
        fun setNegativeButton(@StringRes textId: Int): Builder {
            negativeButton = context.getString(textId)
            return this
        }

        /**
         * Sets the text of the negative button.
         */
        @Suppress("UNUSED")
        fun setNegativeButton(text: CharSequence?): Builder {
            negativeButton = text
            return this
        }

        /**
         * Sets the text of the neutral button using the given resource id.
         */
        @Suppress("UNUSED")
        fun setNeutralButton(@StringRes textId: Int): Builder {
            neutralButton = context.getString(textId)
            return this
        }

        /**
         * Sets the text of the neutral button.
         */
        @Suppress("UNUSED")
        fun setNeutralButton(text: CharSequence?): Builder {
            neutralButton = text
            return this
        }

        /**
         * Creates a [SimpleDialogFragment] with the arguments supplied to this builder.
         */
        fun build(): SimpleDialogFragment = SimpleDialogFragment().apply {
            arguments = Bundle().apply {
                putCharSequence(KEY_TITLE, title)
                putCharSequence(KEY_MESSAGE, message)
                positiveButton?.also {
                    putCharSequence(KEY_POSITIVE_BUTTON, it)
                }
                negativeButton?.also {
                    putCharSequence(KEY_NEGATIVE_BUTTON, it)
                }
                neutralButton?.also {
                    putCharSequence(KEY_NEUTRAL_BUTTON, it)
                }
            }
        }
    }

    // endregion Nested/inner classes

}
