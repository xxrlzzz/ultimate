package main.kotlin.bpmn.element

interface Container: BPMNElement {
    fun iter(): Iterator<BPMNElement>

    override fun hasChild() = true
}