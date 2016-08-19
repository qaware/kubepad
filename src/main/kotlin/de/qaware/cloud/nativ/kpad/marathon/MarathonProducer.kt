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

import com.google.gson.GsonBuilder
import com.moandjiezana.toml.Toml
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
open class MarathonProducer @Inject constructor(@ConfigProperty(name = "dcos.configPath")
                                                private val configPath: String?) {

    @Produces
    @Default
    open fun marathonClient(): MarathonClient {
        val dcosConfigFile = getDcosConfigFile()
        val dcosConfig = getDcosConfig(dcosConfigFile)

        val client = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val req = chain.request().newBuilder()
                            .header("Authorization", "token=${dcosConfig.accessToken}")
                            .build()
                    chain.proceed(req)
                }.build()

        val apiPath = if (dcosConfig.url.endsWith("/")) "service/marathon/" else "/service/marathon/"
        val apiEndpoint = dcosConfig.url + apiPath

        val retrofit = Retrofit.Builder()
                .baseUrl(apiEndpoint)
                .addConverterFactory(createGsonConverterFactory())
                .client(client)
                .build()

        return retrofit.create(MarathonClient::class.java)
    }

    private fun getDcosConfig(dcosConfigFile: File): DcosConfig {
        val dcosConfig = Toml().read(dcosConfigFile)
        val url = dcosConfig.getString("core.dcos_url")
        val accessToken = dcosConfig.getString("core.dcos_acs_token")
        return DcosConfig(url, accessToken)
    }

    private fun getDcosConfigFile(): File {
        if (configPath.isNullOrEmpty()) {
            val homeDirectory = System.getProperty("user.home")
            return File(homeDirectory, ".dcos/dcos.toml")
        } else {
            return File(configPath)
        }
    }

    private fun createGsonConverterFactory(): GsonConverterFactory {
        val gson = GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .create()
        return GsonConverterFactory.create(gson)
    }

    private data class DcosConfig(val url: String, val accessToken: String)
}
