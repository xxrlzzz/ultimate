package main.kotlin.bpmn.element

class Artifacts(name: String, id: String, val artifactType: String) : MiMoFlowElement(name, id) {

    override fun getType() = BPMNElement.Type.Artifacts

    override fun hasChild(): Boolean {
        return false
    }
}