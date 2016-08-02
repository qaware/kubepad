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
 * This class handles the Deployments.
 */
@ApplicationScoped
interface ClusterDeployments {

    fun clear()

    fun deployments(): List<Deployment?>

    operator fun get(row: Int): Deployment?

    /**
     * Scale the Kubernetes deployment to a number of given replicas.
     *
     * @param index the deployment index on the Launchpad
     * @param replicas the number of replicas
     */
    fun scale(index: Int, replicas: Int)
}
