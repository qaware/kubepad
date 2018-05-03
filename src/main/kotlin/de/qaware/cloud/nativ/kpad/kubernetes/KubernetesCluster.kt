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
package de.qaware.cloud.nativ.kpad.kubernetes

import de.qaware.cloud.nativ.kpad.Cluster
import de.qaware.cloud.nativ.kpad.ClusterAppEvent
import io.fabric8.kubernetes.api.KubernetesHelper
import io.fabric8.kubernetes.api.model.extensions.Deployment
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.KubernetesClientException
import io.fabric8.kubernetes.client.Watcher
import org.apache.deltaspike.core.api.config.ConfigProperty
import org.apache.deltaspike.core.api.exclude.Exclude
import org.slf4j.Logger
import javax.annotation.PostConstruct
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.event.Event
import javax.inject.Inject

/**
 * This class handles the Deployments on Kubernetes.
 */
@Exclude(onExpression = "cluster.service!=kubernetes")
@ApplicationScoped
open class KubernetesCluster @Inject constructor(private val client: KubernetesClient,
                                                 @ConfigProperty(name = "kubernetes.namespace")
                                                 private val namespace: String,
                                                 private val events: Event<ClusterAppEvent>,
                                                 private val logger: Logger) : Watcher<Deployment>, Cluster {

    private val deployments = Array<Deployment?>(8, { i -> null })
    private val names = Array<String?>(8, { i -> null })
    private var disableEvents = true

    @PostConstruct
    open fun init() {
        logger.info("Connect to Kubernetes master {}.", client.masterUrl)
        val operation = client.extensions().deployments().inNamespace(namespace)
        val list = operation.list()
        list?.items?.forEach {
            addDepolyment(it)
        }

        // does not work with GCE (No HTTP 101)
        operation.watch(this)

        disableEvents = false
    }

    override fun appExists(appIndex: Int) = deployments.indices.contains(appIndex) && deployments[appIndex] != null

    override fun labels(appIndex: Int): Map<String, String> {
        return labels(deployments[appIndex])
    }

    private fun labels(deployment: Deployment?): Map<String, String> {
        return KubernetesHelper.getLabels(deployment)
    }

    override fun replicas(appIndex: Int): Int {
        return deployments[appIndex]?.spec?.replicas ?: -1
    }

    private fun addDepolyment(deployment: Deployment) {
        val name = KubernetesHelper.getName(deployment)
        var index = deployments.indexOfFirst { it == null }

        if (names.contains(name)) {
            logger.info("Deployment with name {} already added. Ignored.", name)
            return
        }

        if (index == -1) {
            logger.info("Found new deployment {} but could not add because all rows are occupied.", name)
            return
        }

        val labels = labels(deployment)

        if (!"true".equals(labels["LAUNCHPAD_ENABLE"], true)) {
            return
        }

        if (labels.containsKey("LAUNCHPAD_ROW")) {
            val row = labels["LAUNCHPAD_ROW"]!!.toInt()
            if (deployments.indices.contains(row) && deployments[row] == null) {
                index = row
            }
        }

        deployments[index] = deployment
        names[index] = name
        logger.info("Added depolyment {} at index {}.", name, index)
        events.fire(ClusterAppEvent(index, replicas(index), labels(deployment), ClusterAppEvent.Type.ADDED))
    }

    /**
     * Scale the Kubernetes deployment to a number of given replicas.
     *
     * @param appIndex the deployment index on the Launchpad
     * @param replicas the number of replicas
     */
    override fun scale(appIndex: Int, replicas: Int) {
        if (appIndex > deployments.size) return

        val deployment = deployments[appIndex]
        val name = KubernetesHelper.getName(deployment)

        logger.info("Scaling deployment {} to {} replicas.",
                KubernetesHelper.getName(deployment), replicas)

        synchronized(client) {
            deployments[appIndex] = client.extensions().deployments().inNamespace(namespace).withName(name)
                    .edit().editSpec()
                    .withReplicas(replicas)
                    .endSpec().done()
        }

        events.fire(ClusterAppEvent(appIndex, replicas, labels(deployment), ClusterAppEvent.Type.DEPLOYED))
    }

    override fun reset() {
        disableEvents = true
        try {
            0.until(8).forEach {
                deployments[it] = null
                names[it] = null
            }

            val operation = client.extensions().deployments().inNamespace(namespace)
            val list = operation.list()
            list?.items?.forEach {
                addDepolyment(it)
            }
        } finally {
            disableEvents = false
        }

    }

    override fun eventReceived(action: Watcher.Action?, resource: Deployment?) {
        if (disableEvents) {
            logger.info("Event {} not processed as globally disabled. Resource={}", action, resource)
            return
        }

        when (action) {
            Watcher.Action.ADDED -> {
                addDepolyment(resource!!)
            }

            Watcher.Action.MODIFIED -> {
                val name = KubernetesHelper.getName(resource)
                val index = names.indexOf(name)
                val previous = deployments[index]
                deployments[index] = resource

                // now check if the number of replicas has changed
                val oldReplicas = previous?.spec?.replicas ?: 0
                val newReplicas = resource?.spec?.replicas ?: 0

                if (oldReplicas < newReplicas) {
                    logger.info("Scaled up deployment {} from {} to {} replicas", name, oldReplicas, newReplicas)
                    events.fire(ClusterAppEvent(index, newReplicas, labels(resource), ClusterAppEvent.Type.SCALED_UP))
                } else if (oldReplicas > newReplicas) {
                    logger.info("Scaled down deployment {} from {} to {} replicas", name, oldReplicas, newReplicas)
                    events.fire(ClusterAppEvent(index, newReplicas, labels(resource), ClusterAppEvent.Type.SCALED_DOWN))
                }
            }

            Watcher.Action.DELETED -> {
                val name = KubernetesHelper.getName(resource)
                logger.info("Deleted deployment {}.", name)

                val index = names.indexOf(name)
                deployments[index] = null
                names[index] = null

                events.fire(ClusterAppEvent(index, 0, labels(resource), ClusterAppEvent.Type.DELETED))
            }

            Watcher.Action.ERROR -> {
                // on error start blinking red with the row selection button
            }
        }
    }

    override fun onClose(cause: KubernetesClientException?) {
    }
}