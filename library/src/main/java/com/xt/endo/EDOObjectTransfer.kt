package com.xt.endo

import com.eclipsesource.v8.V8Object
import com.eclipsesource.v8.V8Value

/**
 * Created by cuiminghui on 2018/6/11.
 */

class EDOObjectTransfer {

    companion object {

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
                else if (anValue.v8Type == 6 && anValue is V8Object) {
                    (anValue.getObject("_meta_class")?.get("objectRef") as? String)?.let {
                        return EDOExporter.sharedExporter.nsValueWithObjectRef(it)
                    }
                }
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


    }

}