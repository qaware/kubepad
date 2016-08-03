package de.qaware.cloud.nativ.kpad

interface ClusterAppReplica {
    fun phase() : ClusterNode.Phase
    fun name() : String
}