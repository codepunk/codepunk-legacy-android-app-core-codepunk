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

import android.os.Bundle
import androidx.fragment.app.FragmentManager
import com.codepunk.core.domain.model.NetworkResponse
import com.codepunk.core.presentation.base.AlertDialogFragment
import com.codepunk.doofenschmirtz.util.taskinator.*

/*
 * TODO
 * Maybe add tag and requestCode as class-wide. We'll need a separate resolver for every query though?
 * Maybe add a firstErrorKey and firstErrorValue getters to NetworkResponse. Would simplify extractKeyFields below.
 */

open class NetworkResponseDataUpdateResolver<Progress> {

    // region Properties

    open var fragmentManager: FragmentManager? = null

    private val keyFields: KeyFields = KeyFields()

    // endregion Properties

    // region Methods

    open fun resolve(update: DataUpdate<Progress, NetworkResponse>, tag: String, requestCode: Int) {
        when (update) {
            is PendingUpdate -> onPending(update.data, tag, requestCode)
            is ProgressUpdate -> { /* no op onProgress(update.data, update.progress, tag, requestCode) */ }
            is SuccessUpdate -> {
                extractKeyFields(update, keyFields)
                if (!onSuccess(update.data, keyFields.msg, tag, requestCode)) {
                    onDefaultAction(tag, requestCode)
                }
            }
            is FailureUpdate -> {
                extractKeyFields(update, keyFields)
                with(keyFields) {
                    if (!onFailure(update.data, msg, errorKey, errorValue, e, tag, requestCode)) {
                        onDefaultAction(tag, requestCode)
                    }
                }
            }
        }
    }

    open fun onPending(data: Bundle?, tag: String, requestCode: Int): Boolean = false

    open fun onProgress(
        data: Bundle?,
        progress: Array<out Progress?>,
        tag: String,
        requestCode: Int
    ): Boolean = false

    open fun onSuccess(data: Bundle?, msg: String?, tag: String, requestCode: Int): Boolean = false

    open fun onFailure(
        data: Bundle?,
        msg: String?,
        errorKey: String?,
        errorValue: String?,
        e: Exception?,
        tag: String,
        requestCode: Int
    ) = false

    open fun onDefaultAction(tag: String, requestCode: Int) {
        fragmentManager?.also {
            it.findFragmentByTag(tag)
                ?: AlertDialogFragment.showDialogFragmentForResult(it, tag, requestCode)
        }
    }

    // endregion Methods

    // region Nested/inner classes

    private class KeyFields {

        // region Properties

        var msg: String? = null
        var errorKey: String? = null
        var errorValue: String? = null
        var e: Exception? = null

        // endregion Properties

        // region Methods

        fun reset() {
            msg = null
            errorKey = null
            errorValue = null
            e = null
        }

        // endregion Methods

    }

    // endregion Nested/inner classes

    // region Companion object

    companion object {

        // region Methods

        private fun <Progress> extractKeyFields(
            update: ResultUpdate<Progress, NetworkResponse>,
            keyFields: KeyFields
        ) {
            keyFields.reset()
            val e: Exception? = (update as? FailureUpdate)?.e
            val response = (update as? ResultUpdate)?.result
            keyFields.e = e
            response?.apply {
                keyFields.msg = this.message
                this.errors?.entries?.firstOrNull()?.also {
                    keyFields.errorKey = it.key
                    keyFields.errorValue = it.value.firstOrNull()
                }
            }
        }

        // endregion Methods

    }

    // endregion Companion object

}
