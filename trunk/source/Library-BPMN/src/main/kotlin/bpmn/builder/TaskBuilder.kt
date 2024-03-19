package main.kotlin.bpmn.builder

import main.kotlin.bpmn.element.TaskElement
import java.lang.Exception

class TaskBuilder: CommonBuilder() {
    var type = TaskElement.TaskType.Normal
    fun type(type: TaskElement.TaskType): TaskBuilder {
        this.type = type
        return this
    }
    override fun build(): TaskElement {
        if (id.isEmpty() || name.isEmpty()) {
            throw Exception("invalid id ${id}/name ${name}")
        }
        val taskElement = TaskElement(name, id)
        taskElement.taskType = type
        return taskElement
    }
    override fun name(name: String): TaskBuilder {
        super.name(name)
        return this
    }

    override fun id(id: String): TaskBuilder {
        super.id(id)
        return this
    }

}