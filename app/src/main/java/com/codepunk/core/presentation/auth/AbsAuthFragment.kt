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

package com.codepunk.core.presentation.auth

import android.content.Context
import android.view.View
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.codepunk.core.BuildConfig.KEY_REMOTE_ERROR_BODY
import com.codepunk.core.data.remote.entity.RemoteErrorBody
import com.codepunk.core.lib.hideSoftKeyboard
import com.codepunk.core.presentation.base.FloatingActionButtonOwner
import com.codepunk.core.presentation.base.FloatingActionButtonOwner.FloatingActionButtonListener
import com.codepunk.doofenschmirtz.util.Translatinator
import com.codepunk.doofenschmirtz.util.loginator.FormattingLoginator
import com.codepunk.doofenschmirtz.util.resourceinator.FailureResource
import com.codepunk.doofenschmirtz.util.resourceinator.ProgressResource
import com.codepunk.doofenschmirtz.util.resourceinator.Resource
import com.codepunk.doofenschmirtz.util.resourceinator.ResourceResolvinator
import com.google.android.material.textfield.TextInputLayout
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

/**
 * An abstract Fragment class that handles common authentication-related view-based tasks.
 */
abstract class AbsAuthFragment :
    Fragment(),
    FloatingActionButtonListener {

    // region Properties

    /**
     * A [FormattingLoginator] for writing log messages.
     */
    @Inject
    lateinit var loginator: FormattingLoginator

    /**
     * The injected [ViewModelProvider.Factory] that we will use to get an instance of
     * [AuthViewModel].
     */
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    /**
     * The [Translatinator] for translating messages from the network.
     */
    @Inject
    lateinit var translatinator: Translatinator

    /**
     * This fragment's activity cast to a [FloatingActionButtonOwner].
     */
    private val floatingActionButtonOwner: FloatingActionButtonOwner? by lazy {
        activity as? FloatingActionButtonOwner
    }

    /**
     * The [AuthViewModel] instance backing this fragment.
     */
    protected val authViewModel: AuthViewModel by lazy {
        ViewModelProviders.of(requireActivity(), viewModelFactory)
            .get(AuthViewModel::class.java)
    }

    // endregion Properties

    // endregion Properties

    // region Lifecycle methods

    /**
     * Injects dependencies into this fragment.
     */
    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    /**
     * Listens for appropriate events.
     */
    override fun onResume() {
        super.onResume()
        floatingActionButtonOwner?.floatingActionButtonListener = this
    }

    /**
     * Removes any associated listeners.
     */
    override fun onPause() {
        super.onPause()
        if (floatingActionButtonOwner?.floatingActionButtonListener == this) {
            floatingActionButtonOwner?.floatingActionButtonListener = null
        }
    }

    // endregion Lifecycle methods

    // region Implemented methods

    /**
     * Reacts to the user clicking the activity's floating action button.
     */
    override fun onFloatingActionButtonClick(owner: FloatingActionButtonOwner) {
        view?.hideSoftKeyboard()
    }

    // endregion Implemented methods

    // region Methods

    /**
     * Clears any errors from any [TextInputLayout] (or other) widgets in this fragment.
     */
    protected open fun clearErrors() {
        // No op
    }

    /**
     * Disables any appropriate widgets in this fragment while network- or other-related processes
     * are in progress.
     */
    protected open fun disableView() {
        // No op
    }

    /**
     * Enables any appropriate widgets in this fragment once network- or other-related processes
     * have completed.
     */
    protected open fun enableView() {
        // No op
    }

    /**
     * Resets any [EditText] (or other) widgets in this fragment.
     */
    protected open fun resetView() {
        // No op
    }

    /**
     * Validates the form.
     */
    protected open fun validate(): Boolean {
        clearErrors()
        return true
    }

    // endregion Methods

    // region Nested/inner classes

    /**
     * An abstract [ResourceResolvinator] that parses and possibly responds to [Resource]s supplied to
     * the [resolve] method.
     */
    protected abstract inner class AbsAuthResolvinator<Progress, Result>(
        view: View,
        context: Context = view.context
    ) : ResourceResolvinator<Progress, Result>(view, context) {

        // region Inherited methods

        /**
         * Enables or disables the view depending on the type of the [resource].
         */
        override fun resolve(resource: Resource<Progress, Result>) {
            when (resource) {
                is ProgressResource -> disableView()
                else -> enableView()
            }
            super.resolve(resource)
        }

        /**
         * Attempts to display an error in a [RemoteErrorBody] if a [TextInputLayout] exists
         * with the given key.
         */
        override fun onFailure(resource: FailureResource<Progress, Result>): Boolean {
            var handled = super.onFailure(resource)
            if (!handled) {
                resource.data?.also { data ->
                    if (data.containsKey(KEY_REMOTE_ERROR_BODY)) {
                        val remoteErrorBody =
                            resource.data?.getParcelable<RemoteErrorBody>(KEY_REMOTE_ERROR_BODY)
                        remoteErrorBody?.errors?.also { errors ->
                            errors.entries.forEach { error ->
                                view.findViewWithTag<TextInputLayout>(error.key)?.also { layout ->
                                    layout.error = translatinator.translate(error.value.first())
                                    handled = true
                                }
                            }
                        }
                    }
                }
            }
            return handled
        }
    }

    // endregion Nested/inner classes

}
