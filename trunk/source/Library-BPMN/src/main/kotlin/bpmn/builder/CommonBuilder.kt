package main.kotlin.bpmn.builder

import main.kotlin.bpmn.element.BPMNElement

abstract class CommonBuilder {
    var name = ""
    var id = ""

    open fun name(name: String): CommonBuilder {
        this.name = name
        return this
    }

    open fun id(id: String): CommonBuilder {
        this.id = id
        return this
    }

    abstract fun build(): BPMNElement
}