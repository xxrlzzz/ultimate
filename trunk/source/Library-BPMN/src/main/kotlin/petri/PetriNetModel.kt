package main.kotlin.petri


class PetriNetModel(val name: String) {
    var places: LinkedHashSet<String> = LinkedHashSet()
    var transitions: LinkedHashSet<String> = LinkedHashSet()
    var initMarking: Map<String, Int> = HashMap()
    var flows: LinkedHashMap<String, Flow> = LinkedHashMap()
    var description: String? = null

    fun addInComing(transition: String, node: Pair<String, Int>) {
        if (!flows.contains(transition)) {
            flows[transition] = Flow(transition)
        }
        flows[transition]?.inComing?.add(node)
    }

    fun addOutComing(transition: String, node: Pair<String, Int>) {
        if (!flows.contains(transition)) {
            flows[transition] = Flow(transition)
        }
        flows[transition]?.outComing?.add(node)
    }
}

class Flow(val transition: String) {
    var inComing: MutableList<Pair<String, Int>> = ArrayList()
    var outComing: MutableList<Pair<String, Int>> = ArrayList()
}
