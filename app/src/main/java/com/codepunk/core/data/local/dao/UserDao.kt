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

package com.codepunk.core.data.local.dao

import androidx.room.*
import com.codepunk.core.data.local.entity.LocalUser

@Dao
abstract class UserDao {

    /**
     * Inserts a single user into the local database and returns the user ID.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insert(user: LocalUser): Long

    /**
     * Inserts a single user into the local database and returns the number of rows updated.
     */
    @Update(onConflict = OnConflictStrategy.IGNORE)
    abstract fun update(user: LocalUser): Int

    /**
     * Retrieves [LocalUser] information from the local database based on the supplied [username].
     */
    @Query("SELECT * FROM users WHERE username = :username")
    abstract fun retrieveByUsername(username: String): LocalUser?

    /**
     * Retrieves [LocalUser] information from the local database based on the supplied [email].
     */
    @Query("SELECT * FROM users WHERE email = :email")
    abstract fun retrieveByEmail(email: String): LocalUser?

}
