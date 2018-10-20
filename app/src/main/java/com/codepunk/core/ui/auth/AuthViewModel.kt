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

import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.AsyncTask
import androidx.lifecycle.*
import com.codepunk.core.BuildConfig
import com.codepunk.core.data.model.User
import com.codepunk.core.data.model.auth.AccessToken
import com.codepunk.core.data.model.http.ResponseMessage
import com.codepunk.core.data.remote.webservice.AuthWebservice
import com.codepunk.core.data.repository.UserRepository
import com.codepunk.core.lib.DataTask
import com.codepunk.core.lib.DataUpdate
import com.codepunk.core.lib.HttpStatus
import retrofit2.Response
import retrofit2.Retrofit
import java.io.IOException
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
     * A [LiveData] that will serve as the trigger to attempt to authenticate the user.
     */
    private val attemptAuthenticate = MutableLiveData<Boolean>()

    /**
     * A [LiveData] holding the [AccessToken] information relating to the current authorization
     * attempt.
     */
    val authData = MediatorLiveData<DataUpdate<ResponseMessage, AccessToken>>()

    /**
     * The [LiveData] that will hold the result of user authentication.
     */
    val userData: LiveData<DataUpdate<User, User>> = Transformations
        .switchMap(attemptAuthenticate) {
            when (it) {
                true -> userRepository.authenticate()
                else -> null
            }
        }

    lateinit var username: String

    lateinit var password: String

    // endregion Properties

    // region Methods

    /**
     * Tries to authenticate the user.
     */
    fun authenticate() {
        // Get a list of accounts from AccountManager
//        accountManager.getAccountsByType()

        // First, see if we saved an account name
        sharedPreferences.getString(BuildConfig.PREF_KEY_CURRENT_ACCOUNT, null)?.run {

        } ?: run {
            ""
        }

        attemptAuthenticate.value = true
    }

    /**
     * Creates a new account. Note that this account will not be activated as the user will still
     * need to respond to the activation email.
     */
    @SuppressLint("StaticFieldLeak")
    fun register(username: String, email: String, password: String) {
        val registerData =
            object : DataTask<Void, ResponseMessage, AccessToken>() {
                override fun doInBackground(vararg params: Void?): AccessToken? {
                    try {
                        authWebservice.register(username, email, password, password).execute()
                            .apply {
                                val message = toMessage(this)
                                when (message) {
                                    null -> publishProgress()
                                    else -> publishProgress(message)
                                }
                                if (!isSuccessful) {
                                    return fail(message = HttpStatus.descriptionOf(code()))
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

            }.fetchOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        authData.addSource(registerData) {
            authData.value = it
        }
    }

    private fun toMessage(response: Response<ResponseMessage>): ResponseMessage? {
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

    // endregion methods

}
