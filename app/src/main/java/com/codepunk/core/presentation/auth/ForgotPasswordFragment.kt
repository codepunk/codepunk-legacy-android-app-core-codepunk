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

package com.codepunk.core.presentation.auth

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.ContentLoadingProgressBar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import com.codepunk.core.BuildConfig.KEY_REMOTE_ERROR_BODY
import com.codepunk.core.R
import com.codepunk.core.data.remote.entity.RemoteErrorBody
import com.codepunk.core.databinding.FragmentForgotPasswordBinding
import com.codepunk.core.domain.model.Message
import com.codepunk.core.lib.hideSoftKeyboard
import com.codepunk.core.presentation.base.ContentLoadingProgressBarOwner
import com.codepunk.core.presentation.base.FloatingActionButtonOwner
import com.codepunk.core.presentation.base.FloatingActionButtonOwner.FloatingActionButtonListener
import com.codepunk.core.util.ResourceResolver
import com.codepunk.core.util.NetworkTranslator
import com.codepunk.core.util.setSupportActionBarTitle
import com.codepunk.doofenschmirtz.util.http.HttpStatusException
import com.codepunk.doofenschmirtz.util.loginator.FormattingLoginator
import com.codepunk.doofenschmirtz.util.resourceinator.Resource
import com.codepunk.doofenschmirtz.util.resourceinator.FailureResource
import com.codepunk.doofenschmirtz.util.resourceinator.ProgressResource
import com.codepunk.doofenschmirtz.util.resourceinator.SuccessResource
import com.google.android.material.textfield.TextInputLayout
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

/**
 * A simple [Fragment] subclass. TODO
 */
class ForgotPasswordFragment :
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
     * The binding for this fragment.
     */
    private lateinit var binding: FragmentForgotPasswordBinding

    /**
     * The [AuthViewModel] instance backing this fragment.
     */
    private val authViewModel: AuthViewModel by lazy {
        ViewModelProviders.of(requireActivity(), viewModelFactory)
            .get(AuthViewModel::class.java)
    }

    /**
     * The [NetworkTranslator] for translating messages from the network.
     */
    @Inject
    lateinit var networkTranslator: NetworkTranslator

    /**
     * This fragment's activity cast to a [FloatingActionButtonOwner].
     */
    private val floatingActionButtonOwner: FloatingActionButtonOwner? by lazy {
        activity as? FloatingActionButtonOwner
    }

    private lateinit var sendPasswordResetLinkResolver: SendPasswordResetLinkResolver

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
     * Inflates the view.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_forgot_password,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sendPasswordResetLinkResolver = SendPasswordResetLinkResolver(requireActivity(), view)

        authViewModel.sendPasswordResetLiveResource.observe(
            this,
            Observer { sendPasswordResetLinkResolver.resolve(it) }
        )
    }

    /**
     * Listens for floating action button events.
     */
    override fun onResume() {
        super.onResume()
        setSupportActionBarTitle(R.string.authenticate_label_forgot_password)
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

    override fun onFloatingActionButtonClick(owner: FloatingActionButtonOwner) {
        view?.hideSoftKeyboard()
        if (validate()) {
            authViewModel.sendPasswordResetLink(binding.emailEdit.text.toString())
        }

    }

    // endregion Implemented methods

    // region Methods

    /**
     * Validates the form.
     */
    private fun validate(): Boolean {
        resetErrors()
        // return validatinators.registerValidatinator.validate(binding, options.clear())
        return true
    }

    private fun disableView() {
        // TODO disable controls (or at least the button)
    }

    private fun enableView() {
        // TODO enable controls
    }

    private fun resetErrors() {
        binding.emailLayout.error = null
    }

    private fun resetView() {
        binding.emailEdit.text = null
    }

    // endregion Methods

    // region Nested/inner classes

    private inner class SendPasswordResetLinkResolver(activity: Activity, view: View) :
        ResourceResolver<Void, Message>(activity, view) {

        override fun resolve(resource: Resource<Void, Message>) {
            // TODO This is exactly the same as the other fragments in this activity
            when (resource) {
                is ProgressResource -> disableView()
                else -> enableView()
            }
            super.resolve(resource)
        }

        override fun onSuccess(resource: SuccessResource<Void, Message>): Boolean {
            resetView()
            Navigation.findNavController(view).navigate(R.id.action_forgot_password_to_log_in)
            return true
        }

        override fun onFailure(resource: FailureResource<Void, Message>): Boolean {
            // TODO This is exactly the same as RegisterFragment
            var handled = super.onFailure(resource)
            if (!handled) {
                val remoteErrorBody =
                    resource.data?.getParcelable<RemoteErrorBody>(KEY_REMOTE_ERROR_BODY)
                remoteErrorBody?.errors?.also { errors ->
                    errors.entries.forEach { error ->
                        view?.findViewWithTag<TextInputLayout>(error.key)?.also { layout ->
                            layout.post {
                                layout.error = networkTranslator.translate(error.value.first())
                            }
                            handled = true
                        }
                    }
                }
            }

            if (!handled) {
                when (val e = resource.e) {
                    is HttpStatusException -> {
                        // ???
                        // handled = true
                    }
                }
            }

            return handled
        }
    }

    // endregion Nested/inner classes

}
