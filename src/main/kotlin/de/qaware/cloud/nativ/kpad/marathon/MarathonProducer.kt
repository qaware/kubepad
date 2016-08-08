/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 QAware GmbH, Munich, Germany
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package de.qaware.cloud.nativ.kpad.marathon

import com.google.common.io.Files
import okhttp3.OkHttpClient
import org.apache.deltaspike.core.api.config.ConfigProperty
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.inject.Default
import javax.enterprise.inject.Produces
import javax.inject.Inject

/**
 * The CDI producer for the Marathon Java API.
 */
@ApplicationScoped
open class MarathonProducer @Inject constructor(@ConfigProperty(name = "marathon.apiEndpoint")
                                                private val apiEndpoint: String,
                                                @ConfigProperty(name = "marathon.accessTokenFile")
                                                private val accessTokenFile: String) {

    /*
    var wireMockServer : WireMockServer? = null

    @PostConstruct
    fun init() {
        wireMockServer = WireMockServer()
        wireMockServer!!.start()
    }

    @PreDestroy
    fun stop() {
        wireMockServer!!.stop()
    }
    */

    @Produces
    @Default
    open fun marathonClient() : MarathonClient {
        val accessToken = Files.toString(File(accessTokenFile), Charsets.UTF_8)

        val client = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val req = chain.request().newBuilder()
                            .header("Authorization", "token=$accessToken")
                            .build()
                    chain.proceed(req)
                }.build()

        val retrofit = Retrofit.Builder()
                .baseUrl(apiEndpoint)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()

        return retrofit.create(MarathonClient::class.java)
    }

}
