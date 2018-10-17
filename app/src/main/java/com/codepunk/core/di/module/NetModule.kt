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

package com.codepunk.core.di.module

import android.content.Context
import com.codepunk.core.data.remote.adapter.BooleanIntAdapter
import com.codepunk.core.data.remote.adapter.DateJsonAdapter
import com.codepunk.core.data.remote.converter.MoshiEnumConverterFactory
import com.codepunk.core.data.remote.interceptor.AuthorizationInterceptor
import com.codepunk.core.data.remote.webservice.AuthWebservice
import com.codepunk.core.data.remote.webservice.AuthWebserviceWrapper
import com.codepunk.core.data.remote.webservice.UserWebservice
import com.codepunk.core.di.qualifier.ApplicationContext
import com.squareup.moshi.Moshi
//import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory TODO Maybe not needed
import dagger.Module
import dagger.Provides
import okhttp3.Cache
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okio.Buffer
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException
import java.io.InputStream
import java.security.GeneralSecurityException
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.util.*
import javax.inject.Singleton
import javax.net.ssl.*

private const val CACHE_SIZE: Long = 10 * 1024 * 1024

/**
 * A [Module] that provides network-specific instances for dependency injection.
 */
@Module
class NetModule {

    // region Methods

    /**
     * Provides an instance of [Cache] for dependency injection.
     */
    @Provides
    @Singleton
    fun providesCache(@ApplicationContext context: Context): Cache =
        Cache(context.cacheDir, CACHE_SIZE)

    /**
     * Provides an instance of [X509TrustManager] specifically for accessing a local build
     * of the Codepunk webservice.
     */
    @Provides
    @Singleton
    fun providesTrustManager(): X509TrustManager = trustManagerForCertificates(
        trustedCertificatesInputStream()
    )

