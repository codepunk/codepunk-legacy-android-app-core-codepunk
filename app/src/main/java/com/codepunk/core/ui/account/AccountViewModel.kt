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

package com.codepunk.core.ui.account

import android.content.SharedPreferences
import androidx.lifecycle.*
import com.codepunk.core.data.TaskStatus
import com.codepunk.core.data.model.User
import com.codepunk.core.data.repository.UserRepository
import javax.inject.Inject

/**
 * A [ViewModel] for managing account-related data.
 */
class AccountViewModel @Inject constructor(

    /**
     * The repository for accessing and manipulating user-related data.
     */
    private val userRepository: UserRepository,

    /**
     * The application shared preferences.
     */
    @Suppress("UNUSED")
    private val sharedPreferences: SharedPreferences

) : ViewModel() {

    // region Properties

    /**
     * A [LiveData] that will serve as the trigger to attempt to authenticate the user.
     */
    private val attemptAuthenticate = MutableLiveData<Boolean>().apply {
        value = false
    }

    /**
     * The [LiveData] that will hold the result of user authentication.
     */
    val userOperation: LiveData<TaskStatus<User, User?>> = Transformations
        .switchMap(attemptAuthenticate) { attempt ->
            when (attempt) {
                true -> userRepository.authenticate()
                else -> null
            }
        }

    /**
     * TODO: Temporary. Maybe make this a mediator that starts off as null but I add a source
     * as needed?
     */
    @Suppress("UNUSED")
    val userOperation2 = MediatorLiveData<TaskStatus<User, User>>()

    // endregion Properties

    // region Methods

    /**
     * Tries to authenticate the user.
     */
    fun authenticate() {
        attemptAuthenticate.value = true
    }

    // endregion methods

}
