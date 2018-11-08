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

package com.codepunk.core.ui.main

import android.app.PendingIntent
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.codepunk.core.BuildConfig
import com.codepunk.core.R
import com.codepunk.core.data.model.User
import com.codepunk.core.databinding.FragmentMainBinding
import com.codepunk.core.lib.*
import com.codepunk.core.ui.auth.AuthViewModel
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 */
@Suppress("unused")
class MainFragment :
    Fragment(),
    View.OnClickListener {

    // region Properties

    /**
     * Performs dependency injection on fragments.
     */
    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    /**
     * A [ViewModelProvider.Factory] for creating [ViewModel] instances.
     */
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    /**
     * The binding for this fragment.
     */
    private lateinit var binding: FragmentMainBinding

    /**
     * An instance of [AuthViewModel] for managing account-related data.
     */
    private val mainViewModel: MainViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(MainViewModel::class.java)
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
     * Inflates the view.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_main,
            container,
            false
        )
        return binding.root
    }

    // endregion Lifecycle methods

    // region Inherited methods

    /**
     * Updates the view.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.logInOutBtn.setOnClickListener(this)
        mainViewModel.liveUser.observe(this, Observer { onUserUpdate(it) })
    }

    // endregion Inherited methods

    // region Implemented methods

    override fun onClick(v: View?) {
        when (v) {
            binding.logInOutBtn -> {
                when (mainViewModel.liveUser.value) {
                    is PendingUpdate -> mainViewModel.authenticate()
                    is SuccessUpdate -> mainViewModel.logOut()
                    is FailureUpdate -> mainViewModel.authenticate()
                    else -> { // No op
                    }
                }
            }
        }
    }

    // endregion Implemented methods

    // region Methods

    private fun onUserUpdate(update: DataUpdate<Void, User>) {
        Log.d("MainActivity", "onUserUpdate: update=$update")
        when (update) {
            is PendingUpdate -> {
                binding.text1.setText(R.string.hello)
                binding.logInOutBtn.setText(R.string.main_log_in)
                binding.logInOutBtn.isEnabled = true
            }
            is ProgressUpdate -> {
                binding.text1.setText(R.string.main_logging_in)
                binding.logInOutBtn.setText(R.string.main_log_in)
                binding.logInOutBtn.isEnabled = false
            }
            is SuccessUpdate -> {
                binding.text1.text = getString(R.string.hello_user, update.result?.givenName)
                binding.logInOutBtn.setText(R.string.main_log_out)
                binding.logInOutBtn.isEnabled = true
            }
            is FailureUpdate -> {
                // Log.i?
                val userAction: PendingIntent? =
                    update.data?.getParcelable(BuildConfig.KEY_PENDING_INTENT)
                userAction?.send()
                // TODO Message when no pending intent found
                binding.logInOutBtn.setText(R.string.main_log_in)
                binding.logInOutBtn.isEnabled = true
            }
        }
    }

    // endregion Methods

}
