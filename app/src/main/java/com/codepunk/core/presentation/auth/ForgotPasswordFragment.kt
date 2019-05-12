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
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.codepunk.core.R
import com.codepunk.core.databinding.FragmentForgotPasswordBinding
import com.codepunk.core.domain.model.Message
import com.codepunk.core.presentation.base.FloatingActionButtonOwner
import com.codepunk.doofenschmirtz.util.http.HttpStatusException
import com.codepunk.doofenschmirtz.util.resourceinator.FailureResource
import com.codepunk.doofenschmirtz.util.resourceinator.SuccessResource

/**
 * A simple [Fragment] subclass. TODO
 */
class ForgotPasswordFragment :
    AbsAuthFragment() {

    // region Inherited properties

    override val titleResId: Int = R.string.authenticate_label_forgot_password

    // endregion Inherited properties

    // region Properties

    /**
     * The binding for this fragment.
     */
    private lateinit var binding: FragmentForgotPasswordBinding

    private lateinit var sendPasswordResetLinkResolver: SendPasswordResetLinkResolver

    // endregion Properties

    // region Lifecycle methods

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

    // endregion Lifecycle methods

    // region Inherited methods

    override fun onFloatingActionButtonClick(owner: FloatingActionButtonOwner) {
        super.onFloatingActionButtonClick(owner)
        if (validate()) {
            authViewModel.sendPasswordResetLink(binding.emailEdit.text.toString())
        }

    }

    override fun clearErrors() {
        binding.emailLayout.error = null
    }

    override fun resetView() {
        binding.emailEdit.text = null
    }

    /**
     * Validates the form.
     */
    @Suppress("REDUNDANT_OVERRIDING_METHOD")
    override fun validate(): Boolean {
        // return validatinators.registerValidatinator.validate(binding, options.clear())
        return super.validate()
    }

    // endregion Inherited methods

    // region Nested/inner classes

    private inner class SendPasswordResetLinkResolver(activity: Activity, view: View) :
        AbsAuthResolver<Void, Message>(activity, view) {

        // region Inherited methods

        override fun onSuccess(resource: SuccessResource<Void, Message>): Boolean {
            resetView()
            Navigation.findNavController(view).navigate(R.id.action_forgot_password_to_log_in)
            return true
        }

        override fun onFailure(resource: FailureResource<Void, Message>): Boolean {
            val handled = super.onFailure(resource)

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

        // endregion Inherited methods

    }

    // endregion Nested/inner classes

}
