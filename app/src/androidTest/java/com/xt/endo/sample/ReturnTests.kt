package com.xt.endo.sample

import android.support.test.runner.AndroidJUnit4
import com.eclipsesource.v8.V8
import com.xt.endo.EDOExporter
import com.xt.endo.EDOObjectTransfer
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
        val context = V8.createV8Runtime()
        EDOExporter.sharedExporter.exportWithContext(context)
        context.executeObjectScript("var obj = new ReturnTestObject;")
        assertEquals(context.executeIntegerScript("obj.intValue()"), 1)
        assertEquals(context.executeDoubleScript("obj.floatValue()").toFloat(), 1.1f)
        assertEquals(context.executeDoubleScript("obj.doubleValue()"), 1.2, 0.01)
        assertEquals(context.executeBooleanScript("obj.boolValue()"), true)
        assertEquals(context.executeBooleanScript("obj.rectValue().x === 1"), true)
        assertEquals(context.executeBooleanScript("obj.rectValue().y === 2"), true)
        assertEquals(context.executeBooleanScript("obj.rectValue().width === 3"), true)
        assertEquals(context.executeBooleanScript("obj.rectValue().height === 4"), true)
        assertEquals(context.executeBooleanScript("obj.sizeValue().width === 5"), true)
        assertEquals(context.executeBooleanScript("obj.sizeValue().height === 6"), true)
        assertEquals(context.executeBooleanScript("obj.affineTransformValue().a === 1"), true)
        assertEquals(context.executeBooleanScript("obj.affineTransformValue().b === 2"), true)
        assertEquals(context.executeBooleanScript("obj.affineTransformValue().c === 3"), true)
        assertEquals(context.executeBooleanScript("obj.affineTransformValue().d === 4"), true)
        assertEquals(context.executeBooleanScript("obj.affineTransformValue().tx === 55"), true)
        assertEquals(context.executeBooleanScript("obj.affineTransformValue().ty === 66"), true)
        assertEquals(context.executeBooleanScript("obj.stringValue() === 'String Value'"), true)
        assertEquals(context.executeBooleanScript("obj.arrayValue()[0] === 1"), true)
        assertEquals(context.executeBooleanScript("obj.arrayValue()[1] === 2"), true)
        assertEquals(context.executeBooleanScript("obj.arrayValue()[2] === 3"), true)
        assertEquals(context.executeBooleanScript("obj.arrayValue()[3] === 4"), true)
        assertEquals(context.executeBooleanScript("obj.dictValue()['aKey'] === 'aValue'"), true)
        assertEquals(context.executeBooleanScript("obj.nilValue() === undefined"), true)
        assertEquals(context.executeBooleanScript("obj.jsValue()['aKey'] === 'aValue'"), true)
        assertEquals(context.executeBooleanScript("obj.objectValue() instanceof FooObject"), true)
        assertEquals(context.executeBooleanScript("obj.unexportdClassValue() instanceof FooObject"), true)
        assertEquals(context.executeBooleanScript("obj.errorValue().message === 'Error Message.'"), true)
    }

}