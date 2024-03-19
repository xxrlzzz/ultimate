package main.kotlin.bpmn.element

class TaskElement(name: String, id: String): SiSoFlowElement(name, id) {
    var taskType = TaskType.Normal
    var dataInput: List<String>? = null
    var dataOutput: List<String>? = null
    enum class TaskType {
        Normal,
        User,
        Service,
        BusinessRule,
        Script,
        Receive,
    }

    override fun getType(): BPMNElement.Type {
        return BPMNElement.Type.Tasks
    }

    override fun hasChild(): Boolean {
        return false
    }

    fun addDataInput(dataStore: String) {
        if (dataInput == null) {
            dataInput = ArrayList()
        }
        (dataInput as ArrayList).add(dataStore)
    }


    fun addDataOutput(dataStore: String) {
        if (dataOutput == null) {
            dataOutput = ArrayList()
        }
        (dataOutput as ArrayList).add(dataStore)
    }



//    fun getTaskType() = taskType
}
