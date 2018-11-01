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

package com.codepunk.core.lib

import android.util.SparseArray
import com.codepunk.core.lib.HttpStatus.Category.*

/**
 * A convenience class for categorizing, working with and looking up HTTP status codes.
 */
class HttpStatus private constructor(

    /**
     * The integer value of the HTTP status code.
     */
    val code: Int,

    /**
     * A short reason phrase describing the HTTP status code.
     */
    val reasonPhrase: String = "",

    /**
     * The HTTP status code category (i.e. Information, Success, Server Error, etc.).
     */
    val category: Category = when (code) {
        in 100 until 200 -> INFORMATION
        in 200 until 300 -> SUCCESS
        in 300 until 400 -> REDIRECTION
        in 400 until 500 -> CLIENT_ERROR
        in 500 until 600 -> SERVER_ERROR
        else -> UNKNOWN
    }

) {

    // region Properties

    /**
     * A description of the HTTP status code that includes the integer value and reason phrase.
     */
    @Suppress("UNUSED", "WEAKER_ACCESS")
    val description: String by lazy {
        val phrase = when {
            reasonPhrase.isBlank() -> category.description
            else -> reasonPhrase
        }
        "$code $phrase"
    }

    /**
     * The toString value for this HttpStatus.
     */
    private val toString: String by lazy {
        "${javaClass.simpleName}(code=$code, reasonPhrase='$reasonPhrase', category=$category)"
    }

    // endregion Properties

    // region Inherited methods

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HttpStatus

        if (code != other.code) return false
        if (reasonPhrase != other.reasonPhrase) return false
        if (category != other.category) return false

        return true
    }

    override fun hashCode(): Int {
        var result = code
        result = 31 * result + reasonPhrase.hashCode()
        result = 31 * result + category.hashCode()
        return result
    }

    override fun toString(): String = toString

    // endregion Inherited methods

    // region Methods

    /**
     * Adds this HttpStatus to [lookupArray] for quick lookup.
     */
    private fun addToLookup(): HttpStatus {
        lookupArray.put(code, this)
        return this
    }

    // endregion Methods

    // region Nested/inner classes

    /**
     * An enum class representing the category of this HTTP status code.
     */
    enum class Category(

        /**
         * A code for this category, in the form of a string '1xx', '2xx', etc.
         */
        @Suppress("WEAKER_ACCESS")
        val code: String,

        /**
         * A short description of this category.
         */
        val description: String

    ) {

        /**
         * The request was received, continuing process.
         */
        INFORMATION("1xx", "Information"),

        /**
         * The request was successfully received, understood, and accepted.
         */
        SUCCESS("2xx", "Success"),

        /**
         * Further action needs to be taken in order to complete the request.
         */
        REDIRECTION("3xx", "Redirection"),

        /**
         * The request contains bad syntax or cannot be fulfilled.
         */
        CLIENT_ERROR("4xx", "Client Error"),

        /**
         * The server failed to fulfill an apparently valid request.
         */
        SERVER_ERROR("5xx", "Server Error"),

        /**
         * The category of the HTTP status code could not be determined.
         */
        UNKNOWN("???", "Unknown");

        override fun toString(): String {
            return "Category(code='$code', description='$description')"
        }
    }

    // endregion Nester/inner classes

    // region Companion object

    @Suppress("UNUSED")
    companion object {

        // region Properties

        /**
         * A [SparseArray] of HTTP status codes for speedy lookup.
         */
        private val lookupArray = SparseArray<HttpStatus>()

        /**
         * The server has received the request headers and the client should proceed to send the
         * request body.
         */
        @JvmStatic
        val CONTINUE = HttpStatus(100, "Continue").addToLookup()

        /**
         * The requester has asked the server to switch protocols and the server has agreed to do
         * so.
         */
        @JvmStatic
        val SWITCHING_PROTOCOLS = HttpStatus(101, "Switching Protocols")
            .addToLookup()

        /**
         * Standard response for successful HTTP requests.
         */
        @JvmStatic
        val OK = HttpStatus(200, "OK").addToLookup()

        /**
         * The request has been fulfilled, resulting in the creation of a new resource.
         */
        @JvmStatic
        val CREATED = HttpStatus(201, "Created").addToLookup()

        /**
         * The request has been accepted for processing, but the processing has not been completed.
         */
        @JvmStatic
        val ACCEPTED = HttpStatus(202, "Accepted").addToLookup()

        /**
         * The server is a transforming proxy (e.g. a Web accelerator) that received a 200 OK from
         * its origin, but is returning a modified version of the origin's response.
         */
        @JvmStatic
        val NON_AUTHORITATIVE_INFORMATION = HttpStatus(
            203,
            "Non-Authoritative Information"
        ).addToLookup()

        /**
         * The server successfully processed the request and is not returning any content.
         */
        @JvmStatic
        val NO_CONTENT = HttpStatus(204, "No Content").addToLookup()

        /**
         * The server successfully processed the request, but is not returning any content. Unlike
         * a NO_CONTENT response, this response requires that the requester reset the document
         * view.
         */
        @JvmStatic
        val RESET_CONTENT = HttpStatus(205, "Reset Content").addToLookup()

        /**
         * The server is delivering only part of the resource (byte serving) due to a range header
         * sent by the client.
         */
        @JvmStatic
        val PARTIAL_CONTENT = HttpStatus(206, "Partial Content").addToLookup()

        /**
         * Indicates multiple options for the resource from which the client may choose (via
         * agent-driven content negotiation).
         */
        @JvmStatic
        val MULTIPLE_CHOICES = HttpStatus(300, "Multiple Choices").addToLookup()

        /**
         * This and all future requests should be directed to the given URI.
         */
        @JvmStatic
        val MOVED_PERMANENTLY = HttpStatus(300, "Moved Permanently").addToLookup()

        /**
         * Tells the client to look at (browse to) another url.
         */
        @JvmStatic
        val FOUND = HttpStatus(302, "Found").addToLookup()

        /**
         * The response to the request can be found under another URI using the GET method.
         */
        @JvmStatic
        val SEE_OTHER = HttpStatus(303, "See Other").addToLookup()

        /**
         * Indicates that the resource has not been modified since the version specified by the
         * request headers If-Modified-Since or If-None-Match.
         */
        @JvmStatic
        val NOT_MODIFIED = HttpStatus(304, "Not Modified").addToLookup()

        /**
         * The requested resource is available only through a proxy, the address for which is
         * provided in the response.
         */
        @JvmStatic
        val USE_PROXY = HttpStatus(305, "Use Proxy").addToLookup()

        /**
         * The server cannot or will not process the request due to an apparent client error.
         */
        @JvmStatic
        val BAD_REQUEST = HttpStatus(400, "Bad Request").addToLookup()

        /**
         * Similar to 403 Forbidden, but specifically for use when authentication is required and
         * has failed or has not yet been provided.
         */
        @JvmStatic
        val UNAUTHORIZED = HttpStatus(401, "Unauthorized").addToLookup()

        /**
         * Reserved for future use. The original intention was that this code might be used as part
         * of some form of digital cash or micropayment scheme but that has not yet happened, and
         * this code is not usually used.
         */
        @JvmStatic
        val PAYMENT_REQUIRED = HttpStatus(402, "Payment Required").addToLookup()

        /**
         * The request was valid, but the server is refusing action.
         */
        @JvmStatic
        val FORBIDDEN = HttpStatus(403, "Forbidden").addToLookup()

        /**
         * The requested resource could not be found but may be available in the future.
         */
        @JvmStatic
        val NOT_FOUND = HttpStatus(404, "Not Found").addToLookup()

        /**
         * A request method is not supported for the requested resource.
         */
        @JvmStatic
        val METHOD_NOT_ALLOWED = HttpStatus(405, "Method Not Allowed")
            .addToLookup()

        /**
         * The requested resource is capable of generating only content not acceptable according to
         * the Accept headers sent in the request.
         */
        @JvmStatic
        val NOT_ACCEPTABLE = HttpStatus(406, "Not Acceptable").addToLookup()

        /**
         * The client must first authenticate itself with the proxy.
         */
        @JvmStatic
        val PROXY_AUTHENTICATION_REQUIRED = HttpStatus(
            407,
            "Proxy Authentication Required"
        ).addToLookup()

        /**
         * The server timed out waiting for the request.
         */
        @JvmStatic
        val REQUEST_TIMEOUT = HttpStatus(408, "Request Timeout").addToLookup()

        /**
         * Indicates that the request could not be processed because of conflict in the current
         * state of the resource.
         */
        @JvmStatic
        val CONFLICT = HttpStatus(409, "Conflict").addToLookup()

        /**
         * Indicates that the resource requested is no longer available and will not be available
         * again.
         */
        @JvmStatic
        val GONE = HttpStatus(410, "Gone").addToLookup()

        /**
         * The request did not specify the length of its content, which is required by the requested
         * resource.
         */
        @JvmStatic
        val LENGTH_REQUIRED = HttpStatus(411, "Length Required").addToLookup()

        /**
         * The server does not meet one of the preconditions that the requester put on the request.
         */
        @JvmStatic
        val PRECONDITION_FAILED = HttpStatus(412, "Precondition Failed")
            .addToLookup()

        /**
         * The request is larger than the server is willing or able to process.
         */
        @JvmStatic
        val PAYLOAD_TOO_LARGE = HttpStatus(413, "Payload Too Large").addToLookup()

        /**
         * The URI provided was too long for the server to process.
         */
        @JvmStatic
        val REQUEST_URI_TOO_LONG = HttpStatus(414, "Request-URI Too Long")
            .addToLookup()

        /**
         * The request entity has a media type which the server or resource does not support.
         */
        @JvmStatic
        val UNSUPPORTED_MEDIA_TYPE = HttpStatus(415, "Unsupported Media Type")
            .addToLookup()

        /**
         * The client has asked for a portion of the file (byte serving) but the server cannot
         * supply that portion.
         */
        @JvmStatic
        val REQUESTED_RANGE_NOT_SATISFIABLE = HttpStatus(
            416,
            "Requested Range Not Satisfiable"
        ).addToLookup()

        /**
         * The server cannot meet the requirements of the Expect request-header field.
         */
        @JvmStatic
        val EXPECTATION_FAILED = HttpStatus(417, "Expectation Failed")
            .addToLookup()

        /**
         * This code was defined in 1998 as an April Fools' joke and is not expected to be
         * implemented by actual HTTP servers.
         */
        @JvmStatic
        val IM_A_TEAPOT = HttpStatus(418, "I'm a Teapot").addToLookup()

        /**
         * The request was well-formed but was unable to be followed due to semantic errors.
         */
        @JvmStatic
        val UNPROCESSABLE_ENTITY = HttpStatus(422, "Unprocessable Entity")
            .addToLookup()

        /**
         * A generic error message, given when an unexpected condition was encountered and no more
         * specific message is suitable.
         */
        @JvmStatic
        val INTERNAL_SERVER_ERROR = HttpStatus(500, "Internal Server Error")
            .addToLookup()

        /**
         * The server either does not recognize the request method, or it lacks the ability to
         * fulfill the request.
         */
        @JvmStatic
        val NOT_IMPLEMENTED = HttpStatus(501, "Not Implemented").addToLookup()

        /**
         * The server was acting as a gateway or proxy and received an invalid response from the
         * upstream server.
         */
        @JvmStatic
        val BAD_GATEWAY = HttpStatus(502, "Bad Gateway").addToLookup()

        /**
         * The server is currently unavailable (because it is overloaded or down for maintenance).
         */
        @JvmStatic
        val SERVICE_UNAVAILABLE = HttpStatus(503, "Service Unavailable")
            .addToLookup()

        /**
         * The server was acting as a gateway or proxy and did not receive a timely response from
         * the upstream server.
         */
        @JvmStatic
        val GATEWAY_TIMEOUT = HttpStatus(504, "Gateway Timeout").addToLookup()

        /**
         * The server does not support the HTTP protocol version used in the request.
         */
        @JvmStatic
        val HTTP_VERSION_NOT_SUPPORTED = HttpStatus(
            505,
            "HTTP Version Not Supported"
        ).addToLookup()

        // endregion Properties

        // region Methods

        /**
         * Returns a predefined [HttpStatus] if the [code] matches one of the predefined values,
         * otherwise it creates a new HttpStatus and attempts to match the category of the
         * given code.
         */
        fun lookup(code: Int): HttpStatus {
            val predefined = lookupArray[code]
            return when (predefined) {
                null -> HttpStatus(code)
                else -> predefined
            }
        }

        // endregion Methods

    }

    // endregion Companion object

}
