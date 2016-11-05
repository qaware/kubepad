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

import retrofit2.Call
import retrofit2.http.*
import java.util.*
import javax.enterprise.context.ApplicationScoped

/**
 * @see <a href="https://mesosphere.github.io/marathon/docs/generated/api.html">Marathon REST API documentation</a>
 */
@ApplicationScoped
interface MarathonClient {

    @GET("v2/apps?label=LAUNCHPAD_ENABLE==true")
    fun listApps(): Call<Apps>

    @PUT("v2/apps/{app_id}")
    fun updateApp(@Path("app_id") appId: String, @Body app: Any, @Query("force") force: Boolean): Call<UpdateResult>

    @GET("v2/apps/{app_id}/tasks")
    fun listTasksOfApp(@Path("app_id") appId: String): Call<Tasks>

    @GET("v2/groups")
    fun listGroups(): Call<Groups>

    @GET("v2/tasks")
    fun listTasks(@Query("status") status: String = ""): Call<Tasks>

    @GET("v2/deployments")
    fun listDeployments(): Call<List<Deployment>>

    data class App(val id: String, val instances: Int,
                   val deployments: List<Deployment>, val labels: Map<String, String>)

    data class Apps(val apps: List<App>)

    data class Group(val id: String)

    data class Groups(val groups: List<Group>)

    data class ScalingUpdate(val instances: Int)

    data class UpdateResult(val deploymentId: String)

    data class Tasks(val tasks: List<Task>)

    data class Task(val id: String, val appId: String, val stagedAt: Date, val startedAt: Date)

    data class Deployment(val id: String, val affectedApps: List<String>?, val currentActions: List<Action>?)

    data class Action(val action: ActionType, val app: String)

    enum class ActionType {
        StartApplication, StopApplication, ScaleApplication, RestartApplication, ResolveArtifacts
    }
}

