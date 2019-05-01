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

package com.codepunk.core.presentation.auth

import android.content.Context
import com.codepunk.core.R
import com.codepunk.core.databinding.FragmentRegisterBinding
import com.codepunk.core.di.qualifier.ApplicationContext
import com.codepunk.punkubator.util.validatinator.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RegisterValidatinators @Inject constructor(

    @ApplicationContext
    val context: Context

) {

    // region Properties

    val username = context.getString(R.string.validation_input_name_username)
    val usernameValidatinator = ValidatinatorSet<CharSequence?>(
        context,
        username
    ).add(
        RequiredCharSequenceValidatinator(context, username),
        WordCharacterValidatinator(context, username),
        com.codepunk.punkubator.util.validatinator.MaxLengthValidatinator(
            context,
            username,
            64
        )
    )
    val usernameInputValidatinator = TextInputLayoutValidatinator(
        context,
        username,
        usernameValidatinator
    )

    val email = context.getString(R.string.validation_input_name_email)
    val emailValidatinator = ValidatinatorSet<CharSequence?>(
        context,
        email
    ).add(
        RequiredCharSequenceValidatinator(context, email),
        EmailValidatinator(context, email),
        com.codepunk.punkubator.util.validatinator.MaxLengthValidatinator(
            context,
            email,
            255
        )
    )
    val emailInputValidatinator = TextInputLayoutValidatinator(
        context,
        username,
        emailValidatinator
    )

    val password = context.getString(R.string.validation_input_name_password)
    val passwordValidatinator = ValidatinatorSet<CharSequence?>(
        context,
        password
    ).add(
        RequiredCharSequenceValidatinator(context, password),
        MinLengthValidatinator(context, password, 6)
    )
    val passwordInputValidatinator = TextInputLayoutValidatinator(
        context,
        username,
        passwordValidatinator
    )

    val confirmPassword = context.getString(R.string.validation_input_name_confirm_password)
    val confirmPasswordValidatinator = RequiredCharSequenceValidatinator(context, confirmPassword)
    val confirmPasswordInputValidatinator = TextInputLayoutMatchValidatinator(
        context,
        confirmPassword,
        confirmPasswordValidatinator
    )

    val registerValidatinator =
        object : Validatinator<FragmentRegisterBinding>(context) {

            override fun isValid(input: FragmentRegisterBinding, options: Options): Boolean {
                input.usernameLayout.error = null
                input.emailLayout.error = null
                input.passwordLayout.error = null
                input.confirmPasswordLayout.error = null
                return usernameInputValidatinator.validate(
                    input.usernameLayout, options.clear()
                ) && emailInputValidatinator.validate(
                    input.emailLayout,
                    options.clear()
                ) && passwordInputValidatinator.validate(
                    input.passwordLayout,
                    options.clear()
                ) && confirmPasswordInputValidatinator.validate(
                    input.confirmPasswordLayout,
                    input.passwordLayout.editText?.text,
                    options.clear()
                )
            }

        }

    // endregion Properties

}
