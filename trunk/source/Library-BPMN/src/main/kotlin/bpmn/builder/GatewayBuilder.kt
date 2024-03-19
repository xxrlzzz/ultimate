package main.kotlin.bpmn.builder

import main.kotlin.bpmn.element.GatewayElement
import java.lang.Exception

class GatewayBuilder: CommonBuilder() {
    var type = GatewayElement.GatewayType.Exclusive
    fun type(type: GatewayElement.GatewayType): GatewayBuilder {
        this.type = type
        return this
    }
    override fun build(): GatewayElement {
        if (id.isEmpty() || name.isEmpty()) {
            throw Exception("invalid id ${id}/name ${name}")
        }
        val gateway = GatewayElement(name, id)
        gateway.gatewayType = type
        return gateway
    }
    override fun name(name: String): GatewayBuilder {
        super.name(name)
        return this
    }

    override fun id(id: String): GatewayBuilder {
        super.id(id)
        return this
    }
}