package main.kotlin.bpmn.element

interface FlowElement : BPMNElement {
    fun prevElements(): List<String>
    fun nextElements(): List<String>
    fun connPrev(prevElement: String)
    fun connNext(nextElement: String)

    fun removePrev(prevElement: String)

    fun removeNext(nextElement: String)

    fun clearSibling(processElement: SubProcessElement)
}

abstract class SiSoFlowElement(name: String, id: String) : FlowElement, BPMNElementImpl(name, id) {
    private var prevElement: String? = null
    private var nextElement: String? = null

    override fun prevElements(): List<String> {
        if (prevElement == null) {
            return listOf()
        }
        return listOf(prevElement!!)
    }

    override fun nextElements(): List<String> {
        if (nextElement == null) {
            return listOf()
        }
        return listOf(nextElement!!)
    }

    override fun connPrev(prevElement: String) {
        this.prevElement = prevElement
    }

    override fun connNext(nextElement: String) {
        this.nextElement = nextElement
    }

    override fun removeNext(nextElement: String) {
        this.nextElement = null
    }

    override fun removePrev(prevElement: String) {
        this.prevElement = null
    }

    override fun clearSibling(processElement: SubProcessElement) {
        nextElement?.let {
            val element = processElement.getElement(it)
            if (element is FlowElement) {
                element.removePrev(this.getIdV())
            }
        }
        prevElement?.let {
            val element = processElement.getElement(it)
            if (element is FlowElement) {
                element.removePrev(this.getIdV())
            }
        }
    }
}


abstract class MiMoFlowElement(name: String, id: String) : FlowElement, BPMNElementImpl(name, id) {
    private var prevElements: ArrayList<String> = ArrayList()
    private var nextElements: ArrayList<String> = ArrayList()

    override fun prevElements() = prevElements

    override fun nextElements() = nextElements

    override fun connPrev(prevElement: String) {
        this.prevElements.add(prevElement)
    }

    override fun connNext(nextElement: String) {
        this.nextElements.add(nextElement)
    }

    override fun removeNext(nextElement: String) {
        this.nextElements.add(nextElement)
    }

    override fun removePrev(prevElement: String) {
        this.prevElements.remove(prevElement)
    }

    override fun clearSibling(processElement: SubProcessElement) {
        for (nextE in this.nextElements) {
            val element = processElement.getElement(nextE)
            if (element is FlowElement) {
                element.removePrev(this.getIdV())
            }
        }
        for (prevE in this.prevElements) {
            val element = processElement.getElement(prevE)
            if (element is FlowElement) {
                element.removeNext(this.getIdV())
            }
        }
    }
}
