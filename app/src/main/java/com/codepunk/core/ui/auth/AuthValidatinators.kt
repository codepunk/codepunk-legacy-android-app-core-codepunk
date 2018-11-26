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
import com.codepunk.core.R
import com.codepunk.core.di.qualifier.ApplicationContext
import com.codepunk.punkubator.util.validatinator.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthValidatinators @Inject constructor(

    @ApplicationContext
    val context: Context

) {

    // region properties

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

    val givenName = context.getString(R.string.validation_input_name_given_name)
    val givenNameValidatinator = ValidatinatorSet<CharSequence?>(
        context,
        givenName
    ).add(
        RequiredCharSequenceValidatinator(context, givenName),
        com.codepunk.punkubator.util.validatinator.MaxLengthValidatinator(
            context,
            givenName,
            255
        )
    )

    val familyName = context.getString(R.string.validation_input_name_family_name)
    val familyNameValidatinator = ValidatinatorSet<CharSequence?>(
        context,
        familyName
    ).add(
        RequiredCharSequenceValidatinator(context, familyName),
        com.codepunk.punkubator.util.validatinator.MaxLengthValidatinator(
            context,
            familyName,
            255
        )
    )

    val password = context.getString(R.string.validation_input_name_password)
    val passwordValidatinator = ValidatinatorSet<CharSequence?>(
        context,
        password
    ).add(
        RequiredCharSequenceValidatinator(context, password),
        MinLengthValidatinator(context, password, 6)
    )

    val confirmPassword = context.getString(R.string.validation_input_name_confirm_password)
    val confirmPasswordValidatinator = MatchValueValidatinator(
        context,
        confirmPassword
    )

    // endregion Properties

    // endregion Companion object

}
