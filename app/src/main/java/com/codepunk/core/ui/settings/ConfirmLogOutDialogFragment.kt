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

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import com.codepunk.core.R

/**
 * A simple "OK/Cancel" dialog fragment that confirms that user wishes to disable developer
 * options.
 */
class ConfirmLogOutDialogFragment : AppCompatDialogFragment(),
    DialogInterface.OnClickListener {

    // region Inherited methods

    /**
     * Builds the [Dialog] that confirms that the user wishes to disable developer options.
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val appName = getString(R.string.app_name)
        return AlertDialog.Builder(requireContext())
            .setTitle(R.string.settings_log_out_dialog_title)
            .setMessage(getString(R.string.settings_log_out_dialog_message, appName))
            .setPositiveButton(android.R.string.yes, this)
            .setNegativeButton(android.R.string.no, this)
            .create()
    }

    // endregion Inherited methods

    // region Implemented methods

    /**
     * Returns the result of the user's choice back to the target fragment (or to the activity
     * if the target fragment is not present).
     */
    override fun onClick(dialog: DialogInterface?, which: Int) {
        val resultCode =
            if (which == DialogInterface.BUTTON_POSITIVE) Activity.RESULT_OK
            else Activity.RESULT_CANCELED
        targetFragment?.onActivityResult(targetRequestCode, resultCode, null)
    }

    // endregion Implemented methods

}
