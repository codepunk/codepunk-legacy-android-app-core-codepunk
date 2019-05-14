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

package com.codepunk.core.di.module

import android.content.Context
import com.codepunk.core.data.remote.adapter.BooleanIntAdapter
import com.codepunk.core.data.remote.adapter.DateJsonAdapter
import com.codepunk.core.data.remote.converter.MoshiEnumConverterFactory
import com.codepunk.core.data.remote.interceptor.AuthorizationInterceptor
import com.codepunk.core.data.remote.webservice.AuthWebservice
import com.codepunk.core.data.remote.webservice.AuthWebserviceWrapper
import com.codepunk.core.data.remote.webservice.UserWebservice
import com.codepunk.core.di.qualifier.ApplicationContext
import com.codepunk.core.util.NetworkTranslator
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.*
import javax.inject.Singleton

private const val CACHE_SIZE: Long = 10 * 1024 * 1024

//private const val BASE_URL: String = "https://d5357798.ngrok.io"
private const val BASE_URL: String = "http://192.168.12.10"
//private const val BASE_URL: String = "https://codepunk.test"

/**
 * A [Module] that provides network-specific instances for dependency injection.
 */
@Module
class NetModule {

    // region Methods

    /**
     * Provides an instance of [Cache] for dependency injection.
     */
    @Provides
    @Singleton
    fun providesCache(@ApplicationContext context: Context): Cache =
        Cache(context.cacheDir, CACHE_SIZE)

    /**
     * Provides an instance of [OkHttpClient] for dependency injection.
     */
    @Provides
    @Singleton
    fun providesOkHttpClient(
        cache: Cache,
        authorizationInterceptor: AuthorizationInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .cache(cache)
        .addInterceptor(authorizationInterceptor)
        .build()

    /**
     * Provides an instance of [Moshi] for dependency injection.
     */
    @Provides
    @Singleton
    fun providesMoshi(
        booleanIntAdapter: BooleanIntAdapter,
        dateJsonAdapter: DateJsonAdapter
    ): Moshi = Moshi.Builder()
        .add(booleanIntAdapter)
        .add(Date::class.java, dateJsonAdapter)
        .build()

    /**
     * Provides an instance of [MoshiConverterFactory] for dependency injection.
     */
    @Provides
    @Singleton
    fun providesConverterFactory(moshi: Moshi): MoshiConverterFactory =
        MoshiConverterFactory.create(moshi)

    /**
     * Provides an instance of [Retrofit] for dependency injection.
     */
    @Provides
    @Singleton
    fun providesRetrofit(
        okHttpClient: OkHttpClient,
        moshiConverterFactory: MoshiConverterFactory,
        moshiEnumConverterFactory: MoshiEnumConverterFactory
    ): Retrofit = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl(BASE_URL)
        .addConverterFactory(moshiConverterFactory)
        .addConverterFactory(moshiEnumConverterFactory)
        .build()

    /**
     * Provides an instance of [AuthWebservice] for making authentication API calls.
     */
    @Provides
    @Singleton
    fun providesAuthWebservice(
        retrofit: Retrofit
    ): AuthWebservice = AuthWebserviceWrapper(retrofit.create(AuthWebservice::class.java))

    /**
     * Provides an instance of [UserWebservice] for making user API calls.
     */
    @Provides
    @Singleton
    fun providesUserWebservice(
        retrofit: Retrofit
    ): UserWebservice = retrofit.create(UserWebservice::class.java)

    /**
     * Provides an instance of [NetworkTranslator] for translating messages from the network.
     */
    @Provides
    @Singleton
    fun providesNetworkTranslator(@ApplicationContext context: Context): NetworkTranslator =
        NetworkTranslator(context)

    // endregion Methods

}
