package com.xt.endo.sample

import android.support.test.runner.AndroidJUnit4
import com.eclipsesource.v8.V8
import com.xt.endo.EDOExporter
import com.xt.endo.EDOObjectTransfer
import com.xt.jscore.JSContext
import com.xt.jscore.JSValue
import org.junit.runner.RunWith
import org.junit.Assert.*
import org.junit.Test

/**
 * Created by cuiminghui on 2018/7/19.
 */
@RunWith(AndroidJUnit4::class)
class ArgumentTests {

    @Test
    fun testArguments() {
        val context = JSContext()
        EDOExporter.sharedExporter.exportWithContext(context)
        val value = context.evaluateScript("var obj = new ArgumentTestObject; obj") as JSValue
        val obj = EDOObjectTransfer.convertToJavaObjectWithJSValue(value, value) as ArgumentTestObject
        context.evaluateScript("obj.testIntValue(1)")
        context.evaluateScript("obj.testFloatValue(1.1)")
        context.evaluateScript("obj.testDoubleValue(1.2)")
        context.evaluateScript("obj.testBoolValue(true)")
        context.evaluateScript("obj.testRectValue({x: 1, y: 2, width: 3, height: 4})")
        context.evaluateScript("obj.testSizeValue({width: 5, height: 6})")
        context.evaluateScript("obj.testAffineTransformValue({a: 1, b: 2, c: 3, d: 4, tx: 44, ty: 55})")
        context.evaluateScript("obj.testStringValue('String Value')")
        context.evaluateScript("obj.testArrayValue([1,2,3,4])")
        context.evaluateScript("obj.testDictValue({aKey: 'aValue'})")
        context.evaluateScript("obj.testNilValue(undefined)")
        context.evaluateScript("obj.testNilValue()")
        context.evaluateScript("obj.testObjectValue(new FooObject)")
        context.evaluateScript("obj.testBlockValue(function(r){return r;})")
        assertEquals(obj.fulfills, 14)
    }

}