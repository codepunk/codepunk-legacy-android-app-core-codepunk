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
import com.codepunk.doofenschmirtz.util.http.HttpStatusException
import com.codepunk.doofenschmirtz.util.resourceinator.FailureResource
import com.codepunk.doofenschmirtz.util.resourceinator.ResourceResolvinator
import com.codepunk.doofenschmirtz.util.resourceinator.SuccessResource

/**
 * A [Fragment] that allows the user to request a password reset link in case they have forgotten
 * their password.
 */
class ForgotPasswordFragment :
    AbsAuthFragment() {

    // region Properties

    /**
     * The binding for this fragment.
     */
    private lateinit var binding: FragmentForgotPasswordBinding

    /**
     * An instance of [SendPasswordResetLinkResolvinator] for resolving resources related to
     * requesting a password reset link.
     */
    private lateinit var sendPasswordResetLinkResolvinator: SendPasswordResetLinkResolvinator

    // endregion Properties

    // region Lifecycle methods

    /**
     * Inflates the view.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_forgot_password,
            container,
            false
        )
        return binding.root
    }

    /**
     * Sets up resolvers and observers.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sendPasswordResetLinkResolvinator = SendPasswordResetLinkResolvinator(view)

        authViewModel.sendPasswordResetLiveResource.observe(
            this,
            Observer { sendPasswordResetLinkResolvinator.resolve(it) }
        )
    }

    // endregion Lifecycle methods

    // region Inherited methods

    override fun onClick(v: View?) {
        super.onClick(v)
        when (v) {
            floatingActionButton -> if (validate()) {
                authViewModel.sendPasswordResetLink(binding.emailEdit.text.toString())
            }
        }
    }

    override fun clearErrors() {
        binding.emailLayout.error = null
    }

    override fun resetView() {
        binding.emailEdit.text = null
    }

    @Suppress("REDUNDANT_OVERRIDING_METHOD")
    override fun validate(): Boolean {
        // return validatinators.registerValidatinator.validate(binding, options.clear())
        return super.validate()
    }

    // endregion Inherited methods

    // region Nested/inner classes

    /**
     * A [ResourceResolvinator] that resolves the results of requesting a password reset link
     */
    private inner class SendPasswordResetLinkResolvinator(view: View) :
        AbsAuthResolvinator<Void, Message>(view) {

        // region Inherited methods

        override fun onSuccess(resource: SuccessResource<Void, Message>): Boolean {
            resetView()
            Navigation.findNavController(view).navigate(R.id.action_forgot_password_to_log_in)
            return true
        }

        override fun onFailure(resource: FailureResource<Void, Message>): Boolean {
            val handled = super.onFailure(resource)

            if (!handled) {
                when (/* val e = */ resource.e) {
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
