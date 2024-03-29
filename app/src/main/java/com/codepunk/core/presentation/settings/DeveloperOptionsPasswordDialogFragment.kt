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

import android.animation.Animator
import android.animation.AnimatorInflater
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
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
import com.codepunk.core.databinding.DialogDeveloperOptionsPasswordBinding
import com.codepunk.doofenschmirtz.app.AlertDialogFragment
import com.codepunk.doofenschmirtz.view.animation.ShakeInterpolator
import org.apache.commons.codec.binary.Hex
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.codec.digest.MessageDigestAlgorithms

/**
 * Dialog fragment used to get the developer password from the user. The developer password is not
 * stored anywhere in the app; rather, the SHA-256 hashed password is stored and this class hashes
 * the user's input in order to compare it with the stored hash.
 */
class DeveloperOptionsPasswordDialogFragment :
    AlertDialogFragment(),
    DialogInterface.OnShowListener,
    View.OnClickListener {

    // region Properties

    /**
     * The binding for this fragment.
     */
    private lateinit var binding: DialogDeveloperOptionsPasswordBinding

    /**
     * The (optional) [Button] that represents a positive response by the user.
     */
    private val positiveBtn by lazy {
        (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
    }

    /**
     * The [DigestUtils] that will be used to create a hash of the user's input.
     */
    private val digestUtils = DigestUtils(MessageDigestAlgorithms.SHA_256)

    /**
     * An [Animator] that will run if the user enters an incorrect password.
     */
    private val shakeAnimator by lazy {
        AnimatorInflater.loadAnimator(requireContext(), R.animator.shake).apply {
            interpolator = ShakeInterpolator()
        }
    }

    // endregion Properties

    // region Inherited methods

    /**
     * Creates the developer options password dialog.
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).also { dialog ->
            dialog.setOnShowListener(this)
            dialog.window?.run { shakeAnimator.setTarget(decorView) }
        }
    }

    /**
     * Returns an [AlertDialog.Builder] for building the developer options password dialog.
     */
    override fun newBuilder(savedInstanceState: Bundle?): AlertDialog.Builder {
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
            R.layout.dialog_developer_options_password,
            null,
            false
        )
        return AlertDialog.Builder(requireContext())
            .setTitle(R.string.settings_developer_options_password_dialog_title)
            .setMessage(R.string.settings_developer_options_password_dialog_message)
            .setView(binding.root)
            .setPositiveButton(android.R.string.ok, this)
            .setNegativeButton(android.R.string.cancel, this)
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

    /**
     * Implementation of [View.OnClickListener]. Tests the password entered by the user against the
     * stored hashed password, and shake the dialog if the user enters the incorrect password.
     */
    override fun onClick(view: View?) {
        when (view) {
            positiveBtn -> {
                val password = binding.edit.text.toString()
                val hex = String(Hex.encodeHex(digestUtils.digest(password)))
                if (BuildConfig.DEVELOPER_OPTIONS_PASSWORD_HASH.equals(hex, true)) {
                    resultCode = RESULT_POSITIVE
                    data = Intent().apply {
                        putExtra(
                            BuildConfig.EXTRA_DEVELOPER_OPTIONS_PASSWORD_HASH,
                            BuildConfig.DEVELOPER_OPTIONS_PASSWORD_HASH
                        )
                    }
                    dialog.dismiss()
                } else {
                    binding.layout.error =
                        getString(R.string.settings_developer_options_incorrect_password)
                    shakeAnimator.start()
                }
            }
        }
    }

    // endregion Implemented methods

    // region Companion object

    companion object {

        // region Methods

        /**
         * Shows an [DeveloperOptionsPasswordDialogFragment] for which you would like a result when
         * it dismisses (for whatever reason). If it implements
         * [AlertDialogFragment.AlertDialogFragmentListener], then [targetFragment] will
         * automatically be set as the result listener.
         */
        @JvmStatic
        fun showDialogFragmentForResult(
            targetFragment: Fragment,
            requestCode: Int,
            tag: String
        ) = DeveloperOptionsPasswordDialogFragment().apply {
            setTargetFragment(targetFragment, requestCode)
            listenerIdentity = ListenerIdentity.TARGET_FRAGMENT
            show(targetFragment.requireFragmentManager(), tag)
        }

        /**
         * Shows an [DeveloperOptionsPasswordDialogFragment] for which you would like a result when
         * it dismisses (for whatever reason). If it implements
         * [AlertDialogFragment.AlertDialogFragmentListener], then [activity] will automatically be
         * set as the result listener.
         */
        @JvmStatic
        fun showDialogFragmentForResult(
            activity: FragmentActivity,
            requestCode: Int,
            tag: String
        ) = DeveloperOptionsPasswordDialogFragment().apply {
            this.requestCode = requestCode
            listenerIdentity = ListenerIdentity.ACTIVITY
            show(activity.supportFragmentManager, tag)
        }

        /**
         * Shows an [DeveloperOptionsPasswordDialogFragment] for which you would like a result when
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
        ) = DeveloperOptionsPasswordDialogFragment().apply {
            this.requestCode = requestCode
            this.listener = listener
            show(fragmentManager, tag)
        }

        // endregion Methods

    }

    // endregion Companion object

}
