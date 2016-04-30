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
package de.qaware.cloud.nativ.k8s

import io.fabric8.kubernetes.api.KubernetesHelper
import io.fabric8.kubernetes.api.model.extensions.Deployment
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.KubernetesClientException
import io.fabric8.kubernetes.client.Watcher
import org.apache.deltaspike.core.api.config.ConfigProperty
import org.slf4j.Logger
import javax.annotation.PostConstruct
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.event.Event
import javax.inject.Inject

/**
 * This class handles the Deployments on Kubernetes.
 */
@ApplicationScoped
open class KubernetesDeployments @Inject constructor(private val client: KubernetesClient,
                                                     @ConfigProperty(name = "kubernetes.namespace")
                                                     private val namespace: String,
                                                     private val events: Event<ClusterDeploymentEvent>,
                                                     private val logger: Logger) : Watcher<Deployment> {

    private val deployments = mutableListOf<Deployment?>()
    private val names = mutableListOf<String?>()

    @PostConstruct
    open fun init() {
        val operation = client.extensions().deployments().inNamespace(namespace)
        val list = operation.list()
        list?.items?.forEach {
            val name = KubernetesHelper.getName(it)
            logger.debug("Found Kubernetes deployment {} -> {}", name, it)
            deployments.add(it)
            names.add(name)
        }

        operation.watch(this)
    }

    open fun clear() {
        deployments.clear()
        names.clear()
    }

    open fun deployments(): List<Deployment?> = deployments.toList()

    open operator fun get(row: Int): Deployment? = deployments[row]

    /**
     * Scale the Kubernetes deployment to a number of given replicas.
     *
     * @param index the deployment index on the Launchpad
     * @param replicas the number of replicas
     */
    open fun scale(index: Int, replicas: Int) {
        if (index > deployments.size) return

        var deployment = deployments[index]
        val name = KubernetesHelper.getName(deployment)

        logger.debug("Scaling deployment {} to {} replicas.",
                KubernetesHelper.getName(deployment), replicas)

        synchronized(client) {
            deployments[index] = client.extensions().deployments().inNamespace(namespace).withName(name)
                    .edit().editSpec()
                    .withReplicas(replicas)
                    .endSpec().done()
        }
    }

    override fun eventReceived(action: Watcher.Action?, resource: Deployment?) {
        when (action) {
            Watcher.Action.ADDED -> {
                val name = KubernetesHelper.getName(resource)
                logger.debug("Added deployment {}.", name)

                val index = deployments.indexOfFirst { it == null }
                if (index == -1) {
                    deployments.add(resource!!)
                    names.add(name)
                } else {
                    deployments[index] = resource
                    names[index] = name
                }

                names.add(KubernetesHelper.getName(resource))

                events.fire(ClusterDeploymentEvent(deployments.indexOf(resource),
                        resource!!.spec.replicas, labels(resource), ClusterDeploymentEvent.Type.ADDED))
            }
            Watcher.Action.MODIFIED -> {
                val name = KubernetesHelper.getName(resource)
                val index = names.indexOf(name)
                val previous = deployments.set(index, resource)

                // now check if the number of replicas has changed
                val oldReplicas = previous?.spec?.replicas ?: 0
                val newReplicas = resource?.spec?.replicas ?: 0

                if (oldReplicas < newReplicas) {
                    logger.debug("Scaled up deployment {} from {} to {} replicas", name, oldReplicas, newReplicas)
                    events.fire(ClusterDeploymentEvent(index, newReplicas, labels(resource),
                            ClusterDeploymentEvent.Type.SCALED_UP))
                } else if (oldReplicas > newReplicas) {
                    logger.debug("Scaled down deployment {} from {} to {} replicas", name, oldReplicas, newReplicas)
                    events.fire(ClusterDeploymentEvent(index, newReplicas, labels(resource),
                            ClusterDeploymentEvent.Type.SCALED_DOWN))
                }
            }
            Watcher.Action.DELETED -> {
                val name = KubernetesHelper.getName(resource)
                logger.debug("Deleted deployment {}.", name)

                val index = names.indexOf(name)
                deployments[index] = null
                names[index] = null

                events.fire(ClusterDeploymentEvent(index, 0, labels(resource), ClusterDeploymentEvent.Type.DELETED))
            }
            Watcher.Action.ERROR -> {
                // on error start blinking red with the row selection button
            }
        }
    }

    override fun onClose(cause: KubernetesClientException?) {
    }

    companion object {
        /**
         * Helper method to extract the labels from the given Deployment.
         *
         * @param deployment a Kubernetes deployment
         * @return a map of labels
         */
        fun labels(deployment: Deployment?): MutableMap<String, String> = KubernetesHelper.getLabels(deployment)
    }
}