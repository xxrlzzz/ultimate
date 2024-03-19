package main.kotlin.converter

import main.kotlin.bpmn.BPMNElementDescriptor
import main.kotlin.bpmn.Constant
import java.util.regex.Pattern

object CommonUtil {
    val placePattern = Pattern.compile("p\\(([^\\|]*)\\|([^\\)]*)\\)")
    val transPattern = Pattern.compile("t\\(([^\\|]*)\\|([^\\)]*)\\)")
    fun getNodeName(node: String, nxtEle: String? = null, isPlace: Boolean = true): String {
        var tnode = node.replace(" ", "_")
        if (!nxtEle.isNullOrEmpty()) {
            tnode = "($tnode|$nxtEle)"
        }
        return if (isPlace) {
            "p${tnode}"
        } else {
            "t${tnode}"
        }
    }

    fun parseNameId(node: String): BPMNElementDescriptor {
        var res = node
        if (res.endsWith("_")) {
            res = res.substring(0, res.length-1)
        }
        return if (res.contains("_")) {
            val split = res.split("_")
            val name = node
            val elementType = Constant.fullName(split[1])
            val id = "${Constant.elementType(elementType)}_${split[2]}"
            BPMNElementDescriptor(name, elementType, id)
        } else {
            println("unknown node $node")
            BPMNElementDescriptor(res, "1", "1")
        }
    }

}