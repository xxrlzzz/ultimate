package test.kotlin.bpmn.xml

import main.kotlin.bpmn.ElementVisitor
import main.kotlin.bpmn.xml.BPMNParser
import main.kotlin.converter.BPMN2PetriConverter
import main.kotlin.petri.PetriNetWriter
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter

class BPMNParserTest {
}

fun main() {
    val preFix = "/Users/xxrl/codebase/bpmn_projects/BPMNGen/src/main/resources/"
    val fileName = "case1.xml"
    val model = BPMNParser().processModel("${preFix}${fileName}")
    val visitor = ElementVisitor.defaultVisitor()

    for (process in model.processes) {
        visitor.travel(process)
    }

    val netModel = BPMN2PetriConverter(model).generate()
    println(netModel)
    val netWriter = PetriNetWriter()
    val outFile = File("${preFix}out.apt")
//    outFile.deleteOnExit()
    outFile.createNewFile()
    outFile.setWritable(true)
    val writer = BufferedWriter(OutputStreamWriter(FileOutputStream(outFile)))
//    val writer = BufferedWriter(OutputStreamWriter(System.out))
    netWriter.writeApt(netModel, writer)
}