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
import com.codepunk.core.di.qualifier.ApplicationContext
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

private const val CACHE_SIZE: Long = 10 * 1024 * 1024

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
    fun providesOkHttpClient(cache: Cache): OkHttpClient =
            OkHttpClient.Builder()
                .cache(cache)
                .build()

    /**
     * Provides an instance of [Moshi] for dependency injection.
     */
    @Provides
    @Singleton
    fun providesMoshi(): Moshi =
        Moshi.Builder().build()

    /**
     * Provides an instance of [Converter.Factory] for dependency injection.
     */
    @Provides
    @Singleton
    fun providesConverterFactory(moshi: Moshi): Converter.Factory =
            MoshiConverterFactory.create(moshi)

    /**
     * Provides an instance of [Retrofit] for dependency injection.
     */
    @Provides
    @Singleton
    fun providesRetrofit(
        okHttpClient: OkHttpClient,
        converterFactory: Converter.Factory
    ): Retrofit = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl("https://www.codepunk.com") /* TODO */
        .addConverterFactory(converterFactory)
        .build()

    // endregion Methods

}
