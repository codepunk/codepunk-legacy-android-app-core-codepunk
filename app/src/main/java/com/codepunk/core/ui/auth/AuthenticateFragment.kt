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

package com.codepunk.core.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.codepunk.core.R
import com.codepunk.core.databinding.FragmentAuthenticateBinding

/**
 * A [Fragment] that shows options for authenticating into the app.
 */
class AuthenticateFragment :
    Fragment(),
    View.OnClickListener {

    // region Properties

    /**
     * The binding for this fragment.
     */
    private lateinit var binding: FragmentAuthenticateBinding

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
            R.layout.fragment_authenticate,
            container,
            false
        )
        return binding.root
    }

    // endregion Lifecycle methods

    // region Inherited methods

    /**
     * Sets up listeners.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.createBtn.setOnClickListener(this)
        binding.loginBtn.setOnClickListener(this)
    }

    // endregion Inherited methods

    // region Implemented methods

    /**
     * Handles onClick events.
     */
    override fun onClick(v: View?) {
        when (v) {
            binding.createBtn -> Navigation.findNavController(v).navigate(
                R.id.action_authenticate_to_create_account
            )
            binding.loginBtn -> Navigation.findNavController(v).navigate(
                R.id.action_authenticate_to_log_in
            )
        }
    }

    // endregion Implemented methods

}
