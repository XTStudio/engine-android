package com.xt.endo.sample

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.eclipsesource.v8.V8
import com.xt.endo.EDOExporter
import com.xt.endo.EDOObjectTransfer

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
        val context = V8.createV8Runtime()
        EDOExporter.sharedExporter.exportWithContext(context)
        val v8Object = context.executeObjectScript("var obj = new PropertyTestObject; obj")
        val obj = EDOObjectTransfer.convertToJavaObjectWithJSValue(v8Object, v8Object) as PropertyTestObject
        context.executeScript("obj.intValue = 2")
        assertEquals(obj.intValue, 2)
        context.executeScript("obj.floatValue = 1.0")
        assertEquals(obj.floatValue, 1.0f)
        context.executeScript("obj.doubleValue = 1.0")
        assertEquals(obj.doubleValue, 1.0, 0.01)
        context.executeScript("obj.boolValue = true")
        assertEquals(obj.boolValue, true)
        context.executeScript("obj.rectValue = {x: 1, y: 2, width: 3, height: 4}")
        assertEquals(obj.rectValue.x, 1.0, 0.01)
        assertEquals(obj.rectValue.y, 2.0, 0.01)
        assertEquals(obj.rectValue.width, 3.0, 0.01)
        assertEquals(obj.rectValue.height, 4.0, 0.01)
        context.executeScript("obj.sizeValue = {width: 3, height: 4}")
        assertEquals(obj.sizeValue.width, 3.0, 0.01)
        assertEquals(obj.sizeValue.height, 4.0, 0.01)
        context.executeScript("obj.affineTransformValue = {a: 1, b: 2, c: 3, d: 4, tx: 44, ty: 55}")
        assertEquals(obj.affineTransformValue.a, 1.0, 0.01)
        assertEquals(obj.affineTransformValue.b, 2.0, 0.01)
        assertEquals(obj.affineTransformValue.c, 3.0, 0.01)
        assertEquals(obj.affineTransformValue.d, 4.0, 0.01)
        assertEquals(obj.affineTransformValue.tx, 44.0, 0.01)
        assertEquals(obj.affineTransformValue.ty, 55.0, 0.01)
        context.executeScript("obj.stringValue = 'string value'")
        assertEquals(obj.stringValue, "string value")
        context.executeScript("")
        assertEquals()
        context.executeScript("")
        assertEquals()
        context.executeScript("")
        assertEquals()
        context.executeScript("")
        assertEquals()
        context.executeScript("")
        assertEquals()
        context.executeScript("")
        assertEquals()
    }

}
