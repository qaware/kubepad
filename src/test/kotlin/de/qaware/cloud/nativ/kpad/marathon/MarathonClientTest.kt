package de.qaware.cloud.nativ.kpad.marathon

import com.github.tomakehurst.wiremock.junit.WireMockRule
import org.junit.Rule
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.test.assertEquals

class MarathonClientTest {

    val retrofit = Retrofit.Builder()
            .baseUrl("http://localhost:8089")
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
            )
    )

    @Rule @JvmField
    val wireMockRule = WireMockRule(8089)

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
