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
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.codepunk.core.BuildConfig
import com.codepunk.core.data.model.auth.AccessToken
import com.codepunk.core.data.model.http.ResponseMessage
import com.codepunk.core.data.remote.webservice.AuthWebservice
import com.codepunk.core.data.repository.UserRepository
import com.codepunk.core.lib.*
import retrofit2.Response
import retrofit2.Retrofit
import java.io.IOException
import java.lang.IllegalStateException
import java.util.concurrent.TimeUnit
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

    lateinit var username: String

    lateinit var email: String

    lateinit var password: String

    // endregion Properties

    // region Methods

    /**
     * Authenticates using username (or email) and password.
     */
    @SuppressLint("StaticFieldLeak")
    fun authenticate(usernameOrEmail: String, password: String) {
        val task = object : DataTask<Void, ResponseMessage, AccessToken>() {
            override fun doInBackground(vararg params: Void?):
                    DataUpdate<ResponseMessage, AccessToken> =
                authWebservice.getAuthToken(usernameOrEmail, password).toDataUpdate()
        }
        authData.addSource(task.fetchOnExecutor()) { authData.value = it }
    }

    /*
    /**
     * Authenticates using username (or email) and password.
     */
    @SuppressLint("StaticFieldLeak")
    fun authenticateOld(usernameOrEmail: String, password: String) {
        val task = object : DataTaskOld<Void, Void, Pair<Account, AccessToken>>() {
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
    */

    /**
     * Registers a new user. Note that this user will not be activated as the user will
     * still need to respond to the activation email.
     */
    @SuppressLint("StaticFieldLeak")
    fun register(username: String, email: String, password: String) {
        val authTokenTask =
            object : DataTask<Void, ResponseMessage, AccessToken>() {
                override fun doInBackground(vararg params: Void?):
                        DataUpdate<ResponseMessage, AccessToken> {
                    val update: DataUpdate<Void, ResponseMessage> =
                        authWebservice.register(username, email, password, password).toDataUpdate()
                    return when (update) {
                        is SuccessUpdate -> {
                            publishProgress(update.result)
                            authWebservice.getAuthToken(email, password).toDataUpdate()
                        }
                        is FailureUpdate -> FailureUpdate(e = update.e)
                        else -> FailureUpdate()
                    }
                }
            }
        authData.addSource(authTokenTask.fetchOnExecutor()) { authData.value = it }
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

    // region Nested/inner classes


    // endregion Nested/inner classes

}
