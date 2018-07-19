package com.xt.endo.sample

import com.xt.endo.CGAffineTransform
import com.xt.endo.CGRect
import com.xt.endo.CGSize
import com.xt.endo.EDOCallback

/**
 * Created by cuiminghui on 2018/7/19.
 */
class ArgumentTestObject {

    var fulfills = 0

    fun testIntValue(value: Int) {
        if (value == 1) {
            fulfills++
        }
        else {
            throw Exception()
        }
    }

    fun testFloatValue(value: Float) {

        if (Math.abs(value - 1.1f) < 0.01) {
            fulfills++
        }
        else {
            throw Exception()
        }
    }

    fun testDoubleValue(value: Double) {
        if (Math.abs(value - 1.2f) < 0.01) {
            fulfills++
        }
        else {
            throw Exception()
        }
    }

    fun testBoolValue(value: Boolean) {
        if (value) {
            fulfills++
        }
        else {
            throw Exception()
        }
    }

    fun testRectValue(value: CGRect) {
        if (value.x == 1.0 && value.y == 2.0 && value.width == 3.0 && value.height == 4.0) {
            fulfills++
        }
        else {
            throw Exception()
        }
    }

    fun testSizeValue(value: CGSize) {
        if (value.width == 5.0 && value.height == 6.0) {
            fulfills++
        }
        else {
            throw Exception()
        }
    }

    fun testAffineTransformValue(value: CGAffineTransform) {
        if (value.a == 1.0 && value.b == 2.0 && value.c == 3.0 && value.d == 4.0 && value.tx == 44.0 && value.ty == 55.0) {
            fulfills++
        }
        else {
            throw Exception()
        }
    }

    fun testStringValue(value: String) {
        if (value == "String Value") {
            fulfills++
        }
        else {
            throw Exception()
        }
    }

    fun testArrayValue(value: List<Any>) {
        if (value[0] == 1 && value[1] == 2 && value[2] == 3 && value[3] == 4) {
            fulfills++
        }
        else {
            throw Exception()
        }
    }

    fun testDictValue(value: Map<String, Any>) {
        if (value["aKey"] == "aValue") {
            fulfills++
        }
        else {
            throw Exception()
        }
    }

    fun testNilValue(value: Any?) {
        if (value == null) {
            fulfills++
        }
        else {
            throw Exception()
        }
    }

    fun testObjectValue(value: FooObject) {
        fulfills++
    }

    fun testBlockValue(value: EDOCallback) {
        if (value.invoke(2) == 2) {
            fulfills++
        }
        else {
            throw Exception()
        }
    }

}