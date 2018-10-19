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
import com.codepunk.core.data.model.http.ResponseMessage
import com.codepunk.core.data.remote.webservice.AuthWebservice
import com.codepunk.core.data.repository.UserRepository
import com.codepunk.core.lib.DataTask
import com.codepunk.core.lib.DataUpdate
import com.codepunk.core.lib.HttpStatus
import com.squareup.moshi.Moshi
import java.io.IOException
import java.util.concurrent.CancellationException
import javax.inject.Inject

/**
 * A [ViewModel] for managing account-related data.
 */
class AccountViewModel @Inject constructor(

    private val moshi: Moshi,

    /**
     * TODO
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

    val authData = MediatorLiveData<DataUpdate<ResponseMessage, ResponseMessage>>()

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

    @SuppressLint("StaticFieldLeak")
    fun register(
        username: String,
        email: String,
        password: String,
        passwordConfirmation: String
    ) {
        val registerData =
            object : DataTask<Void, ResponseMessage, ResponseMessage>() {
                /*
                override fun doInBackground(vararg params: Void?): ResponseMessage? = handleCall(
                    authWebservice.register(
                        username,
                        email,
                        password,
                        passwordConfirmation
                    )
                )
                */

                override fun doInBackground(vararg params: Void?): ResponseMessage? {
                    val call =
                        authWebservice.register(username, email, password, passwordConfirmation)
                    return try {
                        call.execute().run {
                            when {
                                isSuccessful -> succeed(body(), this)
                                else -> {
                                    val description =
                                        HttpStatus.lookup(code())?.description
                                            ?: "${code()} Unknown"

                                    // TODO **** Can I somehow pass this to DataTask?
                                    val result = errorBody()?.string()?.let {
                                        moshi.adapter(ResponseMessage::class.java).fromJson(it)
                                    }

                                    fail(
                                        result,
                                        this,
                                        CancellationException(description),
                                        true
                                    )
                                }
                            }
                        }
                    } catch (e: IOException) {
                        fail(null, null, e, true)
                    }
                }

            }.fetchOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        authData.addSource(registerData) {
            authData.value = it
        }
    }

    // endregion methods

}
