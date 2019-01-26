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

@file:JvmName("RemoteConstants")

package com.codepunk.core.data.remote

// region Constants

// Header names

/**
 * The "Accept" API header name.
 */
const val HEADER_NAME_ACCEPT = "Accept"

/**
 * A custom header name that supplies an auth token. This will be converted to the "Authorization"
 * header via an interceptor.
 */
const val HEADER_NAME_TEMP_AUTH_TOKEN = "Temp-Auth-Token"

/**
 * The "RemoteAuthorization" API header name.
 */
const val HEADER_NAME_AUTHORIZATION = "Authorization"

/**
 * The "Content-Type" API header name.
 */
const val HEADER_NAME_CONTENT_TYPE = "Content-Type"

// Header values

/**
 * A placeholder for an auth token in endpoints that require authentication.
 */
const val HEADER_VALUE_AUTH_TOKEN_PLACEHOLDER = "\$authToken"

/**
 * A placeholder for an auth token in endpoints that require authentication.
 */
const val HEADER_VALUE_AUTHORIZATION = "Bearer $HEADER_VALUE_AUTH_TOKEN_PLACEHOLDER"

/**
 * The "application/json" header value.
 */
const val HEADER_VALUE_APPLICATION_JSON = "application/json"

// Header name/value pairs

/**
 * A name/value pair for accepting application/json responses.
 */
const val HEADER_ACCEPT_APPLICATION_JSON = "$HEADER_NAME_ACCEPT: $HEADER_VALUE_APPLICATION_JSON"

/**
 * A name/value pair for bearer authorization header.
 */
const val HEADER_AUTHORIZATION_BEARER = "$HEADER_NAME_AUTHORIZATION: $HEADER_VALUE_AUTHORIZATION"

/**
 * A name/value pair for specifying application/json Content-Type.
 */
@Suppress("UNUSED")
const val HEADER_CONTENT_TYPE_APPLICATION_JSON =
    "$HEADER_NAME_CONTENT_TYPE: $HEADER_VALUE_APPLICATION_JSON"

// endregion Constants
