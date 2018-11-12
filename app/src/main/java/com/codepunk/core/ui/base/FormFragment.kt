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

package com.codepunk.core.ui.base

import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.codepunk.core.ui.auth.LogInFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

/**
 * A helper [Fragment] that facilitates entering and validating forms.
 */
open class FormFragment :
    Fragment(),
    TextWatcher {

    // region Properties

    /**
     * A set of all controls that should be automatically enabled/disabled during network
     * operations.
     */
    private val controls = HashSet<View>()

    /**
     * A set of all [TextInputLayout]s in the fragment.
     */
    private val textInputLayouts = HashSet<TextInputLayout>()

    /**
     * A set of all required fields in the fragment.
     */
    private val requiredFields = HashSet<View>()

    // endregion Properties

    // region Inherited methods

    /**
     * Removes all text changed listeners.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        for (view in requiredFields) {
            (view as? EditText)?.removeTextChangedListener(this)
        }
    }

    // region Inherited methods

    // region Implemented methods

    /**
     * Listens for changed text and calls [checkRequiredFields].
     */
    override fun afterTextChanged(s: Editable?) {
        checkRequiredFields()
    }

    /**
     * Called before text changes.
     */
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        // No op
    }

    /**
     * Called after text changes.
     */
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        // No op
    }

    // endregion Implemented methods

    // region Methods

    /**
     * Adds [View]s to the set. All views in this set will be enabled or disabled when
     * [setControlsEnabled] is called.
     */
    protected open fun addControls(vararg views: View) {
        controls.addAll(views)
    }

    /**
     * Adds [TextInputLayout]s to the set. All layouts in this set will be cleared of errors
     * prior to validating.
     */
    protected open fun addTextInputLayouts(vararg layouts: TextInputLayout) {
        textInputLayouts.addAll(layouts)
    }

    /**
     * Adds [TextInputEditText]s to the set of required edit texts. All edit texts in this set will
     * be watched for text changes.
     */
    protected open fun addRequiredFields(vararg views: View) {
        requiredFields.addAll(views)
        for (view in requiredFields) {
            (view as? EditText)?.addTextChangedListener(this)
        }
        checkRequiredFields()
    }

    /**
     * Enables or disables the controls in [controls].
     */
    open fun setControlsEnabled(enabled: Boolean) {
        controls.forEach {
            when (it) {
                is TextInputLayout -> it.isEnabled = enabled
                is TextView -> it.isEnabled = enabled
            }
        }
    }

    /**
     * Checks required fields for valid input.
     */
    open fun checkRequiredFields(): Boolean {
        for (view in requiredFields) {
            when (view) {
                is EditText -> if (TextUtils.isEmpty(view.text)) {
                    onRequiredFieldMissing(view)
                    return false
                }
            }
        }
        onRequiredFieldsComplete()
        return true
    }

    /**
     * Called from [checkRequiredFields] when a required field is found to be empty.
     */
    open fun onRequiredFieldMissing(view: View) {}

    /**
     * Called from [checkRequiredFields] when all required fields are populated.
     */
    open fun onRequiredFieldsComplete() {}

    /**
     * Validates fields.
     */
    open fun validate(): Boolean {
        for (editLayout in textInputLayouts) {
            editLayout.error = ""
        }
        return true
    }

    // endregion Methods

    // region Companion object

    companion object {

        // region Properties

        /**
         * The fragment tag to use for the authentication failure dialog fragment.
         */
        @JvmStatic
        protected val AUTHENTICATION_FAILURE_DIALOG_FRAGMENT_TAG =
            LogInFragment::class.java.name + ".AUTHENTICATION_FAILURE_DIALOG"

        // endregion Properties

    }

    // endregion Companion object

}
