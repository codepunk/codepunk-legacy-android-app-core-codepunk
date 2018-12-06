/*
 * Copyright (C) 2018 Codepunk, LLC
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

package com.codepunk.core.data.util

/**
 * Abstract class that facilitates the mapping of a source data entity ([S]) to a destination
 * data entity ([D]). For example, a class that represents a locally-cached User might need to be
 * mapped to a User domain model for use in the app.
 */
abstract class Mapper<in S, D> {

    /**
     * Takes a source instance of type [S] and maps it to a new destination instance of type [D].
     */
    abstract fun map(source: S): D

}
