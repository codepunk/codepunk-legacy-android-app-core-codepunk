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

package com.codepunk.core.presentation.settings

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.codepunk.core.BuildConfig
import com.codepunk.core.R
import com.codepunk.core.databinding.DialogDeveloperOptionsRemoteUrlBinding
import com.codepunk.core.lib.AlertDialogFragment
import com.codepunk.core.util.addClearButton
import com.codepunk.core.util.makeKey
import dagger.android.support.AndroidSupportInjection
import retrofit2.Retrofit
import javax.inject.Inject

class DeveloperOptionsRemoteUrlDialogFragment :
    AlertDialogFragment(),
    DialogInterface.OnShowListener,
    View.OnClickListener {

    // region Properties

    /**
     * The application [SharedPreferences].
     */
    @Inject
    lateinit var sharedPreferences: SharedPreferences

    /**
     * The binding for this fragment.
     */
    private lateinit var binding: DialogDeveloperOptionsRemoteUrlBinding

    /**
     * The (optional) [Button] that represents a positive response by the user.
     */
    private val positiveBtn by lazy {
        (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
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

    // endregion Lifecycle methods

    // region Inherited methods

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).also { dialog ->
            dialog.setOnShowListener(this)
        }
    }

    override fun newBuilder(savedInstanceState: Bundle?): AlertDialog.Builder {
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
            R.layout.dialog_developer_options_remote_url,
            null,
            false
        )
        binding.edit.addClearButton()
        binding.edit.setText(sharedPreferences.getString(BuildConfig.PREF_KEY_REMOTE_URL, null))

        return AlertDialog.Builder(requireContext())
            .setTitle(R.string.settings_developer_options_remote_url_dialog_title)
            .setView(binding.root)
            .setPositiveButton(android.R.string.ok, this)
            .setNegativeButton(android.R.string.cancel, this)
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        // TODO Test for valid URL
        super.onClick(dialog, which)
        when (which) {
            Dialog.BUTTON_POSITIVE -> {
                data = if (binding.edit.text.isNullOrBlank()) {
                    null
                } else {
                    Intent().apply {
                        putExtra(KEY_REMOTE_URL, binding.edit.text.toString())
                    }
                }
            }
        }
    }

    // endregion Inherited methods

    // region Implemented methods

    /**
     * Implementation of [DialogInterface.OnShowListener]. Sets the positive button's
     * OnClickListener when the dialog is shown so we can perform custom logic (i.e. check the
     * password entered by the user)
     */
    override fun onShow(dialog: DialogInterface?) {
        positiveBtn.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            positiveBtn -> {
                val remoteUrl = binding.edit.text.toString()
                if (remoteUrl.isEmpty()) {
                    resultCode = RESULT_POSITIVE
                    data = null
                    dialog.dismiss()
                } else {
                    // Test against Retrofit to make sure it passes baseUrl
                    try {
                        urlTester.baseUrl(remoteUrl)
                        resultCode = RESULT_POSITIVE
                        data = Intent().apply {
                            putExtra(KEY_REMOTE_URL, remoteUrl)
                        }
                        dialog.dismiss()
                    } catch (e: IllegalArgumentException) {
                        binding.layout.error = e.localizedMessage
                    }
                }
            }
        }
    }

    // endregion Implemented methods

    // region Companion object

    companion object {

        // region Properties

        @JvmStatic
        val KEY_REMOTE_URL =
            DeveloperOptionsRemoteUrlDialogFragment::class.makeKey("REMOTE_URL")

        @JvmStatic
        private val urlTester by lazy {
            Retrofit.Builder()
        }

        // endregion Properties

        // region Methods

        /**
         * Shows an [DeveloperOptionsRemoteUrlDialogFragment] for which you would like a result when
         * it dismisses (for whatever reason). If it implements
         * [AlertDialogFragment.AlertDialogFragmentListener], then [targetFragment] will
         * automatically be set as the result listener.
         */
        @JvmStatic
        fun showDialogFragmentForResult(
            targetFragment: Fragment,
            requestCode: Int,
            tag: String
        ) = DeveloperOptionsRemoteUrlDialogFragment().apply {
            setTargetFragment(targetFragment, requestCode)
            listenerIdentity = ListenerIdentity.TARGET_FRAGMENT
            show(targetFragment.requireFragmentManager(), tag)
        }

        /**
         * Shows an [DeveloperOptionsRemoteUrlDialogFragment] for which you would like a result when
         * it dismisses (for whatever reason). If it implements
         * [AlertDialogFragment.AlertDialogFragmentListener], then [activity] will automatically be
         * set as the result listener.
         */
        @JvmStatic
        fun showDialogFragmentForResult(
            activity: FragmentActivity,
            requestCode: Int,
            tag: String
        ) = DeveloperOptionsRemoteUrlDialogFragment().apply {
            this.requestCode = requestCode
            listenerIdentity = ListenerIdentity.ACTIVITY
            show(activity.supportFragmentManager, tag)
        }

        /**
         * Shows an [DeveloperOptionsRemoteUrlDialogFragment] for which you would like a result when
         * it dismisses (for whatever reason). This version of [showDialogFragmentForResult]
         * specifically supplies a [listener] with the caveat that, after configuration change, a
         * handle to this fragment must be obtained again (i.e. via
         * [FragmentManager.findFragmentByTag], etc.) and the listener must manually be re-set.
         */
        @Suppress("UNUSED")
        @JvmStatic
        fun showDialogFragmentForResult(
            fragmentManager: FragmentManager,
            requestCode: Int,
            tag: String,
            listener: AlertDialogFragmentListener? = null
        ) = DeveloperOptionsRemoteUrlDialogFragment().apply {
            this.requestCode = requestCode
            this.listener = listener
            show(fragmentManager, tag)
        }

        // endregion Methods

    }

    // endregion Companion object

}
