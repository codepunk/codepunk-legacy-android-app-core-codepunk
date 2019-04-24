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

/**
 * A class that describes a class of error returned from the network.
 */
class NetworkError(

    /**
     * The [Type] of this error.
     */
    val type: Type,

    /**
     * A string representing the original error type returned from the network.
     */
    val error: String?

) : Parcelable {

    // region Constructors

    /**
     * Constructor that takes a [Parcel].
     */
    constructor(parcel: Parcel) : this(
        parcel.readSerializable() as Type,
        parcel.readString()
    )

    // endregion Constructors

    // region Inherited methods

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NetworkError) return false

        if (type != other.type) return false
        if (error != other.error) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + (error?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "NetworkError(type=$type, error=$error)"
    }

    // endregion Inherited methods

// region Implemented methods

    /**
     * Flattens this object in to the supplied [parcel]. Implementation of [Parcelable].
     */
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeSerializable(type)
        parcel.writeString(error)
    }

    /**
     * Describes the kinds of special objects contained in this [Parcelable] instance's marshaled
     * representation. Implementation of [Parcelable].
     */
    override fun describeContents(): Int = 0

    // endregion Implemented methods

    // region Methods

    /**
     * Adds this NetworkError to [lookupMap] for quick lookup.
     */
    private fun addToMap(): NetworkError {
        error?.also {
            lookupMap[it] = this
        }
        return this
    }

    // endregion Methods

    // region Nested/inner classes

    /**
     * The type of this [NetworkError].
     */
    enum class Type {

        /**
         * A [Type] value indicating an inactive user.
         */
        INACTIVE_USER,

        /**
         * A [Type] value indicating an unknown or undefined type.
         */
        UNKNOWN

    }

    // endregion Nested/inner classes

    // region Companion object

    companion object {

        // region Properties

        /**
         * A CREATOR field used for parceling this class.
         */
        @Suppress("UNUSED")
        @JvmField
        val CREATOR = object : Parcelable.Creator<NetworkError> {
            override fun createFromParcel(parcel: Parcel): NetworkError {
                return NetworkError(parcel)
            }

            override fun newArray(size: Int): Array<NetworkError?> {
                return arrayOfNulls(size)
            }
        }

        /**
         * A [HashMap] of HTTP status codes for speedy lookup.
         */
        @JvmStatic
        private val lookupMap = HashMap<String, NetworkError>()

        /**
         * A [NetworkError] representing an error resulting from trying to log in to an
         * un-activated user account.
         */
        @Suppress("UNUSED")
        @JvmStatic
        private val INACTIVE_USER = NetworkError(
            Type.INACTIVE_USER,
            "codepunk::activatinator.inactive"
        ).addToMap()

        // endregion Properties

        // region Methods

        /**
         * Returns a predefined [NetworkError] if the supplied [error] matches one of the
         * predefined values, otherwise it creates a new NetworkError and with a type of
         * [Type.UNKNOWN].
         */
        @JvmStatic
        fun lookup(error: String): NetworkError =
            lookupMap[error] ?: NetworkError(Type.UNKNOWN, error)

        // endregion Methods

    }

    // endregion Companion object

}
