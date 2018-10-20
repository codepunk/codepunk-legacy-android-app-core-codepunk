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

/**
 * A class that enumerates Http status codes along with their associated descriptions.
 *
 * See https://en.wikipedia.org/wiki/List_of_HTTP_status_codes
 */
enum class HttpStatus(

    /**
     * The HTTP status code.
     */
    val code: Int,

    /**
     * The text associated with the HTTP status.
     */
    val text: String

) {

    /**
     * The server has received the request headers and the client should proceed to send the
     * request body.
     */
    CONTINUE(100, "Continue"),

    /**
     * The requester has asked the server to switch protocols and the server has agreed to do so.
     */
    SWITCHING_PROTOCOLS(101, "Switching Protocols"),

    /**
     * Standard response for successful HTTP requests.
     */
    OK(200, "OK"),

    /**
     * The request has been fulfilled, resulting in the creation of a new resource.
     */
    CREATED(201, "Created"),

    /**
     * The request has been accepted for processing, but the processing has not been completed.
     */
    ACCEPTED(202, "Accepted"),

    /**
     * The server is a transforming proxy (e.g. a Web accelerator) that received a 200 OK from its
     * origin, but is returning a modified version of the origin's response.
     */
    NON_AUTHORITATIVE_INFORMATION(203, "Non-Authoritative Information"),

    /**
     * The server successfully processed the request and is not returning any content.
     */
    NO_CONTENT(204, "No Content"),

    /**
     * The server successfully processed the request, but is not returning any content. Unlike a
     * NO_CONTENT response, this response requires that the requester reset the document view.
     */
    RESET_CONTENT(205, "Reset Content"),

    /**
     * The server is delivering only part of the resource (byte serving) due to a range header sent
     * by the client.
     */
    PARTIAL_CONTENT(206, "Partial Content"),

    /**
     * Indicates multiple options for the resource from which the client may choose (via
     * agent-driven content negotiation).
     */
    MULTIPLE_CHOICES(300, "Multiple Choices"),

    /**
     * This and all future requests should be directed to the given URI.
     */
    MOVED_PERMANENTLY(300, "Moved Permanently"),

    /**
     * Tells the client to look at (browse to) another url.
     */
    FOUND(302, "Found"),

    /**
     * The response to the request can be found under another URI using the GET method.
     */
    SEE_OTHER(303, "See Other"),

    /**
     * Indicates that the resource has not been modified since the version specified by the
     * request headers If-Modified-Since or If-None-Match.
     */
    NOT_MODIFIED(304, "Not Modified"),

    /**
     * The requested resource is available only through a proxy, the address for which is provided
     * in the response.
     */
    USE_PROXY(305, "Use Proxy"),

    /**
     * The server cannot or will not process the request due to an apparent client error.
     */
    BAD_REQUEST(400, "Bad Request"),

    /**
     * Similar to 403 Forbidden, but specifically for use when authentication is required and has
     * failed or has not yet been provided.
     */
    UNAUTHORIZED(401, "Unauthorized"),

    /**
     * Reserved for future use. The original intention was that this code might be used as part of
     * some form of digital cash or micropayment scheme but that has not yet happened, and this
     * code is not usually used.
     */
    PAYMENT_REQUIRED(402, "Payment Required"),

    /**
     * The request was valid, but the server is refusing action.
     */
    FORBIDDEN(403, "Forbidden"),

    /**
     * The requested resource could not be found but may be available in the future.
     */
    NOT_FOUND(404, "Not Found"),

    /**
     * A request method is not supported for the requested resource.
     */
    METHOD_NOT_ALLOWED(405, "Method Not Allowed"),

    /**
     * The requested resource is capable of generating only content not acceptable according to
     * the Accept headers sent in the request.
     */
    NOT_ACCEPTABLE(406, "Not Acceptable"),

    /**
     * The client must first authenticate itself with the proxy.
     */
    PROXY_AUTHENTICATION_REQUIRED(407, "Proxy Authentication Required"),

    /**
     * The server timed out waiting for the request.
     */
    REQUEST_TIMEOUT(408, "Request Timeout"),

    /**
     * Indicates that the request could not be processed because of conflict in the current state
     * of the resource.
     */
    CONFLICT(409, "Conflict"),

    /**
     * Indicates that the resource requested is no longer available and will not be available again.
     */
    GONE(410, "Gone"),

    /**
     * The request did not specify the length of its content, which is required by the requested
     * resource.
     */
    LENGTH_REQUIRED(411, "Length Required"),

    /**
     * The server does not meet one of the preconditions that the requester put on the request.
     */
    PRECONDITION_FAILED(412, "Precondition Failed"),

    /**
     * The request is larger than the server is willing or able to process.
     */
    PAYLOAD_TOO_LARGE(413, "Payload Too Large"),

    /**
     * The URI provided was too long for the server to process.
     */
    REQUEST_URI_TOO_LONG(414, "Request-URI Too Long"),

    /**
     * The request entity has a media type which the server or resource does not support.
     */
    UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),

    /**
     * The client has asked for a portion of the file (byte serving), but the server cannot supply
     * that portion.
     */
    REQUESTED_RANGE_NOT_SATISFIABLE(416, "Requested Range Not Satisfiable"),

    /**
     * The server cannot meet the requirements of the Expect request-header field.
     */
    EXPECTATION_FAILED(417, "Expectation Failed"),

    /**
     * This code was defined in 1998 as an April Fools' joke and is not expected to be implemented
     * by actual HTTP servers.
     */
    IM_A_TEAPOT(418, "I'm a Teapot"),

    /**
     * The request was well-formed but was unable to be followed due to semantic errors.
     */
    UNPROCESSABLE_ENTITY(422, "Unprocessable Entity"),

    /**
     * A generic error message, given when an unexpected condition was encountered and no more
     * specific message is suitable.
     */
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),

    /**
     * The server either does not recognize the request method, or it lacks the ability to fulfill
     * the request.
     */
    NOT_IMPLEMENTED(501, "Not Implemented"),

    /**
     * The server was acting as a gateway or proxy and received an invalid response from the
     * upstream server.
     */
    BAD_GATEWAY(502, "Bad Gateway"),

    /**
     * The server is currently unavailable (because it is overloaded or down for maintenance).
     */
    SERVICE_UNAVAILABLE(503, "Service Unavailable"),

    /**
     * The server was acting as a gateway or proxy and did not receive a timely response from the
     * upstream server.
     */
    GATEWAY_TIMEOUT(504, "Gateway Timeout"),

    /**
     * The server does not support the HTTP protocol version used in the request.
     */
    HTTP_VERSION_NOT_SUPPORTED(505, "HTTP Version Not Supported");

    // region Properties

    /**
     * A full descriptionOf that includes the code and text of the status.
     */
    val description: String = "$code $text"

    // endregion Properties

    // region Companion object

    companion object {

        // region Properties

        /**
         * A lookup [SparseArray] for looking up [HttpStatus] values by status code.
         */
        private val lookupArray by lazy {
            SparseArray<HttpStatus>(values().size).apply {
                for (type in HttpStatus.values()) {
                    put(type.code, type)
                }
            }
        }

        // endregion Properties

        // region Methods

        /**
         * Gets the [HttpStatus] associated with the supplied [code], or null if no such value
         * exists.
         */
        @Suppress("WEAKER_ACCESS")
        fun lookup(code: Int): HttpStatus? = lookupArray[code]

        /**
         * Gets a descriptionOf
         */
        fun descriptionOf(code: Int, notFoundText: String = "Unknown"): String =
            lookup(code)?.description ?: "$code $notFoundText"

        // endregion Methods

    }

    // endregion Companion object

}

