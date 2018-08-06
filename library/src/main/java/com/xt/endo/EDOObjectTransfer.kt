package com.xt.endo

import com.eclipsesource.v8.*
import com.eclipsesource.v8.utils.V8ObjectUtils
import com.xt.jscore.JSValue
import java.lang.Error
import kotlin.math.max

/**
 * Created by cuiminghui on 2018/6/11.
 */

class EDOObjectTransfer {

    companion object {

        fun convertToJSValueWithJavaValue(anValue: Any?, context: V8): Any {
            anValue?.let {
                (anValue as? V8Value)?.let { return it }
                (anValue as? Int)?.let { return it }
                (anValue as? Double)?.let { return it }
                (anValue as? Float)?.let { return it.toDouble() }
                (anValue as? String)?.let { return it }
                (anValue as? Boolean)?.let { return it }
                (anValue as? Map<String, Any>)?.let {
                    return this.convertToJSDictionaryWithJavaMap(it, context)
                }
                (anValue as? List<*>)?.let {
                    return this.convertToJSArrayWithJavaList(it, context)
                }
                (anValue as? EDOStruct)?.let {
                    return it.toJSObject(context)
                }
                (anValue as? Error)?.let {
                    return context.executeObjectScript("new Error('${it.message ?: ""}')")
                }
                return EDOExporter.sharedExporter.scriptObjectWithObject(it, context, true).twin()
            }
            return V8.getUndefined()
        }

        fun convertToJSDictionaryWithJavaMap(javaMap: Map<String, Any?>, context: V8): V8Object {
            val jsDictionary = javaMap.mapValues { return@mapValues if (it.value != null) this.convertToJSValueWithJavaValue(it.value!!, context) else null }
            return V8ObjectUtils.toV8Object(context, jsDictionary)
        }

        fun convertToJSArrayWithJavaList(javaList: List<*>, context: V8): V8Array {
            return V8ObjectUtils.toV8Array(context, javaList.map { this.convertToJSValueWithJavaValue(it, context) })
        }

        fun convertToJavaObjectWithJSValue(anValue: Any, owner: JSValue?, eageringType: Class<*>? = null): Any? {
            val tAnValue = (anValue as? JSValue)?.v8Value ?: anValue
            val tOwner = owner?.v8Value as? V8Object
            return this.convertToJavaObjectWithJSValue(tAnValue, tOwner, eageringType)
        }

        fun convertToJavaObjectWithJSValue(anValue: Any, owner: V8Object?, eageringType: Class<*>? = null): Any? {
            (anValue as? V8Value)?.let {
                if (anValue.v8Type == 1) {
                    return anValue as? Int ?: 0
                }
                else if (anValue.v8Type == 2) {
                    return anValue as? Double ?: 0.0
                }
                else if (anValue.v8Type == 3) {
                    return anValue as? Boolean ?: false
                }
                else if (anValue.v8Type == 4) {
                    return anValue as? String ?: ""
                }
                else if (anValue.v8Type == 5 && anValue is V8Array) {
                    return this.convertToJavaListWithJSArray(anValue, owner)
                }
                else if (anValue.v8Type == 6 && anValue is V8Object) {
                    eageringType?.let { eageringType ->
                        if (EDOStruct::class.java.isAssignableFrom(eageringType)) {
                            try {
                                val method = eageringType.getDeclaredMethod("fromJSObject", V8Object::class.java)
                                return method.invoke(eageringType, anValue)
                            } catch (e: Exception) {}
                        }
                    }
                    val metaClass = anValue.getObject("_meta_class")
                    if (!metaClass.isUndefined) {
                        (metaClass.get("classname") as? String)?.takeIf { it == "__Function" }?.let {
                            val scriptObject = owner?.twin()?.setWeak() as? V8Object ?: return anValue
                            val idx = metaClass["idx"] as? Int ?: return anValue
                            return EDOCallback(scriptObject, idx)
                        }
                        (metaClass.get("classname") as? String)?.takeIf { it == "__KTENUM" }?.let {
                            val clazz = metaClass.get("clazz") as? String ?: return null
                            val value = metaClass.get("value") as? String ?: return null
                            return try {
                                Class.forName(clazz).getMethod("valueOf", String::class.java).invoke(clazz, value)
                            } catch (e: Exception) { null }
                        }
                        (metaClass.get("objectRef") as? String)?.let {
                            metaClass.release()
                            return EDOExporter.sharedExporter.javaObjectWithObjectRef(it)
                        }
                    }
                    metaClass.release()
                    return this.convertToJavaMapWithJSDictionary(anValue, owner)
                }
                else { }
            }
            (anValue as? Int)?.let {
                if (eageringType == Float::class.java) {
                    return it.toFloat()
                }
                else if (eageringType == Double::class.java) {
                    return it.toDouble()
                }
                return it
            }
            (anValue as? Double)?.let {
                if (eageringType == Float::class.java) {
                    return it.toFloat()
                }
                else if (eageringType == Int::class.java) {
                    return it.toInt()
                }
                return it
            }
            (anValue as? String)?.let { return it }
            (anValue as? Boolean)?.let { return it }
            return null
        }

        fun convertToJavaObjectWithPlainValue(anValue: Any, owner: V8Object?, eageringType: Class<*>? = null): Any? {
            (anValue as? Map<String, Any?>)?.let {
                (it["_meta_class"] as? Map<String, Any?>)?.let {
                    if (it["classname"] == "__Function") {
                        val scriptObject = owner?.twin()?.setWeak() as? V8Object ?: return anValue
                        val idx = it["idx"] as? Int ?: return anValue
                        return EDOCallback(scriptObject, idx)
                    }
                    else if (it["classname"] == "__KTENUM") {
                        val clazz = it["clazz"] as? String ?: return null
                        val value = it["value"] as? String ?: return null
                        return try {
                            Class.forName(clazz).getMethod("valueOf", String::class.java).invoke(clazz, value)
                        } catch (e: Exception) { null }
                    }
                    else {
                        val objectRef = it["objectRef"] as? String ?: return anValue
                        return EDOExporter.sharedExporter.javaObjectWithObjectRef(objectRef)
                    }
                }
                return this.convertToJavaObjectWithPlainValue(it, owner)
            }
            (anValue as? List<Any>)?.let {
                return it.map { return@map this.convertToJavaObjectWithPlainValue(it, owner) }
            }
            return anValue
        }

        fun convertToJavaMapWithJSDictionary(jsDictionary: V8Object, owner: V8Object?): Map<String, Any?> {
            return V8ObjectUtils.toMap(jsDictionary).mapValues {
                return@mapValues if (it.value != null) this.convertToJavaObjectWithPlainValue(it.value!!, owner) else it.value
            }
        }

        fun convertToJavaListWithJSArray(jsArray: V8Array, owner: V8Object?, eageringTypes: List<Class<*>>? = null): List<*> {
            return (0 until max(jsArray.length(), eageringTypes?.count() ?: 0)).map {
                val eageringType = if (it < eageringTypes?.count() ?: 0) eageringTypes?.get(it) else null
                val jsArgument = jsArray.get(it)
                val returnValue = this.convertToJavaObjectWithJSValue(jsArgument, owner, eageringType)
                (jsArgument as? Releasable)?.release()
                return@map returnValue
            }
        }

    }

}