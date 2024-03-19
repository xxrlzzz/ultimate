package main.kotlin.bpmn.xml

import main.kotlin.converter.BPMN2PetriConverter
import main.kotlin.bpmn.*
import main.kotlin.bpmn.Constant.simplifyName
import main.kotlin.bpmn.builder.DescriptorBuilder
import main.kotlin.bpmn.element.*
import main.kotlin.petri.PetriNetWriter
import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.helpers.DefaultHandler
import org.xml.sax.helpers.XMLReaderFactory
import java.io.*

class BPMNParser {
    val model = BPMNModel()
    var processEle = ProcessElement("process", "id")
    val processStack: ArrayList<SubProcessElement> = arrayListOf()
    var currentProcess: SubProcessElement = processEle
    var laneSet = ArrayList<SwimLane>()

    val skipElement = listOf(Constant.TAG_LANE_SET, Constant.TAG_FLOW_NODE_REF)

    var id = 0
    fun nextId(): Int {
        id++
        return id
    }

    fun reset() {
        processEle = ProcessElement("process", "id")
        processStack.clear()
        currentProcess = processEle
        laneSet = ArrayList()
    }

    fun createEle(eleName: String, id: String, name: String): BPMNElement {
        return DescriptorBuilder(BPMNElementDescriptor(name, eleName, id)).build()
    }

    fun isElement(eleName: String): Boolean {
        return eleName in elementSet
    }

    private val elementSet = setOf(
        Constant.TAG_TASK,
        Constant.TAG_USER_TASK,
        Constant.TAG_START_EVENT,
        Constant.TAG_END_EVENT,
        Constant.TAG_INTERMEDIATE_THROW_EVENT,
        Constant.TAG_INTERMEDIATE_CATCH_EVENT,
        Constant.TAG_SUBPROCESS,
        Constant.TAG_EXCLUSIVE_GATEWAY,
        Constant.TAG_INCLUSIVE_GATEWAY,
        Constant.TAG_COMPLEX_GATEWAY,
        Constant.TAG_DATA_STORE
    )

    private val handler = object : DefaultHandler() {
        val elementList = arrayListOf("placeHolder")
        var currentEleName = ""
        var currentElement: BPMNElement = processEle
        val statusBit = StatusBit()
        var cLane = SwimLane("name", "id")
        override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?) {
            if (currentEleName.isNotEmpty()) {
                elementList.add(currentEleName)
            }
            currentEleName = localName ?: "dE_"

            val id = attributes?.getValue("id") ?: "node_${nextId()}"
            var name = attributes?.getValue("name")
            if (name.isNullOrEmpty()) {
                name = "d_${simplifyName(currentEleName)}"
            } else {
                name = "${name}_${simplifyName(currentEleName)}"
            }
            name = if (id.contains("_")) {
                val idSuffix = id.split("_")[1]
                "${name}_${idSuffix}"
            } else {
                "${name}_${nextId()}"
            }

            when (currentEleName) {
                Constant.TAG_TASK,
                Constant.TAG_USER_TASK,
                Constant.TAG_START_EVENT,
                Constant.TAG_END_EVENT,
                Constant.TAG_INTERMEDIATE_THROW_EVENT,
                Constant.TAG_INTERMEDIATE_CATCH_EVENT,
                Constant.TAG_SUBPROCESS,
                Constant.TAG_EXCLUSIVE_GATEWAY,
                Constant.TAG_INCLUSIVE_GATEWAY,
                Constant.TAG_COMPLEX_GATEWAY,
                Constant.TAG_DATA_STORE,
                -> {
                    // is element
                    currentElement = createEle(currentEleName, id, name)
                    currentProcess.addElement(currentElement as FlowElement)
                }

                Constant.TAG_PROCESS -> {
                    processEle = ProcessElement(name, id)
                    currentProcess = processEle
                    statusBit.setProcess()
                }

                Constant.TAG_SEQUENCE_FLOW -> {
                    val target = attributes?.getValue("targetRef") ?: ""
                    val source = attributes?.getValue("sourceRef") ?: ""
                    currentProcess.addSequenceFlow(name, id, target, source)
                }

                Constant.TAG_LANE -> {
                    cLane = SwimLane(name, id)
                    statusBit.setLine()
                }

                Constant.TAG_DATA_INPUT -> {
                    statusBit.setDataInput()
                }

                Constant.TAG_DATA_OUTPUT -> {
                    statusBit.setDataOutput()
                }

                Constant.TAG_LANE_SET, Constant.TAG_FLOW_NODE_REF -> {
                    return
                }
            }

            // 更新 process stack
            if (currentEleName == Constant.TAG_SUBPROCESS) {
                processStack.add(currentProcess)
                currentProcess = currentElement as SubProcessElement
            }
            if (attributes == null) {
                return
            }
//                println("ele:$currentEleName")
//                for (i in 0..<attributes.length) {
//                    println("${attributes.getLocalName(i)} ${attributes.getValue(i)}")
//                }
        }

