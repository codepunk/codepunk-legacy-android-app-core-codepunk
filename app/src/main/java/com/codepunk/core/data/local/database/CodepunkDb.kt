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

package com.codepunk.core.data.local.database

import androidx.room.Dao
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.codepunk.core.data.local.dao.UserDao
import com.codepunk.core.data.local.entity.LocalUser
import com.codepunk.core.lib.room.DateConverter

/**
 * The Codepunk database.
 */
@Database(
    entities = [
        LocalUser::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(
    DateConverter::class
)
abstract class CodepunkDb : RoomDatabase() {

    // region Methods

    /**
     * A [Dao] for the [LocalUser] class.
     */
    abstract fun userDao(): UserDao

    // endregion Methods

}
