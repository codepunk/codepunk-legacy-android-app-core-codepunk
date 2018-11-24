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

package com.codepunk.core.ui.auth

import android.content.Context
import android.util.Patterns
import com.codepunk.core.R
import com.codepunk.core.di.qualifier.ApplicationContext
import com.codepunk.punkubator.util.validatinator.*
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthValidatinators @Inject constructor(

    @ApplicationContext
    val context: Context

) {

    // region properties

    val confirmPassword = context.getString(R.string.validation_input_name_confirm_password)

    val confirmPasswordInputValidatinator: TextInputLayoutValidatinator =
        TextInputLayoutValidatinator.Builder(requiredValidatinator(context, confirmPassword))
            .context(context)
            .inputName(confirmPassword)
            .build()

    val email = context.getString(R.string.validation_input_name_email)

    val emailFormatValidatinator =
        PatternValidatinator.Builder(Patterns.EMAIL_ADDRESS)
            .context(context)
            .inputName(email)
            .invalidMessage { _, inputName ->
                context.getString(R.string.validation_invalid_pattern_email, inputName)
            }
            .build()

    val emailValidatinator: ValidatinatorSet<CharSequence?> =
        ValidatinatorSet.Builder<CharSequence?>()
            .context(context)
            .inputName(email)
            .add(
                requiredValidatinator(context, email),
                emailFormatValidatinator,
                maxLengthValidatinator(context, email, 255)
            )
            .processAll(true)
            .build()

    val emailInputValidatinator: TextInputLayoutValidatinator =
        TextInputLayoutValidatinator.Builder(emailValidatinator)
            .context(context)
            .inputName(email)
            .build()

    val givenName = context.getString(R.string.validation_input_name_given_name)

    val givenNameValidatinator: ValidatinatorSet<CharSequence?> =
        ValidatinatorSet.Builder<CharSequence?>()
            .context(context)
            .inputName(givenName)
            .add(
                requiredValidatinator(context, givenName),
                maxLengthValidatinator(context, givenName, 255)
            )
            .processAll(true)
            .build()

    val givenNameInputValidatinator: TextInputLayoutValidatinator =
        TextInputLayoutValidatinator.Builder(givenNameValidatinator)
            .context(context)
            .inputName(givenName)
            .build()

    val familyName = context.getString(R.string.validation_input_name_family_name)

    val familyNameValidatinator: ValidatinatorSet<CharSequence?> =
        ValidatinatorSet.Builder<CharSequence?>()
            .context(context)
            .inputName(familyName)
            .add(
                requiredValidatinator(context, familyName),
                maxLengthValidatinator(context, familyName, 255)
            )
            .processAll(true)
            .build()

    val familyNameInputValidatinator: TextInputLayoutValidatinator =
        TextInputLayoutValidatinator.Builder(familyNameValidatinator)
            .context(context)
            .inputName(familyName)
            .build()

    val password = context.getString(R.string.validation_input_name_password)

    val passwordMinLengthValidatinator: MinLengthValidatinator =
        MinLengthValidatinator.Builder(6)
            .context(context)
            .inputName(password)
            .build()

    val passwordValidatinator: ValidatinatorSet<CharSequence?> =
        ValidatinatorSet.Builder<CharSequence?>()
            .context(context)
            .inputName(password)
            .add(
                requiredValidatinator(context, password),
                passwordMinLengthValidatinator
            )
            .processAll(true)
            .build()

    val passwordInputValidatinator: TextInputLayoutValidatinator =
        TextInputLayoutValidatinator.Builder(passwordValidatinator)
            .context(context)
            .inputName(password)
            .build()

    val username = context.getString(R.string.validation_input_name_username)

    val usernameFormatValidatinator =
        PatternValidatinator.Builder(Pattern.compile("\\w+"))
            .context(context)
            .inputName(username)
            .invalidMessage { _, inputName ->
                context.getString(R.string.validation_invalid_pattern_word_character, inputName)
            }
            .build()

    val usernameValidatinator: ValidatinatorSet<CharSequence?> =
        ValidatinatorSet.Builder<CharSequence?>()
            .context(context)
            .inputName(username)
            .add(
                requiredValidatinator(context, username),
                usernameFormatValidatinator,
                maxLengthValidatinator(context, username, 64)
            )
            .processAll(true)
            .build()

    val usernameInputValidatinator: TextInputLayoutValidatinator =
        TextInputLayoutValidatinator.Builder(usernameValidatinator)
            .context(context)
            .inputName(username)
            .build()

    // endregion Properties

    // region Companion object

    companion object {

        // region Methods

        private fun maxLengthValidatinator(
            context: Context,
            inputName: CharSequence?,
            maxLength: Int
        ): MaxLengthValidatinator = MaxLengthValidatinator.Builder(maxLength)
            .context(context)
            .inputName(inputName)
            .build()

        private fun requiredValidatinator(
            context: Context,
            inputName: CharSequence?
        ): RequiredValidatinator = RequiredValidatinator.Builder()
            .context(context)
            .inputName(inputName)
            .build()

        // endregion Methods

    }

    // endregion Companion object

}
