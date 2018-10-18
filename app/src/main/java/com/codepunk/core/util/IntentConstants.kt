/*
 * Copyright (C) 2018 Codepunk, LLC
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

@file:JvmName("IntentConstants")
package com.codepunk.core.util

import com.codepunk.core.BuildConfig.APPLICATION_ID

private const val EXTRA = "extra"

const val EXTRA_AUTH_TOKEN_TYPE = "$APPLICATION_ID.$EXTRA.AUTH_TOKEN_TYPE"

const val EXTRA_AUTHENTICATOR_INITIAL_ACTION = "$APPLICATION_ID.$EXTRA.AUTHENTICATOR_INITIAL_ACTION"

const val EXTRA_CONFIRM_CREDENTIALS = "$APPLICATION_ID.$EXTRA.CONFIRM_CREDENTIALS"

const val EXTRA_PASSWORD = "$APPLICATION_ID.$EXTRA.PASSWORD"

const val EXTRA_USERNAME = "$APPLICATION_ID.$EXTRA.USERNAME"

