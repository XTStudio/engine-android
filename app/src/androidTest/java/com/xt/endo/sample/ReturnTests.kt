package com.xt.endo.sample

import android.support.test.runner.AndroidJUnit4
import com.eclipsesource.v8.V8
import com.xt.endo.EDOExporter
import com.xt.endo.EDOObjectTransfer
import com.xt.jscore.JSContext
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/**
 * Created by cuiminghui on 2018/7/19.
 */
@RunWith(AndroidJUnit4::class)
class ReturnTests {

    @Test
    fun testReturns() {
        val context = JSContext()
        EDOExporter.sharedExporter.exportWithContext(context)
        context.evaluateScript("var obj = new ReturnTestObject;")
        assertEquals(context.evaluateScript("obj.intValue()")?.toInt(), 1)
        assertEquals(context.evaluateScript("obj.floatValue()")?.toDouble()?.toFloat(), 1.1f)
        assertEquals(context.evaluateScript("obj.doubleValue()")!!.toDouble(), 1.2, 0.01)
        assertEquals(context.evaluateScript("obj.boolValue()")?.toBool(), true)
        assertEquals(context.evaluateScript("obj.rectValue().x === 1")?.toBool(), true)
        assertEquals(context.evaluateScript("obj.rectValue().y === 2")?.toBool(), true)
        assertEquals(context.evaluateScript("obj.rectValue().width === 3")?.toBool(), true)
        assertEquals(context.evaluateScript("obj.rectValue().height === 4")?.toBool(), true)
        assertEquals(context.evaluateScript("obj.sizeValue().width === 5")?.toBool(), true)
        assertEquals(context.evaluateScript("obj.sizeValue().height === 6")?.toBool(), true)
        assertEquals(context.evaluateScript("obj.affineTransformValue().a === 1")?.toBool(), true)
        assertEquals(context.evaluateScript("obj.affineTransformValue().b === 2")?.toBool(), true)
        assertEquals(context.evaluateScript("obj.affineTransformValue().c === 3")?.toBool(), true)
        assertEquals(context.evaluateScript("obj.affineTransformValue().d === 4")?.toBool(), true)
        assertEquals(context.evaluateScript("obj.affineTransformValue().tx === 55")?.toBool(), true)
        assertEquals(context.evaluateScript("obj.affineTransformValue().ty === 66")?.toBool(), true)
        assertEquals(context.evaluateScript("obj.stringValue() === 'String Value'")?.toBool(), true)
        assertEquals(context.evaluateScript("obj.arrayValue()[0] === 1")?.toBool(), true)
        assertEquals(context.evaluateScript("obj.arrayValue()[1] === 2")?.toBool(), true)
        assertEquals(context.evaluateScript("obj.arrayValue()[2] === 3")?.toBool(), true)
        assertEquals(context.evaluateScript("obj.arrayValue()[3] === 4")?.toBool(), true)
        assertEquals(context.evaluateScript("obj.dictValue()['aKey'] === 'aValue'")?.toBool(), true)
        assertEquals(context.evaluateScript("obj.nilValue() === undefined")?.toBool(), true)
        assertEquals(context.evaluateScript("obj.objectValue() instanceof FooObject")?.toBool(), true)
        assertEquals(context.evaluateScript("obj.unexportdClassValue() instanceof FooObject")?.toBool(), true)
        assertEquals(context.evaluateScript("obj.errorValue().message === 'Error Message.'")?.toBool(), true)
        assertEquals(context.evaluateScript("new Uint8Array(obj.arrayBufferValue())[0] === 72")?.toBool(), true)
    }

}