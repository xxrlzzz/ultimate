package main.kotlin.bpmn.element

interface BPMNElement {

    fun getType(): Type
    fun hasChild(): Boolean
    fun getIdV(): String
    fun getNameV(): String
    enum class Type {
        Gateways,
        Tasks,
        SubProcesses,
        Events,
        Data,
        Participants,
        SwimLanes,
        Artifacts,
        Unknown
    }
}

abstract class BPMNElementImpl(var name: String, var id: String): BPMNElement {
    override fun getIdV(): String {
        return id
    }

    override fun getNameV(): String {
        return name
    }
}

