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

package com.codepunk.core.di.module

import android.accounts.AccountManager
import android.content.SharedPreferences
import com.codepunk.core.data.local.dao.UserDao
import com.codepunk.core.data.remote.webservice.UserWebservice
import com.codepunk.core.data.repository.SessionRepositoryImpl
import com.codepunk.core.data.repository.UserRepositoryImpl
import com.codepunk.core.di.component.UserComponent
import com.codepunk.core.domain.contract.SessionRepository
import com.codepunk.core.domain.contract.UserRepository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * A [Module] that provides layer-independent, data-related instances for dependency injection.
 */
@Module
class DataModule {

    // region Methods

    /**
     * Provides an instance of [SessionRepository] for dependency injection.
     */
    @Provides
    @Singleton
    fun providesAccountRepository(
        sharedPreferences: SharedPreferences,
        userDao: UserDao,
        accountManager: AccountManager,
        userWebservice: UserWebservice,
        userComponentBuilder: UserComponent.Builder
    ): SessionRepository = SessionRepositoryImpl(
        sharedPreferences,
        userDao,
        accountManager,
        userWebservice,
        userComponentBuilder
    )

    /**
     * Provides an instance of [UserRepository] for dependency injection.
     */
    @Provides
    @Singleton
    fun providesUserRepository(
        userDao: UserDao,
        accountManager: AccountManager,
        sharedPreferences: SharedPreferences,
        userWebservice: UserWebservice
    ): UserRepository = UserRepositoryImpl(
        userDao,
        accountManager,
        sharedPreferences,
        userWebservice
    )

    // endregion Methods

}
