package main.kotlin.petri

import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.regex.Pattern

class PetriNetParser {
    enum class Status {
        ModelName,
        Type,
        Places,
        PlacesContent,
        TransitionsContent,
        FlowsContent,
        InitialMarking,
    }

    fun parseApt(aptFile: File): PetriNetModel {
        var current = Status.ModelName
        var description: String? = null
        val places = LinkedHashSet<String>()
        val trans = LinkedHashSet<String>()
        val flows = LinkedHashMap<String, Flow>()
        var marking: Map<String, Int> = HashMap()
        val bis = BufferedReader(InputStreamReader(FileInputStream(aptFile)))
        var name = "err"
        bis.forEachLine {
            when (current) {
                Status.ModelName -> {
                    if (!it.startsWith(".name")) {
                        println("warn invalid status when parse apt file")
                        name = "error"
                        return@forEachLine
                    }
                    name = it.substringAfter(".name ")
                    name = name.substring(1, name.length - 1)
                    current = Status.Type
                }
                Status.Type -> {
                    if (!it.startsWith(".type") || !it.endsWith("PN")) {
                        println("warn invalid status when parse apt file")
                        name = "error"
                        return@forEachLine
                    }
                    current = Status.Places
                }
                Status.Places -> {
                    if (it.isBlank()) {
                        return@forEachLine
                    }
                    if (it.startsWith(".description")) {
                        description = it.substringAfter(".description ")
                        return@forEachLine
                    }
                    if (!it.startsWith(".places")) {
                        println("warn invalid status when parse apt file")
                        name = "error"
                        return@forEachLine
                    }
                    current = Status.PlacesContent
                }
                Status.PlacesContent -> {
                    // TODO multiple content
                    if (it.isNotBlank()) {
                        if (it.startsWith(".transitions")) {
                            current = Status.TransitionsContent
                        } else if (it.startsWith(".")) {
                            println("warn invalid status when parse apt file")
                            name = "error"
                            return@forEachLine
                        } else {
                            places.add(it)
                        }
                    }
                }
                Status.TransitionsContent -> {
                    if (it.isNotBlank()) {
                        if (it.startsWith(".flows")) {
                            current = Status.FlowsContent
                        } else if (it.startsWith(".")) {
                            println("warn invalid status when parse apt file")
                            name = "error"
                            return@forEachLine
                        } else {
                            trans.add(it)
                        }
                    }
                }
                Status.FlowsContent -> {
                    if (it.isNotBlank()) {
                        if (it.startsWith(".initial_marking")) {
                            marking = parseInitial(it.substringAfter(".initial_marking"))
                            current = Status.InitialMarking
                        } else {
                            val (s, flow) = parseFlow(it)
                            flows[s] = flow
                        }
                    }
                }

                Status.InitialMarking -> {

                }
            }
        }
        val res = PetriNetModel(name)
        res.places = places
        res.initMarking = marking
        res.transitions = trans
        res.flows = flows
        description?.let {
            res.description = it
        }
        return res
    }

    private fun parseInitial(initialContent: String): Map<String, Int> {
        val data = initialContent.replace("{", "").replace("}", "")
        if (data.trim().isEmpty()) {
            return HashMap()
        }
        val markings = data.split(",")
        return markings.associate {
            val split = it.trim().split("*")
            Pair(split[1], split[0].toInt())
        }
    }

    private fun parseFlow(flowContent: String): Pair<String, Flow> {
        val flowName = flowContent.substringBefore(":")
        val flowData = flowContent.substringAfter(":")
        val pattern = Pattern.compile("\\{([^\\}]*)\\} -> \\{([^\\}]*)\\}")
        val matcher = pattern.matcher(flowData)
        if (matcher.find()) {
            val inComingContent = matcher.group(1).replace("\\}".toRegex(), "").split(",")
            val outComingContent = matcher.group(2).replace("\\}".toRegex(), "").split(",")
            val inComing = if (inComingContent[0].isNotEmpty()) {
                inComingContent.map {
                    val split = it.trim().split("*")
                    Pair(split[1], split[0].toInt())
                }.toMutableList()
            } else {
                ArrayList()
            }
            val outComing = if (outComingContent[0].isNotEmpty()) {
                outComingContent.map {
                    val split = it.trim().split("*")
                    Pair(split[1], split[0].toInt())
                }.toMutableList()
            } else {
                ArrayList()
            }
            val flow = Flow(flowName)
            flow.inComing = inComing
            flow.outComing = outComing
            return Pair(flowName, flow)
        } else {
            println("error when parsing flow")
            return Pair(flowName, Flow(flowName))
        }
    }
}

fun main() {
    val preFix = "/Users/xxrl/codebase/bpmn_projects/BPMNGen/src/main/resources/"
    val petrinet = PetriNetParser().parseApt(File("${preFix}case1.apt"))
    println(petrinet)
}