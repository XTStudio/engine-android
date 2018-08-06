package com.xt.endo.sample

import com.eclipsesource.v8.V8
import com.eclipsesource.v8.V8Object
import com.xt.endo.*
import java.lang.Error
import java.nio.ByteBuffer

/**
 * Created by cuiminghui on 2018/7/19.
 */
class ReturnTestObject {

    fun intValue(): Int {
        return 1
    }

    fun floatValue(): Float {
        return 1.1f
    }

    fun doubleValue(): Double {
        return 1.2
    }

    fun boolValue(): Boolean{
        return true
    }

    fun rectValue(): CGRect {
        return CGRect(1.0, 2.0, 3.0, 4.0)
    }

    fun sizeValue(): CGSize {
        return CGSize(5.0, 6.0)
    }

    fun affineTransformValue(): CGAffineTransform {
        return CGAffineTransform(1.0, 2.0, 3.0, 4.0, 55.0, 66.0)
    }

    fun stringValue(): String {
        return "String Value"
    }

    fun arrayValue(): List<Int> {
        return listOf(1, 2, 3, 4)
    }

    fun dictValue(): Map<String, Any> {
        return mapOf(Pair("aKey", "aValue"))
    }

    fun nilValue(): Any? {
        return null
    }

    fun jsValue(): V8Object {
        val context = v8CurrentContext() ?: return throw Exception("can not get v8CurrentContext.")
        val v8Object = V8Object(context)
        v8Object.add("aKey", "aValue")
        return v8Object
    }

    fun objectValue(): FooObject {
        return FooObject()
    }

    fun unexportdClassValue(): XXXObject {
        return XXXObject()
    }

    fun errorValue(): Error {
        return Error("Error Message.")
    }

    fun arrayBufferValue(): ByteBuffer {
        val byteArray = "Hello, World!".toByteArray()
        val byteBuffer = ByteBuffer.allocateDirect(byteArray.size)
        byteBuffer.put(byteArray)
        return byteBuffer
    }

}