        override fun endElement(uri: String?, localName: String?, qName: String?) {
            val oldElementName = currentEleName
            // process/subprocess 连接元素
            when (oldElementName) {
                Constant.TAG_PROCESS -> {
                    currentProcess.processFlow()
                    statusBit.resetProcess()
                    if (laneSet.size > 0) {
                        processEle.laneSet = laneSet
                    }
                    model.processes.add(processEle)
                    reset()
                }

                Constant.TAG_SUBPROCESS -> {
                    currentProcess.processFlow()
                }

                Constant.TAG_LANE -> {
                    laneSet.add(cLane)
                    cLane = SwimLane("name", "id")
                    statusBit.resetLine()
                }

                Constant.TAG_DATA_INPUT -> {
                    statusBit.resetDataInput()
                }

                Constant.TAG_DATA_OUTPUT -> {
                    statusBit.resetDataOutput()
                }
            }

            currentEleName = elementList.last()
            elementList.removeLast()
            // 更新 process stack
            if (oldElementName == Constant.TAG_SUBPROCESS) {
                currentProcess = processStack.last()
                processStack.removeLast()
            }
        }

        override fun characters(ch: CharArray?, start: Int, length: Int) {
            if (ch != null) {
                val s = String(ch, start, length)
                if (s.isNotEmpty() && s.isNotBlank()) {
//                    println(s)
                    if (statusBit.containLine()) {
                        cLane.addNode(s)
                    } else if (statusBit.containDataInput() && s.contains("DataStore")) {
                        (currentElement as TaskElement).addDataInput(s)
                    } else if (statusBit.containDataOutput()) {
                        (currentElement as TaskElement).addDataOutput(s)
                    }
                }
            }
            super.characters(ch, start, length)
        }
    }


    fun openStream(fileName: String) {
        val file = File(fileName)
        val reader = XMLReaderFactory.createXMLReader()
        reader.contentHandler = handler
        reader.parse(InputSource(FileInputStream(file)))
    }

    fun processModel(file: String): BPMNModel {
        openStream(file)
        return model
    }
}

class StatusBit {
    var cur = 0
    fun setProcess() {
        cur = cur.or(1)
    }

    fun resetProcess() {
        if (containProcess()) {
            cur = cur.xor(1)
        }
    }

    fun containProcess() = cur.and(1) != 0

    fun setLine() {
        cur = cur.or(2)
    }

    fun resetLine() {
        if (containLine()) {
            cur = cur.xor(2)
        }
    }

    fun containLine() = cur.and(2) != 0

    fun setDataInput() {
        cur = cur.or(4)
    }

    fun resetDataInput() {
        if (containDataInput()) {
            cur = cur.xor(4)
        }
    }

    fun containDataInput() = cur.and(4) != 0


    fun setDataOutput() {
        cur = cur.or(8)
    }

    fun resetDataOutput() {
        if (containDataOutput()) {
            cur = cur.xor(8)
        }
    }

    fun containDataOutput() = cur.and(8) != 0
}
