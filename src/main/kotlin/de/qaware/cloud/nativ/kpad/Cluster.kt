package de.qaware.cloud.nativ.kpad

interface Cluster {

    fun appCount() : Int

    /**
     * Check if there is an app deployed at the given index
     *
     * @param appIndex the index (row) to be checked
     * @return true if an app is deployed at the given index
     */
    fun appExists(appIndex : Int) : Boolean

    fun replicas(appIndex : Int) : List<ClusterAppReplica>

    fun scale(row : Int, replicas : Int)

    fun clear()
}
