package main.kotlin.converter

import main.kotlin.bpmn.ElementVisitor
import main.kotlin.bpmn.xml.BPMNParser
import main.kotlin.petri.PetriNetParser
import main.kotlin.petri.PetriNetWriter
import java.io.File

fun main() {
    val preFix = "/Users/xxrl/codebase/bpmn_projects/BPMNGen/src/main/resources/"
    val petriNetModel = PetriNetParser().parseApt(File("${preFix}out.apt"))
    val bpmnModel = Petri2BPMNConverter(petriNetModel).generate()
    val bpmnModel2 = BPMNParser().processModel("${preFix}case1.xml")

    println(bpmnModel)

    val visitor = ElementVisitor.defaultVisitor()

    for (process in bpmnModel.processes) {
        visitor.travel(process)
    }

    visitor.reset()

    println(bpmnModel2)

    for (process in bpmnModel2.processes) {
        visitor.travel(process)
    }
}