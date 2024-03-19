package main.kotlin.bpmn

import main.kotlin.bpmn.element.ProcessElement

class BPMNModel(val name: String = "default") {
    val processes = ArrayList<ProcessElement>()
}