package main.kotlin.petri

import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.io.Writer

class PetriNetWriter {
    fun writeApt(model: PetriNetModel, writer: Writer) {
        writer.write(".name \"${model.name}\"\n")
        writer.write(".type PN\n")
        writer.write("\n")
        writer.write(".places\n")
        for (place in model.places) {
            writer.write("$place\n")
        }
        writer.write("\n")
        writer.write(".transitions\n")
        for (tran in model.transitions) {
            writer.write("$tran\n")
        }
        writer.write("\n")
        writer.write(".flows\n")
        for (flow in model.flows.values) {
            var inStr = ""
            for (inComing in flow.inComing) {
                inStr = "$inStr, ${inComing.second}*${inComing.first}"
            }
            if (inStr.isNotEmpty()) {
                inStr = inStr.substring(2)
            }
            var outStr = ""
            for (outComing in flow.outComing) {
                outStr = "$outStr, ${outComing.second}*${outComing.first}"
            }
            if (outStr.isNotEmpty()) {
                outStr = outStr.substring(2)
            }
            writer.write("${flow.transition}: {$inStr} -> {$outStr}\n")
        }
        writer.write("\n")
        var markingStr = ""
        for (mark in model.initMarking) {
            markingStr = "$markingStr, ${mark.value}*${mark.key}"
        }
        writer.write(".initial_marking {$markingStr}\n")
        writer.flush()
        writer.close()
    }
}