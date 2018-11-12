/*
 * Copyright (C) 2018 Codepunk, LLC
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

package com.codepunk.core.data.model.http

import android.os.Parcel
import android.os.Parcelable
import com.codepunk.core.util.toBundle
import com.codepunk.core.util.toMap

/**
 * A response message from the server. This response contains a [message] string and
 * optional [errors] detailing any issues discovered during the request.
 */
data class ResponseMessage(

    /**
     * A string message.
     */
    val message: String?,

    /**
     * Any errors that were discovered during the request.
     */
    val errors: Map<String, Array<String>>? = null

) : Parcelable {

    // region Constructors

    /**
     * Constructor that takes a [Parcel].
     */
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        toMap(parcel.readBundle(Array<String>::class.java.classLoader))
    )

    // endregion Constructors

    // region Implemented methods

    /**
     * Flattens this object in to the supplied [parcel]. Implementation of [Parcelable].
     */
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(message)
        parcel.writeBundle(errors.toBundle())
    }

    /**
     * Describes the kinds of special objects contained in this [Parcelable] instance's marshaled
     * representation. Implementation of [Parcelable].
     */
    override fun describeContents(): Int = 0

    // endregion Implemented methods

    // region Companion object

    /**
     * A public CREATOR field that generates instances of [ResponseMessage] from a [Parcel].
     */
    companion object CREATOR : Parcelable.Creator<ResponseMessage> {

        // region Inherited methods

        /**
         * Create a new instance of the [Parcelable] class, instantiating it from the given
         * [Parcel] whose data had previously been written by [Parcelable.writeToParcel].
         */
        override fun createFromParcel(parcel: Parcel): ResponseMessage = ResponseMessage(parcel)

        /**
         * Create a new array of [ResponseMessage].
         */
        override fun newArray(size: Int): Array<ResponseMessage?> = arrayOfNulls(size)

        // endregion Inherited methods

    }

    // endregion Companion object

}
