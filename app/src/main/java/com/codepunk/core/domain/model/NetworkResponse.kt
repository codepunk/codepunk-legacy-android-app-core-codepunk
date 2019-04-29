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

package com.codepunk.core.domain.model

import android.os.Parcel
import android.os.Parcelable
import com.codepunk.core.lib.toBundle
import com.codepunk.core.lib.toMap

/**
 * A response message from a server. This response contains a [message] string and
 * optional [errors] detailing any issues discovered during the request.
 */
data class NetworkResponse(

    /**
     * A string message.
     */
    val message: String?,

    /**
     * A localized string message.
     */
    val localizedMessage: String?,

    /**
     * An optional error code.
     */
    val error: String?,

    /**
     * An optional error description.
     */
    val errorDescription: String?,

    /**
     * Any errors (relating to specific keys) that were discovered during the request.
     */
    val errors: Map<String, Array<String>>? = null

) : Parcelable {

    // region Constructors

    /**
     * Constructor that takes a [Parcel].
     */
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readBundle(Array<String>::class.java.classLoader).toMap()
    )

    // endregion Constructors

    // region Implemented methods

    /**
     * Flattens this object in to the supplied [parcel]. Implementation of [Parcelable].
     */
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(message)
        parcel.writeString(localizedMessage)
        parcel.writeString(error)
        parcel.writeString(errorDescription)
        parcel.writeBundle(errors.toBundle())
    }

    /**
     * Describes the kinds of special objects contained in this [Parcelable] instance's marshaled
     * representation. Implementation of [Parcelable].
     */
    override fun describeContents(): Int = 0

    // endregion Implemented methods

    // region Methods

    fun firstErrorOrNull(): Pair<String, String>? {
        val entry = errors?.entries?.firstOrNull()
        return entry?.let {
            Pair(entry.key, it.value.getOrElse(0) {
                message ?: ""
            })
        }
    }

    // endregion Methods

    // region Companion object

    companion object {

        // region Properties

        /**
         * A constant that corresponds to an attempt to log in to (or get an auth token for) a
         * user account that has never been activated.
         */
        val INACTIVE_USER = "codepunk::activatinator.inactive"

        /**
         * A constant that corresponds to a failed log in attempt due to invalid credentials
         * (i.e. username/password).
         */
        val INVALID_CREDENTIALS = "invalid_credentials"

        /**
         * A public CREATOR field that generates instances of [NetworkResponse] from a [Parcel].
         */
        @JvmField
        val CREATOR = object : Parcelable.Creator<NetworkResponse> {

            // region Inherited methods

            /**
             * Create a new instance of the [Parcelable] class, instantiating it from the given
             * [Parcel] whose data had previously been written by [Parcelable.writeToParcel].
             */
            override fun createFromParcel(parcel: Parcel): NetworkResponse =
                NetworkResponse(parcel)

            /**
             * Create a new array of [NetworkResponse].
             */
            override fun newArray(size: Int): Array<NetworkResponse?> = arrayOfNulls(size)

            // endregion Inherited methods

        }

        // endregion Properties

    }

    // endregion Companion object

}
