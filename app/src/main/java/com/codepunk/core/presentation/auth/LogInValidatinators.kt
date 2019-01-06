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

@Singleton
class LogInValidatinators @Inject constructor(

    @ApplicationContext
    val context: Context

) {

    // region Properties

    val usernameOrEmail = context.getString(R.string.validation_input_name_username_or_email)
    val usernameOrEmailValidatinator = RequiredCharSequenceValidatinator(context, usernameOrEmail)
    val usernameOrEmailInputValidatinator = TextInputLayoutValidatinator(
        context,
        usernameOrEmail,
        usernameOrEmailValidatinator
    )

    val password = context.getString(R.string.validation_input_name_password)
    val passwordValidatinator = RequiredCharSequenceValidatinator(context, password)
    val passwordInputValidatinator = TextInputLayoutValidatinator(
        context,
        password,
        passwordValidatinator
    )

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
