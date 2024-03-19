package main.kotlin.bpmn.element

class SwimLane(name: String, id: String) : BPMNElementImpl(name, id) {
    private val nodeRefs = HashSet<String>()
    override fun getType() =
        BPMNElement.Type.SwimLanes

    override fun hasChild(): Boolean {
        return false
    }

    fun addNode(node: String) {
        nodeRefs.add(node)
    }

    fun getNodes(): HashSet<String> {
        return nodeRefs
    }

    fun contains(node: String): Boolean {
        return nodeRefs.contains(node)
    }
}