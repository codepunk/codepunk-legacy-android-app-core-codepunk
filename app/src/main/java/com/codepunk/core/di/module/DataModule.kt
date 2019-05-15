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
import com.codepunk.core.data.remote.webservice.AuthWebservice
import com.codepunk.core.data.remote.webservice.UserWebservice
import com.codepunk.core.data.repository.AuthRepositoryImpl
import com.codepunk.core.data.repository.SessionRepositoryImpl
import com.codepunk.core.di.component.UserComponent
import com.codepunk.core.domain.contract.AuthRepository
import com.codepunk.core.domain.contract.SessionRepository
import com.codepunk.doofenschmirtz.util.Translatinator
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * A [Module] that provides layer-independent, data-related instances for dependency injection.
 */
@Module
class DataModule {

    // region Methods

    /**
     * Provides an instance of [AuthRepository] for dependency injections.
     */
    @Provides
    @Singleton
    fun providesAuthRepository(
        authWebservice: AuthWebservice,
        userWebservice: UserWebservice,
        retrofit: Retrofit,
        translatinator: Translatinator
    ): AuthRepository = AuthRepositoryImpl(
        authWebservice,
        userWebservice,
        retrofit,
        translatinator
    )

    /**
     * Provides an instance of [SessionRepository] for dependency injection.
     */
    @Provides
    @Singleton
    fun providesSessionRepository(
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

    // endregion Methods

}
