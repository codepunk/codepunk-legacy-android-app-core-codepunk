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


import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.codepunk.core.BuildConfig.KEY_FIRST_TIME
import com.codepunk.core.R
import com.codepunk.core.data.model.User
import com.codepunk.core.databinding.FragmentAuthenticatingBinding
import com.codepunk.core.lib.FailureUpdate
import com.codepunk.core.lib.LoadingUpdate
import com.codepunk.core.lib.PendingUpdate
import com.codepunk.core.lib.SuccessUpdate
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

/**
 * A simple [Fragment] subclass. TODO
 */
class AuthenticatingFragment : Fragment() {

    // region Properties

    /**
     * The [ViewModelProvider.Factory] used to generate view models.
     */
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    /**
     * The application [SharedPreferences].
     */
    @Suppress("UNUSED")
    @Inject
    lateinit var sharedPreferences: SharedPreferences

    /**
     * The binding for this fragment.
     */
    private lateinit var binding: FragmentAuthenticatingBinding

    /**
     * The [AccountViewModel] for managing and observing account-related data.
     */
    private val accountViewModel: AccountViewModel by lazy {
        ViewModelProviders.of(
            requireActivity(),
            viewModelFactory
        ).get(AccountViewModel::class.java)
    }

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
     * Creates the view.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_authenticating,
            container,
            false
        )
        return binding.root
    }

    // endregion Lifecycle methods

    // region Inherited methods

    /**
     * Observes the user operation.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        accountViewModel.userTask.observe(this, Observer { update ->
            Log.d("AuthenticatingFragment", "Observe: update=$update")
            with(update) {
                binding.text1.text = when (this) {
                    is PendingUpdate<User, User> -> "[Pending]"
                    is LoadingUpdate<User, User> -> "Loading…"
                    is SuccessUpdate<User, User> -> "Hello, ${result?.name ?: "User"}!"
                    is FailureUpdate<User, User> -> "Error: ${e.message}"
                }
            }
        })
        when (savedInstanceState) {
            null -> accountViewModel.authenticate()
        }
    }

    /**
     * Writes [KEY_FIRST_TIME] to [outState]. Hopefully this is a temporary workaround to address
     * the fact that savedInstanceState is always null in onCreate -- even after configuration
     * change -- unless some value is written to it in [onSaveInstanceState].
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_FIRST_TIME, false) // TODO TEMP, hopefully
    }

    // endregion Inherited methods

}