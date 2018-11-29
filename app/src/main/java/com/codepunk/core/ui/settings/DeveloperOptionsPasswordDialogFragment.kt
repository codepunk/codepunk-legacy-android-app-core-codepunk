/*
 * Copyright (C) 2018 Codepunk, LLC
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

package com.codepunk.core.ui.settings

import android.animation.Animator
import android.animation.AnimatorInflater
import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.databinding.DataBindingUtil
import com.codepunk.core.BuildConfig.DEVELOPER_OPTIONS_PASSWORD_HASH
import com.codepunk.core.BuildConfig.EXTRA_DEVELOPER_OPTIONS_PASSWORD_HASH
import com.codepunk.core.R
import com.codepunk.core.databinding.FragmentDialogDeveloperOptionsPasswordBinding
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
    AppCompatDialogFragment(),
    DialogInterface.OnShowListener,
    View.OnClickListener {

    // region Properties

    /**
     * The binding for this fragment.
     */
    private lateinit var binding: FragmentDialogDeveloperOptionsPasswordBinding

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
     * Builds the [Dialog] in which the user will enter the developer password.
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.fragment_dialog_developer_options_password,
            null,
            false
        )
        return AlertDialog.Builder(requireContext())
            .setTitle(R.string.settings_developer_options_password_dialog_title)
            .setMessage(R.string.settings_developer_options_password_dialog_message)
            .setView(binding.root)
            .setPositiveButton(android.R.string.ok, null)
            .setNegativeButton(android.R.string.cancel, null)
            .create().apply {
                setOnShowListener(this@DeveloperOptionsPasswordDialogFragment)
                window?.run { shakeAnimator.setTarget(decorView) }
            }
    }

    // endregion Inherited methods

    // region Implemented methods

    /**
     * Sets the positive button's OnClickListener when the dialog is shown so we can perform
     * custom logic (i.e. check the password entered by the user)
     */
    // DialogInterface.OnShowListener
    override fun onShow(dialog: DialogInterface?) {
        positiveBtn.setOnClickListener(this)
    }

    /**
     * Tests the password entered by the user against the stored hashed password, and
     * shake the dialog if the user enters the incorrect password.
     */
    // View.OnClickListener
    override fun onClick(view: View?) {
        when (view) {
            positiveBtn -> {
                val password = binding.edit.text.toString()
                val hex = String(Hex.encodeHex(digestUtils.digest(password)))
                if (DEVELOPER_OPTIONS_PASSWORD_HASH.equals(hex, true)) {
                    dialog.dismiss()
                    targetFragment?.onActivityResult(
                        targetRequestCode,
                        Activity.RESULT_OK,
                        Intent().apply {
                            putExtra(
                                EXTRA_DEVELOPER_OPTIONS_PASSWORD_HASH,
                                DEVELOPER_OPTIONS_PASSWORD_HASH
                            )
                        })
                } else {
                    binding.layout.error =
                            getString(R.string.settings_developer_options_incorrect_password)
                    shakeAnimator.start()
                }
            }
        }
    }

    // endregion Implemented methods

}
