package com.xt.endo

import com.eclipsesource.v8.V8
import com.eclipsesource.v8.V8Array
import com.eclipsesource.v8.V8Object
import java.util.*

/**
 * Created by cuiminghui on 2018/7/19.
 */

class EDOJavaHelper {

    companion object {

        val listeningEvents: WeakHashMap<Any, Set<String>> = WeakHashMap()

        fun emit(obj: Any, eventName: String, vararg arguments: Any?) {
            if (listeningEvents[obj]?.contains(eventName) != true) { return }
            EDOExporter.sharedExporter.scriptObjectsWithObject(obj).filter { it is V8Object && it.v8Type == 6 }.forEach { scriptObject ->
                val args = arguments.toList().map { EDOObjectTransfer.convertToJSValueWithJavaValue(it, scriptObject.runtime) }
                var v8Array = V8Array(scriptObject.runtime)
                v8Array.push(eventName)
                args.forEach { v8Array.push(it) }
                try {
                    (scriptObject as V8Object).executeFunction("emit", v8Array)
                } catch (e: Exception) { }
                v8Array?.release()
            }
        }

        fun value(obj: Any, eventName: String, vararg arguments: Any?): Any? {
            if (listeningEvents[obj]?.contains(eventName) != true) { return null }
            EDOExporter.sharedExporter.scriptObjectsWithObject(obj).firstOrNull { it is V8Object && it.v8Type == 6 }?.let { scriptObject ->
                val args = arguments.toList().map { EDOObjectTransfer.convertToJSValueWithJavaValue(it, scriptObject.runtime) }
                var v8Array = V8Array(scriptObject.runtime)
                v8Array.push(eventName)
                args.forEach { v8Array.push(it) }
                val returnValue = try {
                    (scriptObject as V8Object).executeFunction("val", v8Array)
                } catch (e: Exception) { V8.getUndefined() }
                v8Array?.release()
                return EDOObjectTransfer.convertToJavaObjectWithJSValue(returnValue, returnValue as? V8Object)
            }
            return null
        }

        fun invokeBindedMethod(obj: Any, method: String, vararg arguments: Any?) {
            EDOExporter.sharedExporter.scriptObjectsWithObject(obj).forEach {
                val scriptObject = it as? V8Object ?: return@forEach
                var v8Array: V8Array? = null
                val args = arguments.toList()
                if (args.count() > 0) {
                    v8Array = EDOObjectTransfer.convertToJSArrayWithJavaList(args, scriptObject.runtime)
                }
                try {
                    scriptObject.executeFunction("__$method", v8Array)
                } catch (e: Exception) {}
                v8Array?.release()
            }
        }

    }

}
