package main.kotlin.bpmn.element

abstract class EventElement<T>(name: String, id: String) : SiSoFlowElement(name, id) {
    var throwStatus = ThrowStatus.None
    var eventType = EventType.Normal
    val attachData: T? = null
    var boundaryElement: BPMNElement? = null

    override fun getType(): BPMNElement.Type {
        return BPMNElement.Type.Events
    }

    override fun hasChild(): Boolean {
        return false
    }

    enum class Place {
        Start,
        Intermediate,
        End,
    }

    enum class ThrowStatus {
        Throw,
        Catch,
        None
    }

    enum class EventType {
        Normal,
        Message,
        Timer,
        Conditional, // what is this?
        Signal,
        Escalation, // what is this?
        Error,
        Cancel,
        Compensation // what is this?
    }

    abstract fun getPlace(): Place

//    open fun getEventType() = eventType

    fun throwEvent() =
        throwStatus == ThrowStatus.Throw

    fun catchEvent() =
        throwStatus == ThrowStatus.Catch
    open fun boundaryEvent() = boundaryElement == null
    open fun boundaryElement() = boundaryElement
}

class StartEvent<T>(name: String, id: String, data: T? = null) : EventElement<T>(name, id) {
    override fun getPlace(): Place {
        return Place.Start
    }
}

class EndEvent<T>(name: String, id: String, data: T? = null) : EventElement<T>(name, id) {
    override fun getPlace(): Place {
        return Place.End
    }
}

class IntermediateEvent<T>(name: String, id:String, data: T? = null): EventElement<T>(name, id) {
    override fun getPlace(): Place {
        return Place.Intermediate
    }
}

