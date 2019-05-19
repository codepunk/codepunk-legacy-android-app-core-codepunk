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

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.codepunk.core.R
import com.codepunk.core.databinding.FragmentRegisterBinding
import com.codepunk.core.domain.model.Message
import com.codepunk.core.lib.consume
import com.codepunk.core.presentation.base.FloatingActionButtonOwner
import com.codepunk.doofenschmirtz.util.http.HttpStatusException
import com.codepunk.doofenschmirtz.util.resourceinator.FailureResource
import com.codepunk.doofenschmirtz.util.resourceinator.Resource
import com.codepunk.doofenschmirtz.util.resourceinator.ResourceResolvinator
import com.codepunk.doofenschmirtz.util.resourceinator.SuccessResource
import com.codepunk.punkubator.util.validatinator.Validatinator
import com.codepunk.punkubator.util.validatinator.Validatinator.Options
import com.google.android.material.snackbar.Snackbar
import javax.inject.Inject
import android.content.DialogInterface.OnClickListener as DialogOnClickListener

/**
 * A [Fragment] used to register (i.e. create) a new account.
 */
class RegisterFragment :
    AbsAuthFragment(),
    OnClickListener {

    // region Properties

    /**
     * A set of [Validatinator]s for validating the form.
     */
    @Suppress("UNUSED")
    @Inject
    lateinit var validatinators: RegisterValidatinators

    /**
     * The binding for this fragment.
     */
    private lateinit var binding: FragmentRegisterBinding

    /**
     * The default [Options] used to validate the form.
     */
    @Suppress("UNUSED")
    private val options = Options().apply {
        requestMessage = true
    }

    /**
     * An instance of [RegisterResolvinator] for resolving registration-related resources.
     */
    private lateinit var registerResolvinator: RegisterResolvinator

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
            R.layout.fragment_register,
            container,
            false
        )
        return binding.root
    }

    // endregion Lifecycle methods

    // region Inherited methods

    /**
     * Sets up form information and event listeners.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.loginBtn.setOnClickListener(this)

        registerResolvinator = RegisterResolvinator(view)

        authViewModel.registerLiveResource.removeObservers(this)
        authViewModel.registerLiveResource.observe(
            this,
            Observer { registerResolvinator.resolve(it) }
        )
    }

    override fun onFloatingActionButtonClick(owner: FloatingActionButtonOwner) {
        super.onFloatingActionButtonClick(owner)
        if (validate()) {
            authViewModel.register(
                binding.usernameEdit.text.toString(),
                binding.emailEdit.text.toString(),
                binding.passwordEdit.text.toString(),
                binding.confirmPasswordEdit.text.toString()
            )
        }
    }

    override fun clearErrors() {
        binding.usernameLayout.error = null
        binding.emailLayout.error = null
        binding.passwordLayout.error = null
        binding.confirmPasswordLayout.error = null
    }

    override fun resetView() {
        binding.usernameEdit.text = null
        binding.emailEdit.text = null
        binding.passwordEdit.text = null
        binding.confirmPasswordEdit.text = null
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

    // region Implemented methods

    /**
     * Implementation of [OnClickListener]. Submits the new account.
     */
    override fun onClick(v: View?) {
        when (v) {
            binding.loginBtn -> {
                authViewModel.registerLiveResource.consume()
                clearErrors()
                Navigation.findNavController(v)
                    .navigate(R.id.action_register_to_log_in)
            }
        }
    }

    // endregion Implemented methods

    // region Nested/inner classes

    /**
     * A [ResourceResolvinator] that resolves authorization-related [Resource]s.
     */
    private inner class RegisterResolvinator(view: View) :
        AbsAuthResolvinator<Void, Message>(view) {

        // region Inherited methods

        @SuppressLint("SwitchIntDef")
        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
            when (event) {
                DISMISS_EVENT_ACTION, DISMISS_EVENT_SWIPE, DISMISS_EVENT_TIMEOUT ->
                    authViewModel.registerLiveResource.consume()
            }
        }

        override fun onSuccess(resource: SuccessResource<Void, Message>): Boolean {
            resetView()
            Navigation.findNavController(view).navigate(R.id.action_register_to_log_in)
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
