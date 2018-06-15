package com.xt.endo

import com.eclipsesource.v8.V8Object

/**
 * Created by cuiminghui on 2018/6/11.
 */

interface EDONativeObject {

    val objectRef: String
        get() {
            return System.identityHashCode(this).toString()
        }

    fun deinit() { }

    fun retain() { EDOExporter.sharedExporter.retain(this) }

    fun release() { EDOExporter.sharedExporter.release(this) }

    fun invokeBindingMethod(name: String, arguments: List<Any>? = null) {
        EDOExporter.sharedExporter.scriptObjectWithObject(this, null)?.let {
            (it as? V8Object)?.let {
                arguments?.takeIf { it.isNotEmpty() }?.let { arguments ->
                    it.executeJSFunction("__$name", *EDOObjectTransfer.convertToJSListWithNSArray(arguments, it.runtime).toTypedArray())
                } ?: kotlin.run {
                    it.executeJSFunction("__$name")
                }
            }
        }
    }

}