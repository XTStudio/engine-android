package com.xt.endo.sample

import android.support.test.runner.AndroidJUnit4
import com.eclipsesource.v8.V8
import com.xt.endo.EDOExporter
import com.xt.endo.EDOJavaHelper
import com.xt.endo.EDOObjectTransfer
import com.xt.jscore.JSContext
import com.xt.jscore.JSValue
import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Created by cuiminghui on 2018/7/19.
 */

@RunWith(AndroidJUnit4::class)
class ObjectTests {

    val context = JSContext()

    init {
        setup()
    }

    fun setup() {
        EDOExporter.sharedExporter.exportWithContext(context)
    }

    @Test
    fun testNewInstance() {
        context.evaluateScript("var obj = new BarObject")
        assertEquals(context.evaluateScript("obj.intValue")?.toInt(), 1)
    }

    @Test
    fun testCustomInstance() {
        context.evaluateScript("var obj = new BarObject(123)")
        assertEquals(context.evaluateScript("obj.intValue")?.toInt(), 123)
    }

    @Test
    fun testSubclassInstance() {
        context.evaluateScript("var obj = new BarObject")
        assertEquals(context.evaluateScript("obj instanceof FooObject")?.toBool(), true)
        assertEquals(context.evaluateScript("obj.floatValue")!!.toDouble(), 0.1, 0.01)
    }

    @Test
    fun testEventEmitter() {
        val value = context.evaluateScript("var testEventEmitter = new FooObject; testEventEmitter") as JSValue
        val obj = EDOObjectTransfer.convertToJavaObjectWithJSValue(value, value) as FooObject
        context.evaluateScript("testEventEmitter.on('click', function(sender){ sender.floatValue = 2.0 })")
        context.evaluateScript("testEventEmitter.on('clickTime', function(){ return 1 })")
        EDOJavaHelper.emit(obj, "click", obj)
        assertEquals(obj.floatValue, 2.0f)
        assertEquals(EDOJavaHelper.value(obj, "clickTime") as Int, 1)
    }

    @Test
    fun testBind() {
        context.evaluateScript("class SSSObject extends BarObject { bindTest(e) { this.intValue = e; } } ; var obj = new SSSObject")
        context.evaluateScript("obj.bindTest(123);")
        assertEquals(context.evaluateScript("obj.intValue")?.toInt(), 123)
    }

    @Test
    fun testStatic() {
        assertEquals(context.evaluateScript("FooObject.staticFoo.floatValue")?.toDouble() as Double, 0.1, 0.01)
    }

}