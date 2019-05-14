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
import com.codepunk.core.databinding.FragmentLogInBinding
import com.codepunk.core.di.qualifier.ApplicationContext
import com.codepunk.punkubator.util.validatinator.RequiredCharSequenceValidatinator
import com.codepunk.punkubator.util.validatinator.TextInputLayoutValidatinator
import com.codepunk.punkubator.util.validatinator.Validatinator
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A set of [Validatinator]s to validate the login form found in [LogInFragment].
 */
@Singleton
class LogInValidatinators @Inject constructor(

    /**
     * A [Context] to associate with this [Validatinator].
     */
    @ApplicationContext
    val context: Context

) {

    // region Properties

    /**
     * A user-friendly reference for the "username or email" value.
     */
    private val usernameOrEmail: String =
        context.getString(R.string.validation_input_name_username_or_email)

    /**
     * The [Validatinator] used to validate the "username or email" value.
     */
    private val usernameOrEmailValidatinator =
        RequiredCharSequenceValidatinator(context, usernameOrEmail)

    /**
     * The [TextInputLayoutValidatinator] used to validate the "username or email" field.
     */
    val usernameOrEmailInputValidatinator = TextInputLayoutValidatinator(
        context,
        usernameOrEmail,
        usernameOrEmailValidatinator
    )

    /**
     * A user-friendly reference for the password value.
     */
    private val password: String = context.getString(R.string.validation_input_name_password)

    /**
     * The [Validatinator] used to validate the password value.
     */
    private val passwordValidatinator = RequiredCharSequenceValidatinator(context, password)

    /**
     * The [TextInputLayoutValidatinator] used to validate the password field.
     */
    val passwordInputValidatinator = TextInputLayoutValidatinator(
        context,
        password,
        passwordValidatinator
    )

    /**
     * A [Validatinator] that validates an entire [FragmentLogInBinding].
     */
    @Suppress("UNUSED")
    val logInValidatinator =
        object : Validatinator<FragmentLogInBinding>(context) {

            override fun isValid(input: FragmentLogInBinding, options: Options): Boolean {
                input.usernameOrEmailLayout.error = null
                input.passwordLayout.error = null
                return usernameOrEmailInputValidatinator.validate(
                    input.usernameOrEmailLayout, options.clear()
                ) && passwordInputValidatinator.validate(
                    input.passwordLayout,
                    options.clear()
                )
            }

        }

    // endregion Properties

}