    /**
     * Provides an instance of [SSLSocketFactory] specifically for accessing a local build
     * of the Codepunk webservice.
     */
    @Provides
    @Singleton
    fun providesSslSocketFactory(trustManager: X509TrustManager): SSLSocketFactory {
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, arrayOf(trustManager), null)
        return sslContext.socketFactory
    }

    /**
     * Provides an instance of [OkHttpClient] for dependency injection.
     */
    @Provides
    @Singleton
    fun providesOkHttpClient(
        cache: Cache,
        sslSocketFactory: SSLSocketFactory,
        trustManager: X509TrustManager,
        authorizationInterceptor: AuthorizationInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .cache(cache)
        .sslSocketFactory(sslSocketFactory, trustManager)
        .addInterceptor(authorizationInterceptor)
        .build()

    /**
     * Provides an instance of [Moshi] for dependency injection.
     */
    @Provides
    @Singleton
    fun providesMoshi(
        booleanIntAdapter: BooleanIntAdapter,
        dateJsonAdapter: DateJsonAdapter
    ): Moshi = Moshi.Builder()
        // .add(KotlinJsonAdapterFactory()) // TODO Maybe not needed
        .add(booleanIntAdapter)
        .add(Date::class.java, dateJsonAdapter)
        .build()

    /**
     * Provides an instance of [MoshiConverterFactory] for dependency injection.
     */
    @Provides
    @Singleton
    fun providesConverterFactory(moshi: Moshi): MoshiConverterFactory =
        MoshiConverterFactory.create(moshi)

    /**
     * Provides an instance of [Retrofit] for dependency injection.
     */
    @Provides
    @Singleton
    fun providesRetrofit(
        okHttpClient: OkHttpClient,
        moshiConverterFactory: MoshiConverterFactory,
        moshiEnumConverterFactory: MoshiEnumConverterFactory
    ): Retrofit = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl("https://codepunk.test") /* TODO */
        .addConverterFactory(moshiConverterFactory)
        .addConverterFactory(moshiEnumConverterFactory)
        .build()

    /**
     * Provides an instance of [AuthWebservice] for making authorization API calls.
     */
    @Provides
    @Singleton
    fun providesAuthWebservice(
        retrofit: Retrofit
    ): AuthWebservice = AuthWebserviceWrapper(retrofit.create(AuthWebservice::class.java))

    /**
     * Provides an instance of [UserWebservice] for making user API calls.
     */
    @Provides
    @Singleton
    fun providesUserWebservice(
        retrofit: Retrofit
    ): UserWebservice = retrofit.create(UserWebservice::class.java)

    // endregion Methods

    // region Companion object

    companion object {

        // region Methods

        /**
         * Returns a trust manager that trusts `certificates` and none other. HTTPS services whose
         * certificates have not been signed by these certificates will fail with a
         * [SSLHandshakeException].
         *
         * This can be used to replace the host platform's built-in trusted certificates with a
         * custom set. This is useful in development where certificate authority-trusted
         * certificates aren't available. Or in production, to avoid reliance on third-party
         * certificate authorities.
         *
         * See also [CertificatePinner], which can limit trusted certificates while still using
         * the host platform's built-in trust store.
         *
         * Warning: Customizing Trusted Certificates is Dangerous!
         *
         * Relying on your own trusted certificates limits your server team's ability to update
         * their TLS certificates. By installing a specific set of trusted certificates, you take
         * on additional operational complexity and limit your ability to migrate between
         * certificate authorities. Do not use custom trusted certificates in production without
         * the blessing of your server's TLS administrator.
         */
        @Throws(GeneralSecurityException::class)
        private fun trustManagerForCertificates(stream: InputStream): X509TrustManager {
            val certificateFactory = CertificateFactory.getInstance("X.509")
            val certificates =
                certificateFactory.generateCertificates(stream)
            if (certificates.isEmpty()) {
                throw IllegalArgumentException("expected non-empty set of trusted certificates")
            }

            // Put the certificates a key store.
            val password = "password".toCharArray()
            val keyStore = newEmptyKeyStore(password)
            for ((index, certificate) in certificates.withIndex()) {
                val certificateAlias: String = Integer.toString(index)
                keyStore.setCertificateEntry(certificateAlias, certificate)
            }

            // Use it to build an X509 trust manager.
            val keyManagerFactory =
                KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
            keyManagerFactory.init(keyStore, password)
            val trustManagerFactory =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            trustManagerFactory.init(keyStore)
            val trustManagers = trustManagerFactory.trustManagers
            if (trustManagers.size != 1 || (trustManagers[0] !is X509TrustManager)) {
                throw IllegalStateException(
                    "Unexpected default trust managers:" + Arrays.toString(trustManagers)
                )
            }
            return trustManagers[0] as X509TrustManager
        }

        /**
         * Generates an empty keystore.
         */
        @Throws(GeneralSecurityException::class)
        private fun newEmptyKeyStore(password: CharArray): KeyStore {
            try {
                val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
                val stream: InputStream? = null // By convention, 'null' creates an empty key store
                keyStore.load(stream, password)
                return keyStore
            } catch (e: IOException) {
                throw AssertionError(e)
            }
        }

        /**
         * Returns an input stream containing one or more certificate PEM files. This
         * implementation just embeds the PEM files in Java strings; most applications will
         * instead read this from a resource file that gets bundled with the application.
         */
        private fun trustedCertificatesInputStream(): InputStream {
            // PEM file for Codepunk root certificate. This CAs is sufficient to view
            // https://codepunk.test in a local laravel/homestead installation.
            @Suppress("SpellCheckingInspection")
            val codepunkRootCertificationAuthority = "" +
                    "-----BEGIN CERTIFICATE-----\n" +
                    "MIIE9DCCAtygAwIBAgIJAImoOObaNoHrMA0GCSqGSIb3DQEBCwUAMEUxEDAOBgNV\n" +
                    "BAoMB1ZhZ3JhbnQxCzAJBgNVBAYTAlVOMSQwIgYDVQQDDBtIb21lc3RlYWQgaG9t\n" +
                    "ZXN0ZWFkIFJvb3QgQ0EwHhcNMTgwODA4MTkwMzI5WhcNMTkwODA4MTkwMzI5WjA3\n" +
                    "MRAwDgYDVQQKDAdWYWdyYW50MQswCQYDVQQGEwJVTjEWMBQGA1UEAwwNY29kZXB1\n" +
                    "bmsudGVzdDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBANn+gDiu7IOR\n" +
                    "wspgF0N9beCXVPuw726h/C5yP3oQsH/61Fgj55qdYgL7P+beEEjYv5JP+Q1vvMwz\n" +
                    "hW9L/Fa6f/Gc0RgZzkofDl1GiuWb62mbYblyESK8epxzV2YX58k+IpTRkf+Pbq9M\n" +
                    "AJJaQOkp1XkSnxarCzpOfVqXPQrelN33pWbhLMKUn2ouz7KUMw1lGrufUSbBX9yF\n" +
                    "5Ta9J6ReK/fBASVxbM4Pq31asbArUJyzLe8ZKQF+wcrHhDLd18OrKYVp3iDbXfVI\n" +
                    "ZBahpwHeQ7jwAfHF6XMhXSFF0NEvQajsTmbl7wuTvPNC+83+iczW6irPPotguStA\n" +
                    "VgXxEDK2BbMCAwEAAaOB9DCB8TB1BgNVHSMEbjBsgBSzjoxTL8lo+2k0OS5BYen7\n" +
                    "B62pLKFJpEcwRTEQMA4GA1UECgwHVmFncmFudDELMAkGA1UEBhMCVU4xJDAiBgNV\n" +
                    "BAMMG0hvbWVzdGVhZCBob21lc3RlYWQgUm9vdCBDQYIJAIUvCTLQk5SyMAkGA1Ud\n" +
                    "EwQCMAAwEwYDVR0lBAwwCgYIKwYBBQUHAwEwDgYDVR0PAQH/BAQDAgWgMCkGA1Ud\n" +
                    "EQQiMCCCDWNvZGVwdW5rLnRlc3SCDyouY29kZXB1bmsudGVzdDAdBgNVHQ4EFgQU\n" +
                    "hb0wVNsQu8ZhWEW39auMGu5adQAwDQYJKoZIhvcNAQELBQADggIBAEEFXdvy/O1v\n" +
                    "XBGqKG3rMga3fQxtZqoxK2Qe0to7r5w8ojGbv17DavIoBu2TBUlTuhzwVuhzZI4n\n" +
                    "8mX1j3UfsKdwoBdSTEeXef5MKhwXOreXxLLzyO0FUu5j5qf1lKdPIEpOeSr9rlIe\n" +
                    "VrN+A6nEYSIj/O6k9gGUH2zG6SeJqPbZS2KMtiHFDQVgwq6F8TAJSjHZZAgc1ZmJ\n" +
                    "JG8dHMRWCif0U0vv/2a0k9yyEPVW6Eh0BSYFSIHhGhOifwaAUU+IjJmeVMX2BExk\n" +
                    "97mfeJKazjrE3PnEFva2fp9rFxnU+tYpkwN9c4/xoEAsfeqMY6nBLRHMzOXG4K1U\n" +
                    "UFZjceHwAOjhqmbKYDjl29O4P6zA1OKTPbUGB8Jq0IjWhoINPTFBauKZWh+VlWTq\n" +
                    "tEaMLGF7ckvVQeDsdOsSdKxT8O/VpNvoJ1js/34tQyTNvXbcskCqqT221UZOVTGQ\n" +
                    "jw+k7Th8lLLLSx7l3jby3lfYVX71+6oFWnEuhk4eEY6AFFwxHKjEqFRHH2j4SOfD\n" +
                    "9cVT0yDxd5hm354xS2S8DA7rGJtHf579rJyJlEcM0+k7C8JmL9/Hq5a64NkM0wuO\n" +
                    "njzECm71bdvEclZJa+O4ObHOWskOuGNkyFsjkgUxLThY0fLGG4zWbLFCNTR8sx7M\n" +
                    "mFqaAuxm71w0kuoDd6sgInlhiEPLGBQB\n" +
                    "-----END CERTIFICATE-----\n"
            return Buffer()
                .writeUtf8(codepunkRootCertificationAuthority)
                .inputStream()
        }

        // endregion Methods

    }

    // endregion Companion object
}
