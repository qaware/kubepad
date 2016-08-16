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

import de.qaware.cloud.nativ.kpad.Cluster
import de.qaware.cloud.nativ.kpad.ClusterAppEvent
import org.apache.deltaspike.core.api.exclude.Exclude
import org.slf4j.Logger
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.event.Event
import javax.inject.Inject
import javax.inject.Named

/**
 * The DC/OS Marathon specific cluster implementation.
 */
@Exclude(onExpression = "cluster.service!=marathon")
@ApplicationScoped
open class MarathonCluster @Inject constructor(private val client: MarathonClient,
                                               @Named("scheduled")
                                               private val scheduler: ScheduledExecutorService,
                                               private val events: Event<ClusterAppEvent>,
                                               private val logger: Logger) : Cluster {

    private val apps = Array<MarathonClient.App?>(8, { i -> null })
    private val deploying = Array<Boolean>(8, { i -> false })

    @PostConstruct
    open fun init() {
        watch()
    }

    private fun update() {
        val newApps = client.listApps().execute().body().apps.toMutableList()

        apps.forEachIndexed { i, app ->
            if (app == null) return@forEachIndexed

            val newApp = newApps.find { it.id.equals(app.id) }

            if (newApp != null) { // app found in newApps
                newApps.remove(newApp)
                apps[i] = newApp

                if (deploying[i] && (newApp.deployments.count() == 0)) { // -> status changed
                    logger.debug("App {} finished deploying", app.id)
                    deploying[i] = false
                    events.fire(ClusterAppEvent(i, newApp.instances, labels(i), ClusterAppEvent.Type.DEPLOYED))
                }

                if (app.instances < newApp.instances) { // -> scaled up
                    logger.debug("Scaled up app {} from {} to {} replicas.", app.id, app.instances, newApp.instances)
                    deploying[i] = true
                    events.fire(ClusterAppEvent(i, newApp.instances, labels(i), ClusterAppEvent.Type.SCALED_UP))
                } else if (app.instances > newApp.instances) { // -> scaled down
                    logger.debug("Scaled down app {} from {} to {} replicas.", app.id, app.instances, newApp.instances)
                    deploying[i] = true
                    events.fire(ClusterAppEvent(i, newApp.instances, labels(i), ClusterAppEvent.Type.SCALED_DOWN))
                }
            } else { // app not found in newApps -> deleted
                logger.debug("Deleted app {}.", apps[i]!!.id)
                apps[i] = null
                events.fire(ClusterAppEvent(i, 0, labels(i), ClusterAppEvent.Type.DELETED))
            }
        }

        newApps.forEachIndexed { i, newApp -> // newApp not (yet) in apps -> added
            var index = apps.indexOfFirst { it == null }

            if (index == -1) {
                logger.debug("Found new app {} but could not add because all rows are occupied!", newApp.id)
                return@forEachIndexed
            }

            if (newApp.labels.containsKey("LAUNCHPAD_ROW")) {
                val row = newApp.labels["LAUNCHPAD_ROW"]!!.toInt()
                if (apps.indices.contains(row) && apps[row] == null) {
                    index = row
                }
            }

            apps[index] = newApp
            deploying[index] = false
            logger.debug("Added app {} at index {}.", newApp.id, index)
            events.fire(ClusterAppEvent(index, newApp.instances, labels(index), ClusterAppEvent.Type.ADDED))
        }
    }

    override fun appExists(appIndex: Int): Boolean = apps.indices.contains(appIndex) && apps[appIndex] != null

    override fun replicas(appIndex: Int): Int = apps[appIndex]?.instances ?: -1

    override fun scale(appIndex: Int, replicas: Int) {
        val app = apps[appIndex]
        if (app == null) {
            logger.error("Scaling failed! No app at index {}.", appIndex)
            return
        }

        logger.debug("Scaling app {} to {} replicas.", app.id, replicas)
        val result = client.updateApp(app.id, MarathonClient.ScalingUpdate(replicas)).execute()

        apps[appIndex] = app.copy(instances = replicas)
        deploying[appIndex] = true

        if (result.isSuccessful) {
            logger.debug("Scaling successful.")
        } else {
            logger.error("Scaling failed. ERROR: {}", result.errorBody().string())
        }
    }

    override fun labels(appIndex: Int): Map<String, String> {
        return apps[appIndex]?.labels ?: emptyMap()
    }

    private fun watch() {
        scheduler.scheduleAtFixedRate({
            try {
                update()
            } catch (e: Exception) {
                logger.error("Failed to update data!")
                e.printStackTrace()
            }
        }, 0, 2500, TimeUnit.MILLISECONDS)
    }
}