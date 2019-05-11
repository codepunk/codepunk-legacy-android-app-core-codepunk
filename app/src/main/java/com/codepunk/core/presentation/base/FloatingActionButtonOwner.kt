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

package com.codepunk.core.presentation.base

import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * A simple interface that indicates that a class contains a [FloatingActionButton].
 */
interface FloatingActionButtonOwner {

    // region Properties

    /**
     * Returns the [FloatingActionButton].
     */
    val floatingActionButton: FloatingActionButton

    /**
     * An instance of [FloatingActionButtonListener].
     */
    var floatingActionButtonListener: FloatingActionButtonListener?

    // endregion Properties

    // region Nested/inner classes

    /**
     * A listener for events related to a floating action button.
     */
    interface FloatingActionButtonListener {

        // region Methods

        /**
         * Triggered when the floating action button is clicked.
         */
        fun onFloatingActionButtonClick(owner: FloatingActionButtonOwner)

        // endregion Methods
    }

    // endregion Nested/inner classes

}
