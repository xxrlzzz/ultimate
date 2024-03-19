package main.kotlin.bpmn.builder

import main.kotlin.bpmn.BPMNElementDescriptor
import main.kotlin.bpmn.Constant
import main.kotlin.bpmn.element.*
import java.lang.RuntimeException

class DescriptorBuilder(private val descriptor: BPMNElementDescriptor): CommonBuilder() {
    override fun build(): BPMNElement {
        id(descriptor.id)
        name(descriptor.name)
        return when(descriptor.type) {
            Constant.TAG_TASK -> TaskBuilder().id(id).name(name).build()
            Constant.TAG_USER_TASK -> TaskBuilder().type(TaskElement.TaskType.User).id(id).name(name).build()
            Constant.TAG_RECEIVE_TASK -> TaskBuilder().type(TaskElement.TaskType.Receive).id(id).name(name).build()
            Constant.TAG_START_EVENT -> EventBuilder().place(EventElement.Place.Start).id(id).name(name).build()
            Constant.TAG_END_EVENT -> EventBuilder().place(EventElement.Place.End).id(id).name(name).build()
            Constant.TAG_INTERMEDIATE_THROW_EVENT -> EventBuilder().place(EventElement.Place.Intermediate)
                .setThrow().id(id).name(name).build()

            Constant.TAG_INTERMEDIATE_CATCH_EVENT -> EventBuilder().place(EventElement.Place.Intermediate)
                .setCatch().id(id).name(name).build()

            Constant.TAG_SUBPROCESS -> SubProcessElement(name, id)
            Constant.TAG_EXCLUSIVE_GATEWAY -> GatewayBuilder().id(id).name(name).build()
            Constant.TAG_INCLUSIVE_GATEWAY -> GatewayBuilder().type(GatewayElement.GatewayType.Inclusive).id(id)
                .name(name).build()

            Constant.TAG_COMPLEX_GATEWAY -> GatewayBuilder().type(GatewayElement.GatewayType.Complex).id(id)
                .name(name).build()

            Constant.TAG_DATA_STORE -> Artifacts(name, id, "dataStore")
            else -> throw RuntimeException("invalid element")
        }
    }
}