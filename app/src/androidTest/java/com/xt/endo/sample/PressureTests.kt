package com.xt.endo.sample

import android.os.SystemClock
import android.support.test.runner.AndroidJUnit4
import com.xt.endo.EDOExporter
import com.xt.jscore.JSContext
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PressureTests {

    val context = JSContext()

    init {
        setup()
    }

    fun setup() {
        EDOExporter.sharedExporter.exportWithContext(context)
    }

    @Test
    fun testRead() {
        val startTime = SystemClock.uptimeMillis()
        context.evaluateScript("var a = new BarObject; for(var i = 0; i < 10000; i++) { a.intValue }")
        val usedTime = SystemClock.uptimeMillis() - startTime
        assert(usedTime < 100)
        context.evaluateScript("a.intValue = 2")
        Assert.assertEquals(context.evaluateScript("a.intValue")!!.toInt(), 2)
    }

    @Test
    fun testStaticRead() {
        val startTime = SystemClock.uptimeMillis()
        context.evaluateScript("for(var i = 0; i < 10000; i++) { FooObject.staticValue }")
        val usedTime = SystemClock.uptimeMillis() - startTime
        assert(usedTime < 100)
        context.evaluateScript("FooObject.staticValue = 0.3")
        Assert.assertEquals(context.evaluateScript("FooObject.staticValue")?.toDouble() as Double, 0.3, 0.01)
    }

}