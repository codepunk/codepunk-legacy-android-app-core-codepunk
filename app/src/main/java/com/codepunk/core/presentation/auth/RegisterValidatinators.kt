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

/**
 * A set of [Validatinator]s to validate the register form found in [RegisterFragment].
 */
@Singleton
class RegisterValidatinators @Inject constructor(

    /**
     * A [Context] to associate with this [Validatinator].
     */
    @ApplicationContext
    val context: Context

) {

    // region Properties

    /**
     * A user-friendly reference for the username value.
     */
    val username: String = context.getString(R.string.validation_input_name_username)

    /**
     * The [Validatinator] used to validate the username value.
     */
    private val usernameValidatinator = ValidatinatorSet<CharSequence?>(
        context,
        username
    ).add(
        RequiredCharSequenceValidatinator(context, username),
        WordCharacterValidatinator(context, username),
        MaxLengthValidatinator(
            context,
            username,
            64
        )
    )

    /**
     * The [TextInputLayoutValidatinator] used to validate the username field.
     */
    val usernameInputValidatinator = TextInputLayoutValidatinator(
        context,
        username,
        usernameValidatinator
    )

    /**
     * A user-friendly reference for the email value.
     */
    val email: String = context.getString(R.string.validation_input_name_email)

    /**
     * The [Validatinator] used to validate the email value.
     */
    private val emailValidatinator = ValidatinatorSet<CharSequence?>(
        context,
        email
    ).add(
        RequiredCharSequenceValidatinator(context, email),
        EmailValidatinator(context, email),
        MaxLengthValidatinator(
            context,
            email,
            255
        )
    )

    /**
     * The [TextInputLayoutValidatinator] used to validate the email field.
     */
    val emailInputValidatinator = TextInputLayoutValidatinator(
        context,
        username,
        emailValidatinator
    )

    /**
     * A user-friendly reference for the password value.
     */
    val password: String = context.getString(R.string.validation_input_name_password)

    /**
     * The [Validatinator] used to validate the password value.
     */
    private val passwordValidatinator = ValidatinatorSet<CharSequence?>(
        context,
        password
    ).add(
        RequiredCharSequenceValidatinator(context, password),
        MinLengthValidatinator(context, password, 6)
    )

    /**
     * The [TextInputLayoutValidatinator] used to validate the password field.
     */
    val passwordInputValidatinator = TextInputLayoutValidatinator(
        context,
        username,
        passwordValidatinator
    )

    /**
     * A user-friendly reference for the "confirm password" value.
     */
    private val confirmPassword: String =
        context.getString(R.string.validation_input_name_confirm_password)

    /**
     * The [Validatinator] used to validate the "confirm password" value.
     */
    private val confirmPasswordValidatinator =
        RequiredCharSequenceValidatinator(context, confirmPassword)

    /**
     * The [TextInputLayoutValidatinator] used to validate the "confirm password" field.
     */
    val confirmPasswordInputValidatinator = TextInputLayoutMatchValidatinator(
        context,
        confirmPassword,
        confirmPasswordValidatinator
    )

    /**
     * A [Validatinator] that validates an entire [FragmentRegisterBinding].
     */
    @Suppress("UNUSED")
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
