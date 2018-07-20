package com.xt.endo.sample

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.eclipsesource.v8.V8
import com.xt.endo.EDOExporter
import com.xt.endo.EDOObjectTransfer
import com.xt.jscore.JSContext
import com.xt.jscore.JSValue

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *nn
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class PropertyTests {

    @Test
    fun testProperties() {
        val context = JSContext()
        EDOExporter.sharedExporter.exportWithContext(context)
        val value = context.evaluateScript("var obj = new PropertyTestObject; obj") as JSValue
        val obj = EDOObjectTransfer.convertToJavaObjectWithJSValue(value, value) as PropertyTestObject
        context.evaluateScript("obj.intValue = 2")
        assertEquals(obj.intValue, 2)
        context.evaluateScript("obj.floatValue = 1.0")
        assertEquals(obj.floatValue, 1.0f)
        context.evaluateScript("obj.doubleValue = 1.0")
        assertEquals(obj.doubleValue, 1.0, 0.01)
        context.evaluateScript("obj.boolValue = true")
        assertEquals(obj.boolValue, true)
        context.evaluateScript("obj.rectValue = {x: 1, y: 2, width: 3, height: 4}")
        assertEquals(obj.rectValue.x, 1.0, 0.01)
        assertEquals(obj.rectValue.y, 2.0, 0.01)
        assertEquals(obj.rectValue.width, 3.0, 0.01)
        assertEquals(obj.rectValue.height, 4.0, 0.01)
        context.evaluateScript("obj.sizeValue = {width: 3, height: 4}")
        assertEquals(obj.sizeValue.width, 3.0, 0.01)
        assertEquals(obj.sizeValue.height, 4.0, 0.01)
        context.evaluateScript("obj.affineTransformValue = {a: 1, b: 2, c: 3, d: 4, tx: 44, ty: 55}")
        assertEquals(obj.affineTransformValue.a, 1.0, 0.01)
        assertEquals(obj.affineTransformValue.b, 2.0, 0.01)
        assertEquals(obj.affineTransformValue.c, 3.0, 0.01)
        assertEquals(obj.affineTransformValue.d, 4.0, 0.01)
        assertEquals(obj.affineTransformValue.tx, 44.0, 0.01)
        assertEquals(obj.affineTransformValue.ty, 55.0, 0.01)
        context.evaluateScript("obj.stringValue = 'string value'")
        assertEquals(obj.stringValue, "string value")
        context.evaluateScript("obj.arrayValue = [1, 2, 3, 4]")
        assertEquals(obj.arrayValue[0], 1)
        assertEquals(obj.arrayValue[1], 2)
        assertEquals(obj.arrayValue[2], 3)
        assertEquals(obj.arrayValue[3], 4)
        context.evaluateScript("obj.dictValue = {aKey: 'aValue'}")
        assertEquals(obj.dictValue["aKey"], "aValue")
        context.evaluateScript("obj.nilValue = undefined")
        assertEquals(obj.nilValue, null)
        context.evaluateScript("obj.objectValue = new FooObject")
        assertTrue(obj.objectValue is FooObject)
        context.evaluateScript("obj.readonlyIntValue = 2")
        assertNotEquals(obj.readonlyIntValue, 2)
    }

}
