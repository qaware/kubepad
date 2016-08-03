package de.qaware.cloud.nativ.kpad

interface Cluster {

    /**
     * @return the size of the list of apps. This number may be lager than the actual number of currently deployed apps,
     * as there is not an app at every index.
     * @see Cluster.appExists()
     */
    fun appCount() : Int

    /**
     * Check if there is an app deployed at the given index
     *
     * @param appIndex the index (row) to be checked
     * @return true if an app is deployed at the given index
     */
    fun appExists(appIndex : Int) : Boolean

    /**
     * Retrieves the list of all replicas of the app at the given index. Each of the replicas is represented by one
     * square on the launchpad.
     */
    fun replicas(appIndex : Int) : List<ClusterAppReplica>

    /**
     * Scale the app at the given index to a number of given replicas.
     *
     * @param index the deployment index on the Launchpad
     * @param replicas the number of replicas
     */
    fun scale(appIndex : Int, replicas : Int)

    /**
     * Clears the local list of apps.
     */
    fun clear()
}
