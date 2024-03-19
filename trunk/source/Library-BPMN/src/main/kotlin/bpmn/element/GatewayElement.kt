package main.kotlin.bpmn.element

class GatewayElement(name: String, id: String) : MiMoFlowElement(name, id) {
    var gatewayType = GatewayType.Exclusive

    enum class GatewayType {
        Exclusive,
        Inclusive,
        Intermediate,
        Parallel,
        Complex, // unused
        EventBase
    }

    override fun getType(): BPMNElement.Type {
        return BPMNElement.Type.Gateways
    }

    override fun hasChild(): Boolean {
        return false
    }

    override fun connNext(nextElement: String) {
        if (prevElements().size > 1 && nextElements().size == 1) {
            println("warn: gateway element $name has multiple input and output")
        }
        super.connNext(nextElement)
    }

    override fun connPrev(prevElement: String) {
        if (prevElements().size == 1 && nextElements().size > 1) {
            println("warn: gateway element $name has multiple input and output")
        }
        super.connPrev(prevElement)
    }

//    fun getGatewayType(): GatewayType {
//        return gatewayType
//    }
}