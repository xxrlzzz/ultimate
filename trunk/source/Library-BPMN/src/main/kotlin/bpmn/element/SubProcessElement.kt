package main.kotlin.bpmn.element

open class SubProcessElement(name: String, id: String): SiSoFlowElement(name, id), Container {
    val elements: ArrayList<BPMNElement> = arrayListOf()
    private val elementIdMap = hashMapOf<String, BPMNElement>()
    var startEvent: StartEvent<Any>? = null
    private val seqs = arrayListOf<SequenceFlow>()
    override fun iter(): Iterator<BPMNElement> {
        return elements.iterator()
    }

    override fun getType(): BPMNElement.Type {
        return BPMNElement.Type.SubProcesses
    }

    fun idList2Name(ids: List<String>) :List<String> {
        return ids.map {
            elementIdMap[it]?.getNameV() ?: ""
        }
    }

    fun addElement(prev: String, element: FlowElement) {
        if (elementIdMap.contains(element.getIdV())) {
            return
        }
        elementIdMap[element.getIdV()] = element
        elements.add(element)

        (elementIdMap[prev] as FlowElement).connNext(element.getIdV())
        element.connPrev(prev)
    }

    fun addElement(element: FlowElement) {
        if (elementIdMap.contains(element.getIdV())) {
            return
        }
        elementIdMap[element.getIdV()] = element
        elements.add(element)

        if (element is StartEvent<*>) {
            startEvent = element as StartEvent<Any>
        }
    }

    fun deleteElement(elementId: String) {
        if (!elementIdMap.contains(elementId)) {
            return
        }
        val element = elementIdMap.get(elementId)
        elementIdMap.remove(elementId)
        elements.remove(element)
        if (element is FlowElement) {
            element.clearSibling(this)
        }
    }

    fun addSequenceFlow(name: String?, id: String, target: String, source: String) {
        seqs.add(SequenceFlow(name, id, target, source))
    }

    fun processFlow() {
        for (flow in seqs) {
            val source = elementIdMap[flow.source]
            val target = elementIdMap[flow.target]
            if (source != null && target != null) {
                (source as FlowElement).connNext(flow.target)
                (target as FlowElement).connPrev(flow.source)
            }
        }
    }

    fun getElement(id: String): BPMNElement? {
        return elementIdMap[id]
    }

    open fun isCrossLine(from: String, to: String): Boolean {
        return false
    }

    open fun getLine(ele: String): String? {
        return null
    }
}

data class SequenceFlow(val name: String?, val id: String, val target: String, val source: String)

class ProcessElement(name: String, id: String): SubProcessElement(name, id) {
    var laneSet: List<SwimLane>? = null
    var nodeLaneRef: MutableMap<String, String>? = null
    private fun processRef() {
        if (nodeLaneRef != null) {
            return
        }
        nodeLaneRef = HashMap()
        if (laneSet == null) {
            return
        }
        for (line in laneSet!!) {
            for (node in line.getNodes()) {
                (nodeLaneRef as HashMap<String, String>)[node] = line.getNameV()
            }
        }
    }
    override fun isCrossLine(from: String, to: String): Boolean {
        processRef()
        val fromLine = nodeLaneRef!![from]
        val toLine = nodeLaneRef!![to]
        return fromLine != toLine
    }

    override fun getLine(ele: String): String? {
        processRef()
        return nodeLaneRef?.get(ele)
    }
}