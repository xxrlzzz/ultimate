package main.kotlin.converter

import main.kotlin.converter.CommonUtil.parseNameId
import main.kotlin.converter.CommonUtil.placePattern
import main.kotlin.bpmn.BPMNModel
import main.kotlin.bpmn.builder.DescriptorBuilder
import main.kotlin.bpmn.element.FlowElement
import main.kotlin.bpmn.element.ProcessElement
import main.kotlin.petri.PetriNetModel

class Petri2BPMNConverter(private val model: PetriNetModel) {
    private val bpmnModel = BPMNModel()
    private val processElement = ProcessElement("process", "1")

    fun generate(): BPMNModel {
        model.places.forEach {
            val matcher = placePattern.matcher(it)
            if (!matcher.find()) {
                return@forEach
            }
            var existSeq = true
            val inComingElementDes = parseNameId(matcher.group(1))
            val outComingElementDes = parseNameId(matcher.group(2))
            val inComingElement = DescriptorBuilder(inComingElementDes).build()
            val outComingElement = DescriptorBuilder(outComingElementDes).build()
            if (inComingElement is FlowElement) {
                processElement.addElement(
                    inComingElement
                )
            } else {
                existSeq = false
                println("inComingElement ${inComingElement.getNameV()} is unknown")
            }
            if (outComingElement is FlowElement) {
                processElement.addElement(outComingElement)
            } else {
                existSeq = false
                println("outComingElement ${inComingElement.getNameV()} is unknown")
            }
            if (existSeq) {
                processElement.addSequenceFlow(null, "x", outComingElement.getIdV(), inComingElement.getIdV())
            }
        }
//        model.flows.forEach {
//            val inComingList = it.value.inComing.map { flow ->
//                parseNameId(flow.first)
//            }
//            val outComingList = it.value.outComing.map { flow ->
//                parseNameId(flow.first)
//            }
//            if (inComingList.isEmpty()) {
//                // start event
//                // assume only one start event
//                assert(outComingList.size == 1) {
//                    "multiple start event is not support"
//                }
//                processElement.deleteElement(outComingList[0].second)
//                processElement.addElement(
//                    EventBuilder().id(outComingList[0].second).name(outComingList[0].first)
//                        .place(EventElement.Place.Start)
//                        .build()
//                )
//            } else {
//                for (inComing in inComingList) {
//                    for (outComing in outComingList) {
//                        processElement.addSequenceFlow("", "id", inComing.second, outComing.second)
//                    }
//                }
//            }
//        }
        processElement.processFlow()
        bpmnModel.processes.add(processElement)
        return bpmnModel
    }
}