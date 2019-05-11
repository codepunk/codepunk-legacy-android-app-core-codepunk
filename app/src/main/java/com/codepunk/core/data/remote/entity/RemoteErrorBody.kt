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

package com.codepunk.core.data.remote.entity

import android.os.Parcel
import android.os.Parcelable
import com.squareup.moshi.Json

/**
 * An error body response returned from the network. This response optionally contains a [message]
 * string, a [type], [errorDescription], a map of field-specific [errors], and a [hint] detailing
 * possible steps to mitigate the error.
 */
data class RemoteErrorBody(

    /**
     * A string message.
     */
    val message: String?,

    /**
     * The optional type describing this error body.
     */
    @field:Json(name = "error")
    val type: Type?,

    /**
     * An optional description of the error.
     */
    @field:Json(name = "error_description")
    val errorDescription: String?,

    /**
     * Any errors (relating to specific keys) that were discovered during the request.
     */
    val errors: Map<String, Array<String>>?,

    /**
     * An optional hint that describes what may have triggered the exception.
     */
    val hint: String?

) : Parcelable {

    // region Constructors

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readSerializable() as Type,
        parcel.readString(),
        readErrors(parcel),
        parcel.readString()
    )

    // endregion Constructors

    // region Implemented methods

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(message)
        dest.writeSerializable(type)
        dest.writeSerializable(errorDescription)
        writeErrors(dest, errors)
        dest.writeString(hint)
    }

    override fun describeContents(): Int = 0

    // endregion Implemented methods

    // region Nested/inner classes

    enum class Type(

        /**
         * The string value associated with the error body type.
         */
        val value: String

    ) {

        @Json(name = "codepunk::activatinator.inactive_user")
        INACTIVE_USER("codepunk::activatinator.inactive_user"),

        @Json(name = "invalid_credentials")
        INVALID_CREDENTIALS("invalid_credentials"),

        @Json(name = "invalid_request")
        INVALID_REQUEST("invalid_request")

    }

    // endregion Nested/inner classes

    // region Companion object

    companion object {

        // region Properties

        /**
         * A public CREATOR field that generates instances of [RemoteErrorBody] from a [Parcel].
         */
        @Suppress("unused")
        @JvmField
        val CREATOR: Parcelable.Creator<RemoteErrorBody> =
            object : Parcelable.Creator<RemoteErrorBody> {

                // region Methods

                override fun createFromParcel(source: Parcel): RemoteErrorBody =
                    RemoteErrorBody(source)

                override fun newArray(size: Int): Array<RemoteErrorBody?> = arrayOfNulls(size)

                // endregion methods

            }

        // endregion Properties

        // region Methods

        @JvmStatic
        private fun readErrors(parcel: Parcel): Map<String, Array<String>>? {
            val hasErrors = parcel.readByte().toInt() != 0
            return if (hasErrors) {
                val size = parcel.readInt()
                HashMap<String, Array<String>>(size).apply {
                    parcel.readString()?.also { key ->
                        val value = Array(parcel.readInt()) { "" }
                        parcel.readStringArray(value)
                        put(key, value)
                    }
                }
            } else {
                null
            }
        }

        @JvmStatic
        private fun writeErrors(parcel: Parcel, errors: Map<String, Array<String>>?) {
            errors?.also {
                parcel.writeByte(1)
                parcel.writeInt(it.size)
                it.entries.forEach { entry ->
                    parcel.writeString(entry.key)
                    parcel.writeInt(entry.value.size)
                    parcel.writeStringArray(entry.value)
                }
            } ?: parcel.writeByte(0)
        }

        // endregion methods

    }

    // endregion Companion object

}
