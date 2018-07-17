package com.xt.endo

import com.eclipsesource.v8.*
import com.eclipsesource.v8.utils.V8ObjectUtils

/**
 * Created by cuiminghui on 2018/6/11.
 */

class EDOObjectTransfer {

    companion object {

        fun convertToJSValueWithNSValue(anValue: Any?, context: V8): Any {
            anValue?.let {
                (anValue as? Int)?.let { return it }
                (anValue as? Double)?.let { return it }
                (anValue as? Float)?.let { return it.toDouble() }
                (anValue as? String)?.let { return it }
                (anValue as? Boolean)?.let { return it }
//                (anValue as? EDONativeObject)?.let {
//                    return EDOExporter.sharedExporter.scriptObjectWithObject(it, context)
//                }
                (anValue as? Map<String, Any>)?.let {
                    return this.convertToJSDictionaryWithNSDictionary(it, context)
                }
                (anValue as? List<*>)?.let {
                    return this.convertToJSArrayWithNSArray(it, context)
                }
            }
            return V8.getUndefined()
        }

        fun convertToJSDictionaryWithNSDictionary(nsDictionary: Map<String, Any?>, context: V8): V8Object {
            val jsDictionary = nsDictionary.mapValues { return@mapValues if (it.value != null) this.convertToJSValueWithNSValue(it.value!!, context) else null }
            return V8ObjectUtils.toV8Object(context, jsDictionary)
        }

        fun convertToJSListWithNSArray(nsArray: List<*>, context: V8): List<Any> {
            return nsArray.map { this.convertToJSValueWithNSValue(it, context) }
        }

        fun convertToJSArrayWithNSArray(nsArray: List<*>, context: V8): V8Object {
            return V8ObjectUtils.toV8Array(context, nsArray.map { this.convertToJSValueWithNSValue(it, context) })
        }

        fun convertToNSValueWithJSValue(anValue: Any, owner: V8Object, eageringType: Class<*>? = null): Any? {
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
                    return this.convertToNSArgumentsWithJSArguments(anValue, owner)
                }
//                else if (anValue.v8Type == 6 && anValue is V8Object) {
//                    val metaClass = anValue.getObject("_meta_class")
//                    if (!metaClass.isUndefined) {
//                        (metaClass.get("objectRef") as? String)?.let {
//                            metaClass.release()
//                            return EDOExporter.sharedExporter.nsValueWithObjectRef(it)
//                        }
//                    }
//                    metaClass.release()
//                    return this.convertToNSDictionaryWithJSDictionary(anValue, owner)
//                }
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

        fun convertToNSValueWithPlainValue(anValue: Any, owner: V8Object, eageringType: Class<*>? = null): Any? {
            (anValue as? Map<String, Any?>)?.let {
                (it["_meta_class"] as? Map<String, Any?>)?.let {
                    val objectRef = it["objectRef"] as? String ?: return anValue
//                    return EDOExporter.sharedExporter.nsValueWithObjectRef(objectRef)
                }
                return this.convertToNSValueWithPlainValue(it, owner)
            }
            (anValue as? List<Any>)?.let {
                return it.map { return@map this.convertToNSValueWithPlainValue(it, owner) }
            }
            return anValue
        }

        fun convertToNSDictionaryWithJSDictionary(jsDictionary: V8Object, owner: V8Object): Map<String, Any?> {
            return V8ObjectUtils.toMap(jsDictionary).mapValues {
                return@mapValues if (it.value != null) this.convertToNSValueWithPlainValue(it.value!!, owner) else it.value
            }
        }

        fun convertToNSArgumentsWithJSArguments(jsArguments: V8Array, owner: V8Object, eageringTypes: List<Class<*>>? = null): List<*> {
            return (0 until jsArguments.length()).map {
                val eageringType = if (it < eageringTypes?.count() ?: 0) eageringTypes?.get(it) else null
                val jsArgument = jsArguments.get(it)
                val returnValue = this.convertToNSValueWithJSValue(jsArgument, owner, eageringType)
                (jsArgument as? Releasable)?.release()
                return@map returnValue
            }
        }

    }

}