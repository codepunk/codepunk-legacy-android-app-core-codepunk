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
import com.codepunk.core.databinding.FragmentCreateAccountBinding
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

    val username = context.getString(R.string.authenticator_username)
    val email = context.getString(R.string.authenticator_email)

    val requiredUsernameValidatinator = RequiredValidatinator.Builder()
        .context(context)
        .inputName(username)
        .build()

    val wordCharValidatinator =
        PatternValidatinator.Builder(Pattern.compile("\\w+"))
            .context(context)
            .inputName(username)
            .invalidMessage(
                context.getString(
                    R.string.validation_word_character_pattern,
                    username
                )
            )
            .build()

    val maxLengthValidatinator =
        MaxLengthValidatinator.Builder(64)
            .context(context)
            .inputName(username)
            .build()

    val usernameValidatinator =
        ValidatinatorSet.Builder<CharSequence?>()
            .context(context)
            .inputName(username)
            .add(
                requiredUsernameValidatinator,
                wordCharValidatinator,
                maxLengthValidatinator
            )
            .build()

    val requiredEmailValidatinator = RequiredValidatinator.Builder()
        .context(context)
        .inputName(email)
        .build()

    val emailPatternValidatinator =
        PatternValidatinator.Builder(Patterns.EMAIL_ADDRESS)
            .context(context)
            .inputName(email)
            .invalidMessage(
                context.getString(
                    R.string.validation_email,
                    email
                )
            )
            .build()

    val emailValidatinator =
        ValidatinatorSet.Builder<CharSequence?>()
            .context(context)
            .inputName(username)
            .add(
                requiredEmailValidatinator,
                emailPatternValidatinator
            )
            .build()

    val usernameTextInputLayoutValidatinator =
        TextInputLayoutValidatinator.Builder(usernameValidatinator).build()

    val emailTextInputLayoutValidatinator =
        TextInputLayoutValidatinator.Builder(emailValidatinator).build()

}
