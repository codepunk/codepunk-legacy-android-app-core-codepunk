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

package com.codepunk.codepunk.ui.main

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.codepunk.codepunk.R
import com.codepunk.codepunk.databinding.FragmentMainBinding
import com.codepunk.codepunk.di.*
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
     * This is just a dependency injection test.
     */
    @Inject
    lateinit var sharedPreferences: SharedPreferences // TODO TEMP

    /**
     * This is just a dependency injection test.
     */
    @Inject
    lateinit var applicationTestObject: ApplicationTestObject // TODO TEMP

    /**
     * This is just a dependency injection test.
     */
    @Inject
    lateinit var singletonInjectedTestObject: SingletonInjectedTestObject // TODO TEMP

    /**
     * This is just a dependency injection test.
     */
    @Inject
    lateinit var activityTestObject: ActivityTestObject // TODO TEMP

    /**
     * This is just a dependency injection test.
     */
    @Inject
    lateinit var fragmentTestObject: FragmentTestObject // TODO TEMP

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
        try {
            mainFragmentComponent = (context as HasMainFragmentComponentBuilder)
                .mainFragmentComponentBuilder
                .fragment(this)
                .build()
            mainFragmentComponent.inject(this)
        } catch (e: ClassCastException) {
            throw IllegalStateException(
                "Parent activity of ${MainFragment::class.java.simpleName} must implement " +
                        HasMainFragmentComponentBuilder::class.java.simpleName,
                e
            )
        }

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
        val text = fragmentTestObject.text
        binding.text1.text = getString(R.string.dependency_injection_message, text)
    }

    // endregion Lifecycle methods

}
