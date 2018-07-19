package com.xt.endo.sample

import android.support.test.runner.AndroidJUnit4
import com.eclipsesource.v8.V8
import com.xt.endo.EDOExporter
import com.xt.endo.EDOObjectTransfer
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
        val context = V8.createV8Runtime()
        EDOExporter.sharedExporter.exportWithContext(context)
        val v8Object = context.executeObjectScript("var obj = new ArgumentTestObject; obj")
        val obj = EDOObjectTransfer.convertToJavaObjectWithJSValue(v8Object, v8Object) as ArgumentTestObject
        context.executeScript("obj.testIntValue(1)")
        context.executeScript("obj.testFloatValue(1.1)")
        context.executeScript("obj.testDoubleValue(1.2)")
        context.executeScript("obj.testBoolValue(true)")
        context.executeScript("obj.testRectValue({x: 1, y: 2, width: 3, height: 4})")
        context.executeScript("obj.testSizeValue({width: 5, height: 6})")
        context.executeScript("obj.testAffineTransformValue({a: 1, b: 2, c: 3, d: 4, tx: 44, ty: 55})")
        context.executeScript("obj.testStringValue('String Value')")
        context.executeScript("obj.testArrayValue([1,2,3,4])")
        context.executeScript("obj.testDictValue({aKey: 'aValue'})")
        context.executeScript("obj.testNilValue(undefined)")
        context.executeScript("obj.testObjectValue(new FooObject)")
        context.executeScript("obj.testBlockValue(function(r){return r;})")
        assertEquals(obj.fulfills, 13)
    }

}