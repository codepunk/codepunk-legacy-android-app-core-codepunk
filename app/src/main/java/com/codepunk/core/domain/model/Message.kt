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
 * A class containing a single (optional) [message] (which is typically returned from the network)
 * as well as a [localizedMessage], which is the localized version of that message (if any
 * localization was found) or a copy of the original message otherwise.
 */
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

    /**
     * A public CREATOR field that generates instances of [Message] from a [Parcel].
     */
    companion object CREATOR : Parcelable.Creator<Message> {

        // region Inherited methods

        /**
         * Implementation of [Parcelable.Creator]. Create a new instance of [Message],
         * instantiating it from the given [Parcel] whose data had previously been written by
         * [Parcelable.writeToParcel].
         */
        override fun createFromParcel(parcel: Parcel): Message = Message(parcel)

        /**
         * Implementation of [Parcelable.Creator]. Create a new array of [Message].
         */
        override fun newArray(size: Int): Array<Message?> = arrayOfNulls(size)

        // endregion Inherited methods

    }

    // endregion Companion object

}
