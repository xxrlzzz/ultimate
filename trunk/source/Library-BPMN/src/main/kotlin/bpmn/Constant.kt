package main.kotlin.bpmn

import main.kotlin.bpmn.element.BPMNElement

object Constant {
    const val TAG_PROCESS = "process"
    const val TAG_SUBPROCESS = "subProcess"
    const val TAG_SEQUENCE_FLOW = "sequenceFlow"

    const val TAG_LANE_SET = "laneSet"
    const val TAG_LANE = "lane"

    const val TAG_FLOW_NODE_REF = "flowNodeRef"

    const val TAG_START_EVENT = "startEvent"
    const val TAG_END_EVENT = "endEvent"
    const val TAG_INTERMEDIATE_THROW_EVENT = "intermediateThrowEvent"
    const val TAG_INTERMEDIATE_CATCH_EVENT = "intermediateCatchEvent"

    const val TAG_TASK = "task"
    const val TAG_USER_TASK = "userTask"
    const val TAG_RECEIVE_TASK = "receiveTask"

    const val TAG_DATA_STORE = "dataStoreReference"
    const val TAG_DATA_INPUT = "dataInputAssociation"
    const val TAG_DATA_OUTPUT = "dataOutputAssociation"

    const val TAG_EXCLUSIVE_GATEWAY = "exclusiveGateway"
    const val TAG_INCLUSIVE_GATEWAY = "inclusiveGateway"
    const val TAG_COMPLEX_GATEWAY = "complexGateway"

    const val TAG_MESSAGE_EVENT_DEF = "messageEventDefinition"
    const val TAG_TIMER_EVENT_DEF = "timerEventDefinition"
    const val TAG_CONDITION_EVENT_DEF = "conditionalEventDefinition"


    fun simplifyName(eleName: String): String {
        return when (eleName) {
            TAG_TASK -> "nT"
            TAG_USER_TASK -> "uT"
            TAG_RECEIVE_TASK -> "rT"
            TAG_EXCLUSIVE_GATEWAY -> "eG"
            TAG_INCLUSIVE_GATEWAY -> "iG"
            TAG_COMPLEX_GATEWAY -> "cG"
            TAG_START_EVENT -> "sE"
            TAG_END_EVENT -> "eE"
            TAG_INTERMEDIATE_THROW_EVENT -> "itE"
            TAG_INTERMEDIATE_CATCH_EVENT -> "icE"
            else -> eleName
        }
    }

    fun fullName(eleName: String): String {
        return when (eleName) {
            "nT" -> TAG_TASK
            "uT" -> TAG_USER_TASK
            "rT" -> TAG_RECEIVE_TASK
            "eG" -> TAG_EXCLUSIVE_GATEWAY
            "iG" -> TAG_INCLUSIVE_GATEWAY
            "cG" -> TAG_COMPLEX_GATEWAY
            "sE" -> TAG_START_EVENT
            "eE" -> TAG_END_EVENT
            "itE" -> TAG_INTERMEDIATE_THROW_EVENT
            "icE" -> TAG_INTERMEDIATE_CATCH_EVENT
            else -> eleName
        }
    }

    fun elementType(eleName: String): String {
        return when (eleName) {
            TAG_TASK -> "Activity"
            TAG_USER_TASK -> "Activity"
            TAG_RECEIVE_TASK -> "Activity"
            TAG_EXCLUSIVE_GATEWAY -> "Gateway"
            TAG_INCLUSIVE_GATEWAY -> "Gateway"
            TAG_COMPLEX_GATEWAY -> "Gateway"
            TAG_START_EVENT -> "Event"
            TAG_END_EVENT -> "Event"
            TAG_INTERMEDIATE_THROW_EVENT -> "Event"
            TAG_INTERMEDIATE_CATCH_EVENT -> "Event"
            else -> eleName
        }
    }
}