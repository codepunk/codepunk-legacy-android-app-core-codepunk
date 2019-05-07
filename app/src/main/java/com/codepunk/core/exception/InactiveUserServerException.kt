/*
 * Copyright (C) 2019 Codepunk, LLC
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

package com.codepunk.core.exception

import com.codepunk.core.data.remote.entity.RemoteOAuthServerExceptionResponse

class InactiveUserServerException : OAuthServerException {

    // region Constructors

    constructor(errorDescription: String?, hint: String?) : super(errorDescription, hint)

    constructor(message: String?, errorDescription: String?, hint: String?) : super(
        message,
        errorDescription,
        hint
    )

    constructor(
        message: String?,
        cause: Throwable?,
        errorDescription: String?,
        hint: String?
    ) : super(message, cause, errorDescription, hint)

    constructor(cause: Throwable?, errorDescription: String?, hint: String?) : super(
        cause,
        errorDescription,
        hint
    )

    constructor(
        message: String?,
        cause: Throwable?,
        enableSuppression: Boolean,
        writableStackTrace: Boolean,
        errorDescription: String?,
        hint: String?
    ) : super(message, cause, enableSuppression, writableStackTrace, errorDescription, hint)

    constructor(response: RemoteOAuthServerExceptionResponse) : super(response)

    // endregion Constructors

}
