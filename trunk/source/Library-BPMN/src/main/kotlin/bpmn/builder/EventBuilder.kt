package main.kotlin.bpmn.builder

import main.kotlin.bpmn.element.*
import java.lang.Exception

class EventBuilder : CommonBuilder() {
    private var place = EventElement.Place.Intermediate
    var type = EventElement.EventType.Normal
    private var msg: String? = null
    private var throwStatus = EventElement.ThrowStatus.None
    private var boundaryElement: BPMNElement? = null
    override fun name(name: String): EventBuilder {
        super.name(name)
        return this
    }

    override fun id(id: String): EventBuilder {
        super.id(id)
        return this
    }

    fun place(place: EventElement.Place): EventBuilder {
        this.place = place
        return this
    }

    fun type(type: EventElement.EventType): EventBuilder {
        this.type = type
        return this
    }

    fun message(msg: String?): EventBuilder {
        type = EventElement.EventType.Message
        this.msg = msg
        return this
    }

    fun setThrow(): EventBuilder {
        throwStatus = EventElement.ThrowStatus.Throw
        return this
    }

    fun setCatch(): EventBuilder {
        throwStatus = EventElement.ThrowStatus.Catch
        return this
    }

    fun boundary(element: BPMNElement): EventBuilder {
        this.boundaryElement = element
        return this
    }

    override fun build(): EventElement<Unit> {
        if (id.isEmpty() || name.isEmpty()) {
            throw Exception("invalid id ${id}/name $name")
        }
        val eventElement = when (place) {
            EventElement.Place.Start -> StartEvent<Unit>(name, id)
            EventElement.Place.Intermediate -> IntermediateEvent(name, id)
            EventElement.Place.End -> EndEvent(name, id)
        }
        eventElement.eventType = type
        eventElement.throwStatus = throwStatus
        eventElement.boundaryElement = boundaryElement
        return eventElement
    }
}