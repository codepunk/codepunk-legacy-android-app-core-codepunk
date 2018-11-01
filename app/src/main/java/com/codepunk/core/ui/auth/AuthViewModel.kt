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

package com.codepunk.core.ui.auth

import android.accounts.Account
import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.codepunk.core.BuildConfig
import com.codepunk.core.data.model.auth.AccessToken
import com.codepunk.core.data.model.http.ResponseMessage
import com.codepunk.core.data.remote.webservice.AuthWebservice
import com.codepunk.core.data.repository.UserRepository
import com.codepunk.core.lib.DataTask
import com.codepunk.core.lib.DataTask2
import com.codepunk.core.lib.DataUpdate
import com.codepunk.core.lib.HttpStatusEnum
import retrofit2.Response
import retrofit2.Retrofit
import java.io.IOException
import java.lang.IllegalStateException
import javax.inject.Inject

/**
 * A [ViewModel] for managing account-related data.
 */
class AuthViewModel @Inject constructor(

    private val retrofit: Retrofit,

    /**
     * The authorization webservice.
     */
    private val authWebservice: AuthWebservice,

    /**
     * The repository for accessing and manipulating user-related data.
     */
    private val userRepository: UserRepository,

    /**
     * The application shared preferences.
     */
    private val sharedPreferences: SharedPreferences,

    /**
     * The Android [AccountManager].
     */
    @Suppress("UNUSED")
    private val accountManager: AccountManager

) : ViewModel() {

    // region Properties

    /**
     * A [LiveData] holding the [AccessToken] information relating to the current authorization
     * attempt.
     */
    val authData = MediatorLiveData<DataUpdate<ResponseMessage, AccessToken>>()

    val authAccountData = MediatorLiveData<DataUpdate<Void, Pair<Account, AccessToken>>>()

    lateinit var username: String

    lateinit var email: String

    lateinit var password: String

    // endregion Properties

    // region Methods

    /**
     * Authenticates using username (or email) and password.
     */
    @SuppressLint("StaticFieldLeak")
    fun authenticate2(usernameOrEmail: String, password: String) {
        val task = object : DataTask2<Void, ResponseMessage, AccessToken>() {
            override fun doInBackground(vararg params: Void?):
                    DataUpdate<ResponseMessage, AccessToken> {
                return generateUpdate(authWebservice.getAuthToken(usernameOrEmail, password))
            }
        }
        authData.addSource(task.fetchOnExecutor()) { authData.value = it }
    }

    /**
     * Authenticates using username (or email) and password.
     */
    @SuppressLint("StaticFieldLeak")
    fun authenticate(usernameOrEmail: String, password: String) {
        val task = object : DataTask<Void, Void, Pair<Account, AccessToken>>() {
            override fun doInBackground(vararg params: Void?): Pair<Account, AccessToken>? {
                val response =
                    authWebservice.getAuthToken(usernameOrEmail, password).execute()
                return when {
                    response.isSuccessful -> {
                        val accessToken =
                            response.body() ?: throw IllegalStateException()
                        val account =
                            Account(usernameOrEmail, BuildConfig.AUTHENTICATOR_ACCOUNT_TYPE)
                        addOrUpdateAccount(accountManager, account, accessToken.refreshToken)
                        Pair(account, accessToken)
                    }
                    else -> fail(message = HttpStatusEnum.descriptionOf(response?.code()))
                }
            }
        }
        authAccountData.addSource(task.fetchOnExecutor()) { authAccountData.value = it }
    }

    /*
    /**
     * Authenticates using email and password.
     */
    @SuppressLint("StaticFieldLeak")
    fun authenticateOld(email: String, password: String) {
        val authenticateData =
            object : DataTask<Void, ResponseMessage, AccessToken>() {
                override fun doInBackground(vararg params: Void?): AccessToken? =
                    handleCall(authWebservice.getAuthToken(email, password))
            }.fetchOnExecutor()
        authData.addSource(authenticateData) { authData.value = it }
    }
    */

    @SuppressLint("StaticFieldLeak")
    fun register(username: String, email: String, password: String) {
        // TODO NEXT !!!!!!
    }

    /**
     * Creates a new account. Note that this account will not be activated as the user will still
     * need to respond to the activation email.
     */
    @SuppressLint("StaticFieldLeak")
    fun registerOld(username: String, email: String, password: String) {
        val registerData =
            object : DataTask<Void, ResponseMessage, AccessToken>() {
                override fun doInBackground(vararg params: Void?): AccessToken? {
                    try {
                        authWebservice.register(username, email, password, password).execute()
                            .apply {
                                val message = toMessage(this, retrofit)
                                when (message) {
                                    null -> publishProgress()
                                    else -> publishProgress(message)
                                }
                                if (!isSuccessful) {
                                    return fail(message = HttpStatusEnum.descriptionOf(code()))
                                }
                            }

                        return authWebservice.getAuthToken(email, password).execute().run {
                            when {
                                isSuccessful -> succeed(this)
                                else -> fail(this)
                            }
                        }
                    } catch (e: IOException) {
                        return failWithException(e = e)
                    }
                }

            }.fetchOnExecutor()
        authData.addSource(registerData) { authData.value = it }
    }

    // endregion methods

    // region Companion object

    companion object {

        // region Methods

        private fun toMessage(
            response: Response<ResponseMessage>,
            retrofit: Retrofit
        ): ResponseMessage? {
            return when {
                response.isSuccessful -> response.body()
                else -> response.errorBody()?.run {
                    retrofit.responseBodyConverter<ResponseMessage>(
                        ResponseMessage::class.java,
                        arrayOf()
                    ).convert(this)
                }
            }
        }

        private fun addOrUpdateAccount(
            accountManager: AccountManager,
            account: Account,
            password: String,
            userdata: Bundle? = null
        ) {
            if (!accountManager.addAccountExplicitly(account, password, userdata)) {
                accountManager.setPassword(account, password)
            }
        }

        // endregion Methods

    }

    // endregion Companion object

}
