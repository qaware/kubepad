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
import io.fabric8.kubernetes.api.model.Pod
import io.fabric8.kubernetes.client.KubernetesClient
import org.apache.deltaspike.core.api.config.ConfigProperty
import org.slf4j.Logger
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * This class handles the Pods on Kubernetes.
 */
@ApplicationScoped
open class KubernetesPods @Inject constructor(private val client: KubernetesClient,
                                              @ConfigProperty(name = "kubernetes.namespace")
                                              private val namespace: String,
                                              private val logger: Logger) {

    /**
     * Basic query method for all pods matching the given labels.
     *
     * @param labels the labels to query
     * @return the list of pods
     */
    open fun all(labels: MutableMap<String, String>): List<Pod> {
        logger.debug("Get all pods with labels {}.", labels)
        return client.pods().inNamespace(namespace).withLabels(labels).list()?.items ?: emptyList()
    }

    companion object {
        fun name(pod: Pod) = KubernetesHelper.getName(pod)
        fun phase(pod: Pod) = ClusterNode.Phase.valueOf(pod.status.phase)
    }

}