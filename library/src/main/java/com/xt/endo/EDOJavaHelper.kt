package com.xt.endo

import android.os.SystemClock
import com.eclipsesource.v8.V8
import com.eclipsesource.v8.V8Array
import com.eclipsesource.v8.V8Object
import com.xt.jscore.JSContext
import java.util.*

/**
 * Created by cuiminghui on 2018/7/19.
 */

class EDOJavaHelper {

    companion object {

        internal val listeningEvents: WeakHashMap<Any, Set<String>> = WeakHashMap()
        internal val cachingProperties: WeakHashMap<Any, MutableSet<String>> = WeakHashMap()

        fun valueChanged(obj: Any, propName: String) {
            (obj as? String)?.let { clazzName ->
                EDOExporter.sharedExporter.activeContexts().forEach {
                    JSContext.setCurrentContext(EDOExporter.sharedExporter.contextWithRuntime(it.runtime))
                    try {
                        it.evaluateScript("delete _EDO_valueMaps.get(${clazzName})['$propName']")
                    } catch (e: Exception) { }
                }
                return
            }
            if (cachingProperties[obj]?.contains(propName) == true || cachingProperties[obj]?.contains("edo_" + propName) == true) {
                EDOExporter.sharedExporter.scriptObjectsWithObject(obj).filter { it is V8Object && it.v8Type == 6 }.forEach { scriptObject ->
                    JSContext.setCurrentContext(EDOExporter.sharedExporter.contextWithRuntime(scriptObject.runtime))
                    try {
                        (scriptObject as V8Object).executeJSFunction("__clearValueCache", propName)
                    } catch (e: Exception) { }
                }
                cachingProperties[obj]?.remove(propName)
            }
        }

        fun emit(obj: Any, eventName: String, vararg arguments: Any?) {
            try {
                if (listeningEvents[obj]?.contains(eventName) != true) { return }
                EDOExporter.sharedExporter.scriptObjectsWithObject(obj).filter { it is V8Object && it.v8Type == 6 }.forEach { scriptObject ->
                    JSContext.setCurrentContext(EDOExporter.sharedExporter.contextWithRuntime(scriptObject.runtime))
                    val args = arguments.toList().map { EDOObjectTransfer.convertToJSValueWithJavaValue(it, scriptObject.runtime) }
                    var v8Array = V8Array(scriptObject.runtime)
                    v8Array.push(eventName)
                    args.forEach { v8Array.push(it) }
                    try {
                        (scriptObject as V8Object).executeFunction("emit", v8Array)
                    } catch (e: Exception) { }
                    v8Array?.release()
                }
            } catch (e: Exception) { }
        }

        fun value(obj: Any, eventName: String, vararg arguments: Any?): Any? {
            try {
                if (listeningEvents[obj]?.contains(eventName) != true) { return null }
                EDOExporter.sharedExporter.scriptObjectsWithObject(obj).firstOrNull { it is V8Object && it.v8Type == 6 }?.let { scriptObject ->
                    JSContext.setCurrentContext(EDOExporter.sharedExporter.contextWithRuntime(scriptObject.runtime))
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
            } catch (e: Exception) {}
            return null
        }

        fun invokeBindedMethod(obj: Any, method: String, vararg arguments: Any?) {
            try {
                EDOExporter.sharedExporter.scriptObjectsWithObject(obj).forEach {
                    val scriptObject = it as? V8Object ?: return@forEach
                    JSContext.setCurrentContext(EDOExporter.sharedExporter.contextWithRuntime(scriptObject.runtime))
                    var v8Array: V8Array? = null
                    val args = arguments.toList()
                    if (args.count() > 0) {
                        v8Array = EDOObjectTransfer.convertToJSArrayWithJavaList(args, scriptObject.runtime)
                    }
                    try {
                        scriptObject.executeFunction("__$method", v8Array)
                    } catch (e: Exception) { }
                    v8Array?.release()
                }
            } catch (e: Exception) {}
        }

    }

}
