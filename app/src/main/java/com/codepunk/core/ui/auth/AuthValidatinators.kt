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
import com.codepunk.punkubator.util.validatinator.MaxLengthValidatinator
import com.codepunk.punkubator.util.validatinator.PatternValidatinator
import com.codepunk.punkubator.util.validatinator.RequiredValidatinator
import com.codepunk.punkubator.util.validatinator.ValidatinatorSet
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthValidatinators @Inject constructor(

    @ApplicationContext
    val context: Context

) {

    val username = context.getString(R.string.authenticator_username)

    val requiredValidatinator = RequiredValidatinator.Builder()
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
                requiredValidatinator,
                wordCharValidatinator,
                maxLengthValidatinator)
            .style(ValidatinatorSet.Style.ALL)
            .validateAll(true)
            .build()

}
