package main.kotlin.bpmn

import main.kotlin.bpmn.element.BPMNElement
import main.kotlin.bpmn.element.FlowElement
import main.kotlin.bpmn.element.SubProcessElement
import java.util.Stack

class ElementVisitor {
    var callback: Function2<BPMNElement, Context, Unit>? = null
    var processFinish: Function1<Unit, Unit>? = null
    var contextStack = Stack<Context>()
    var context = Context(null)

    private val visMap = hashSetOf<String>()

    /**
     * 遍历一个process
     * 找到 startEvent, 然后从 startEvent 的第一个后继开始遍历
     */
    fun travel(process: SubProcessElement) {
        val startEvent = process.startEvent ?: return
        context = Context(process)
        callback?.invoke(startEvent, context)
        val nxtId = startEvent.nextElements().elementAtOrNull(0) ?: return
        context.prevElement = startEvent
        travel(process, process.getElement(nxtId))
        processFinish?.invoke(Unit)
    }

    private fun travel(process: SubProcessElement, ele: BPMNElement?) {
        if (ele == null) {
            return
        }
        context.isRevisit = visMap.contains(ele.getIdV())
        visMap.add(ele.getIdV())
        callback?.invoke(ele, context)

        // 如果元素已经被访问过，只触发当前元素的回调，不再触发后续逻辑
        if (context.isRevisit) {
            return
        }
        if (ele is SubProcessElement) {
            contextStack.add(context)
            travel(ele)
            context = contextStack.pop()
        }
        if (ele is FlowElement) {
            val nxtIds = ele.nextElements()
            for (id in nxtIds) {
                context.prevElement = ele
                travel(process, process.getElement(id))
            }
        }
    }
    fun reset() {
        visMap.clear()
        context = Context(null)
    }

    class Context(var process: SubProcessElement? = null) {
        var prevElement: BPMNElement? = null
        var isRevisit = false
    }

    companion object {
        fun defaultVisitor() : ElementVisitor {
            val visitor = ElementVisitor()
            visitor.callback = { it: BPMNElement, _: Context ->
                println(it.getNameV())
            }
            visitor.processFinish = {
                println()
            }
            return visitor
        }
    }
}