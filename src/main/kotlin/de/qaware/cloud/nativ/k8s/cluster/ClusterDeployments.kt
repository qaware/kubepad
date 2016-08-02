package de.qaware.cloud.nativ.k8s.cluster

import io.fabric8.kubernetes.api.model.extensions.Deployment
import javax.enterprise.context.ApplicationScoped

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
