package com.xt.endo.sample

import android.support.test.runner.AndroidJUnit4
import com.eclipsesource.v8.V8
import com.xt.endo.EDOExporter
import com.xt.endo.EDOJavaHelper
import com.xt.endo.EDOObjectTransfer
import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Created by cuiminghui on 2018/7/19.
 */

@RunWith(AndroidJUnit4::class)
class ObjectTests {

    val context = V8.createV8Runtime()

    init {
        setup()
    }

    fun setup() {
        EDOExporter.sharedExporter.exportWithContext(context)
    }

    @Test
    fun testNewInstance() {
        context.executeScript("var obj = new BarObject")
        assertEquals(context.executeIntegerScript("obj.intValue"), 1)
    }

    @Test
    fun testCustomInstance() {
        context.executeScript("var obj = new BarObject(123)")
        assertEquals(context.executeIntegerScript("obj.intValue"), 123)
    }

    @Test
    fun testSubclassInstance() {
        context.executeScript("var obj = new BarObject")
        assertEquals(context.executeBooleanScript("obj instanceof FooObject"), true)
        assertEquals(context.executeDoubleScript("obj.floatValue"), 0.1, 0.01)
    }

    @Test
    fun testEventEmitter() {
        val v8Object = context.executeObjectScript("var testEventEmitter = new FooObject; testEventEmitter")
        val obj = EDOObjectTransfer.convertToJavaObjectWithJSValue(v8Object, v8Object) as FooObject
        context.executeScript("testEventEmitter.on('click', function(sender){ sender.floatValue = 2.0 })")
        context.executeScript("testEventEmitter.on('clickTime', function(){ return 1 })")
        EDOJavaHelper.emit(obj, "click", obj)
        assertEquals(obj.floatValue, 2.0f)
        assertEquals(EDOJavaHelper.value(obj, "clickTime") as Int, 1)
    }

    @Test
    fun testBind() {
        context.executeScript("class SSSObject extends BarObject { bindTest(e) { this.intValue = e; } } ; var obj = new SSSObject")
        context.executeScript("obj.bindTest(123);")
        assertEquals(context.executeIntegerScript("obj.intValue"), 123)
    }

}