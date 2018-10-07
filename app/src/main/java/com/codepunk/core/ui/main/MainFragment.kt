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

import android.accounts.AccountManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.codepunk.core.BuildConfig
import com.codepunk.core.R
import com.codepunk.core.databinding.FragmentMainBinding
import com.codepunk.core.di.component.MainFragmentComponent
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 */
@Suppress("unused")
class MainFragment : Fragment() {

    // region Properties

    /**
     * A [MainFragmentComponent] instance used to inject dependencies into this fragment.
     */
    private lateinit var mainFragmentComponent: MainFragmentComponent

    /**
     * The application [SharedPreferences].
     */
    @Inject
    lateinit var sharedPreferences: SharedPreferences

    /**
     * The [AccountManager] that provides access to a centralized registry of the user's online
     * accounts.
     */
    @Inject
    lateinit var accountManager: AccountManager

    /**
     * The binding for this fragment.
     */
    private lateinit var binding: FragmentMainBinding

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

    /**
     * Updates the view.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val text = getString(BuildConfig.DEFAULT_REMOTE_ENVIRONMENT.nameResId) // TODO TEMP
        binding.text1.text = getString(R.string.dependency_injection_message, text)
    }

    // endregion Lifecycle methods

}
