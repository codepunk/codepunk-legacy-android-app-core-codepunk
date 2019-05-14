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

import android.content.Context
import androidx.room.Room
import com.codepunk.core.data.local.dao.UserDao
import com.codepunk.core.data.local.database.CodepunkDb
import com.codepunk.core.di.qualifier.ApplicationContext
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * A [Module] that provides instances for data persistence (i.e., Room).
 */
@Module
class LocalModule {

    // region Methods

    /**
     * Provides a [CodepunkDb] instance.
     */
    @Singleton
    @Provides
    fun provideDb(@ApplicationContext context: Context): CodepunkDb = Room
        .databaseBuilder(context, CodepunkDb::class.java, "codepunk.db")
        .fallbackToDestructiveMigration()
        .build()

    /**
     * Creates a [UserDao] instance.
     */
    @Singleton
    @Provides
    fun providesUserDao(db: CodepunkDb): UserDao = db.userDao()

    // endregion Methods

}
