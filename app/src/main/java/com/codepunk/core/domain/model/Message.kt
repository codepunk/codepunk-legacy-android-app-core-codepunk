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

data class Message(

    /**
     * A string message.
     */
    val message: String?,

    /**
     * The localized string message.
     */
    val localizedMessage: String?

) : Parcelable {

    // region Constructors

    /**
     * Constructor that takes a [Parcel].
     */
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString()
    )

    // endregion Constructors

    // region Implemented methods

    /**
     * Flattens this object in to the supplied [parcel]. Implementation of [Parcelable].
     */
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(message)
        parcel.writeString(localizedMessage)
    }

    /**
     * Describes the kinds of special objects contained in this [Parcelable] instance's marshaled
     * representation. Implementation of [Parcelable].
     */
    override fun describeContents(): Int = 0

    // endregion Implemented methods

    // region Companion object

    companion object CREATOR : Parcelable.Creator<Message> {

        // region Inherited methods

        override fun createFromParcel(parcel: Parcel): Message = Message(parcel)

        override fun newArray(size: Int): Array<Message?> = arrayOfNulls(size)

        // endregion Inherited methods

    }

    // endreigon Companion object

}
