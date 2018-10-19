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
import androidx.fragment.app.Fragment
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

    // region Methods

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
     * Valides fields.
     */
    open fun validate(): Boolean {
        for (editLayout in textInputLayouts) {
            editLayout.error = ""
        }
        return true
    }

    // endregion Methods

    // region Implemented methods

    /**
     * Listens for chenged text and calls [checkRequiredFields].
     */
    override fun afterTextChanged(s: Editable?) {
        checkRequiredFields()
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        // No op
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        // No op
    }

    // endregion Implemented methods

}
