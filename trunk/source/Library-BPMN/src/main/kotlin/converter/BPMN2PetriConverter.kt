package main.kotlin.converter

import main.kotlin.bpmn.BPMNModel
import main.kotlin.bpmn.ElementVisitor
import main.kotlin.bpmn.element.BPMNElement
import main.kotlin.bpmn.element.FlowElement
import main.kotlin.petri.PetriNetModel

class BPMN2PetriConverter(val model: BPMNModel) {
    private val petriNetModel = PetriNetModel("model")
    private val visitor = ElementVisitor()

    //        val flowMap = HashMap<String, Flow>()
    /**
     * 用于覆盖前向元素
     * 1. 跨泳道
     */
//    private var prePlace: String? = null

    private fun handleCrossLine(line: String, preNode: String, curNode: String) {
        val crossLineTran = "t${line}"
        val crossLinePlace = "p(${preNode}_|$curNode)"
        val prePlace = "p(${preNode}|${curNode})"
        petriNetModel.places.add(crossLinePlace)
        petriNetModel.transitions.add(crossLineTran)
        petriNetModel.addOutComing(crossLineTran, Pair(crossLinePlace, 1))
        petriNetModel.addInComing(crossLineTran, Pair(prePlace, 1))
//        prePlace = crossLinePlace
    }

    /**
     * 不处理前后都多元素的情况
     */
    private fun handleElements(ele: String, preEle: String?, nxtEles: List<String>?) {
        if (nxtEles != null) {
            for (nxtEle in nxtEles) {
                handleElement(ele, preEle, nxtEle)
            }
        } else {
            handleElement(ele, preEle)
        }
    }

    private fun handleElement(ele: String, preEle: String? = null, nxtEle: String? = null) {
        val transName = CommonUtil.getNodeName(ele, nxtEle, false)
        val placeName = CommonUtil.getNodeName(ele, nxtEle)
        petriNetModel.transitions.add(transName)
//        if (!prePlace.isNullOrEmpty()) {
//            petriNetModel.addInComing(transName, Pair(prePlace!!, 1))
//            prePlace = null
//        } else
        if (!preEle.isNullOrEmpty()) {
            petriNetModel.addInComing(transName, Pair(CommonUtil.getNodeName(preEle, ele), 1))
        }
        petriNetModel.addOutComing(transName, Pair(placeName, 1))
        petriNetModel.places.add(placeName)
    }

    fun generate(): PetriNetModel {
        visitor.callback = { it: BPMNElement, ctx: ElementVisitor.Context ->
            // 多个prevElement是由visitor来触发
            var preEle = ctx.prevElement?.getNameV()
            if (ctx.prevElement != null &&
                ctx.process?.isCrossLine(it.getIdV(), ctx.prevElement?.getIdV()!!) == true
            ) {
                val line = ctx.process?.getLine(it.getIdV())
                    ?.replace(" ", "_")
                if (line != null) {
                    handleCrossLine(
                        line,
                        ctx.prevElement?.getNameV()!!,
                        it.getNameV()
                    )
                    preEle?.let {
                        preEle = "${it}_"
                    }
                }
            }
            if (it is FlowElement) {
                handleElements(
                    it.getNameV(), preEle,
                    ctx.process?.idList2Name(it.nextElements())
                )
            } else {
                handleElement(it.getNameV(), preEle)
            }
        }

        visitor.travel(model.processes[0])
        return petriNetModel
    }
}

