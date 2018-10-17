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

package com.codepunk.core.data.repository

import android.annotation.SuppressLint
import android.os.AsyncTask
import androidx.lifecycle.LiveData
import com.codepunk.core.data.model.User
import com.codepunk.core.data.remote.interceptor.AuthorizationInterceptor
import com.codepunk.core.data.remote.webservice.UserWebservice
import com.codepunk.core.lib.DataTask
import com.codepunk.core.lib.DataUpdate
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("SpellCheckingInspection")
private const val TEMP_ACCESS_TOKEN: String =
    "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImp0aSI6IjcxMWE0Y2I2NDhhZmViNTkxYjJlMDI0OTQwYjYyY2U4ZDJkODVlOTRmZmMzYmZkYTU2YTY0YWYyOTlmYzM2OGYxZjA1ZDUwNmQyNGUzOTkxIn0.eyJhdWQiOiIyIiwianRpIjoiNzExYTRjYjY0OGFmZWI1OTFiMmUwMjQ5NDBiNjJjZThkMmQ4NWU5NGZmYzNiZmRhNTZhNjRhZjI5OWZjMzY4ZjFmMDVkNTA2ZDI0ZTM5OTEiLCJpYXQiOjE1MzkxODMzMTksIm5iZiI6MTUzOTE4MzMxOSwiZXhwIjoxNTcwNzE5MzE5LCJzdWIiOiIyIiwic2NvcGVzIjpbIioiXX0.z4sD2627vTYMQR0F_kmL5mjwHtE97VzA55P5bgHJM-TYgDrgVtmN8yuAfBUhoMHRSYz1H6Htrxx47-rnMjJ6Mvta6Y-or2gMJMZfO-wj2yVkeiM8P1HPk0XCRApPHVDBQ37uMhfzPO70DSs-yFwjPTn_Wc-1nd5wMul7zoj6yF6WIPs4TJP2S7Su_r58ti7ZOxcbPpPt2sQtGF5ePP8mDr647vOepwV9ah-KVoSOB206rGrxzNhDs6fyAE19YxLHpzrlItV7YpFVHR_iXJDRAKpFU_fudh-fy8PYU1qXjxn3sV1NKuUrldBGLGW8vmhx0yoP0pl_rEigvk6wQ1UMMUcfz9YndXKLEi3Z-erwmjvBImPdpe9m2TIPCVZKn1072R0TUljO4CLfapSDSWOhkTXmmoOHK4yaDBpf_OfOsZZ1qfOfv6owfn_CgwY9DYPNJi-P3XEFU9X1eaR2TMg9pae5q1bEJiPFB5ZnVD_6FqFL9cXIJcDDHtQLUlhdkbOrAQNWqhV2GBjkd1lBJM2sH0STRqMNNASMXSkBctQkDPGwdnlC_U6qkp11AvXhI-UZAl_vw_MylhvNjZ4EjWzGeaFW8koSpMMcFyxUqetdUuxgzxxhLV_ReWYPdrE8eS3GrzsMR31NwoJPPup1NU1dzwjQW8A_3c6Y5Y46wjQLEyE"

/**
 * A repository for accessing and manipulating user-related data.
 */
@Singleton
@SuppressLint("StaticFieldLeak")
class UserRepository @Inject constructor(

    /**
     * the singleton [AuthorizationInterceptor] instance. This is needed (perhaps
     * temporarily) so we can set the access token for API calls that require it.
     */
    authorizationInterceptor: AuthorizationInterceptor,

    /**
     * An instance of [UserWebservice] for making user-related API calls.
     */
    private val userWebservice: UserWebservice

) {

    // region Constructors

    /**
     * Sets the access token for api calls that require it.
     */
    init {
        // TODO TEMP How to get token into interceptor and/or retrofit calls?
        authorizationInterceptor.accessToken = TEMP_ACCESS_TOKEN
    }

    // endregion Constructors

    // region Methods

    /**
     * Attempts to authenticate using the current account if one exists.
     */
    @SuppressLint("StaticFieldLeak")
    fun authenticate(): LiveData<DataUpdate<User, User>> {
        return object : DataTask<Void, User, User>() {

            // TODO I still want to include getting locally-stored user from db here
            override fun doInBackground(vararg params: Void?): User? =
                handleCall(userWebservice.getUser())

        }.fetchOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    // endregion Methods

}
