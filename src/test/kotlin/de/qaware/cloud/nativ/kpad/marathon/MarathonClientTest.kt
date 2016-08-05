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

import com.github.tomakehurst.wiremock.junit.WireMockRule
import org.junit.Rule
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.test.assertEquals

class MarathonClientTest {
    val port = 8089

    val retrofit = Retrofit.Builder()
            .baseUrl("http://localhost:$port")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    val client = retrofit.create(MarathonClient::class.java)

    val testApp = MarathonClient.App(
            id = "testapp",
            cmd = "testcmd",
            instances = 3,
            deployments = listOf(
                    MarathonClient.Deployment(
                            id = "testapp-d0",
                            affectedApps = listOf("testapp"),
                            currentActions = listOf(
                                    MarathonClient.Action(
                                            action = MarathonClient.ActionType.ScaleApplication,
                                            app = "testapp"
                                    )
                            )
                    )
            ),
            labels = mapOf(Pair("key", "value"))
    )

    @Rule @JvmField
    val wireMockRule = WireMockRule(port)

    @Test
    fun listAppsTest() {
        val apps = client.listApps().execute().body().apps
        apps.forEach { println("Found Marathon app with id ${it.id}") }

        val deployments = client.listDeployments().execute().body()
        deployments.forEach { println("Found Marathon deployment with id ${it.id}") }

        val appsWithDeployments = apps.map { app -> app.copy(deployments = deployments.filter {
            dep -> app.deployments.any { it.id.equals(dep.id) } }) }

        for (app in appsWithDeployments) {
            println(app.toString())
        }

        assertEquals(appsWithDeployments.first(), testApp,
                "The parsed data received from the testing api does not match the test object")
    }

    @Test
    fun updateAppTest() {
        println("Scaling app ${testApp.id} to 4 instances")
        val result = client.updateApp(testApp.id, MarathonClient.ScalingUpdate(4)).execute().body()
        val deployment = client.listDeployments().execute().body().findLast { it.id.equals(result.deploymentId) }
        println("Scaling successful. Created depolyment ${deployment}")

        assertEquals(deployment, testApp.deployments.first(),
                "The parsed data received from the testing api does not match the test object")
    }
}
