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

package com.codepunk.core.data.remote.webservice

import com.codepunk.core.BuildConfig
import com.codepunk.core.data.model.auth.Authorization
import com.codepunk.core.data.model.auth.GrantType
import com.codepunk.core.data.model.http.ResponseMessage
import retrofit2.Call

/**
 * TODO: Replace all CODEPUNK_LOCAL_CLIENT_ID/CODEPUNK_LOCAL_CLIENT_SECRET with current somehow
 */

/**
 * Implementation of [AuthWebservice] that allows for default arguments by wrapping another
 * instance ([base]) and passing default arguments to its methods where appropriate.
 */
class AuthWebserviceWrapper(private val base: AuthWebservice) : AuthWebservice {

    // region Inherited methods

    override fun authorize(
        grantType: GrantType,
        clientId: String,
        clientSecret: String,
        username: String,
        password: String,
        scope: String
    ): Call<Authorization> = base.authorize(
        grantType,
        clientId,
        clientSecret,
        username,
        password,
        scope
    )

    /**
     * Gets an authorization token using default values.
     */
    override fun authorize(
        username: String,
        password: String,
        scope: String
    ): Call<Authorization> {
        return base.authorize(
            GrantType.PASSWORD,
            BuildConfig.CODEPUNK_LOCAL_CLIENT_ID,
            BuildConfig.CODEPUNK_LOCAL_CLIENT_SECRET,
            username,
            password,
            scope
        )
    }


    /**
     * Gets an authorization token using default values.
     */
    override fun authorize(username: String, password: String): Call<Authorization> {
        return base.authorize(
            GrantType.PASSWORD,
            BuildConfig.CODEPUNK_LOCAL_CLIENT_ID,
            BuildConfig.CODEPUNK_LOCAL_CLIENT_SECRET,
            username,
            password,
            "*"
        )
    }

    override fun refreshToken(
        grantType: GrantType,
        clientId: String,
        clientSecret: String,
        refreshToken: String
    ): Call<Authorization> = base.refreshToken(grantType, clientId, clientSecret, refreshToken)

    /**
     * Gets an authorization token from an existing [refreshToken] by passing default arguments to
     * the base implementation.
     */
    override fun refreshToken(refreshToken: String): Call<Authorization> = base.refreshToken(
        GrantType.REFRESH_TOKEN,
        BuildConfig.CODEPUNK_LOCAL_CLIENT_ID,
        BuildConfig.CODEPUNK_LOCAL_CLIENT_SECRET,
        refreshToken
    )

    override fun register(
        name: String,
        email: String,
        givenName: String,
        familyName: String,
        password: String,
        passwordConfirmation: String
    ): Call<ResponseMessage> =
        base.register(name, email, givenName, familyName, password, passwordConfirmation)

    // endregion Inherited methods

}
