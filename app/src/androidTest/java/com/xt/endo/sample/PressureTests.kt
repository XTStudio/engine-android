package com.xt.endo.sample

import android.os.SystemClock
import android.support.test.runner.AndroidJUnit4
import com.xt.endo.CGRect
import com.xt.endo.EDOExporter
import com.xt.endo.EDOObjectTransfer
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
        Assert.assertTrue(usedTime < 100)
        context.evaluateScript("a.intValue = 2")
        Assert.assertEquals(context.evaluateScript("a.intValue")!!.toInt(), 2)
    }

    @Test
    fun testStaticRead() {
        val startTime = SystemClock.uptimeMillis()
        context.evaluateScript("for(var i = 0; i < 10000; i++) { FooObject.staticValue }")
        val usedTime = SystemClock.uptimeMillis() - startTime
        Assert.assertTrue(usedTime < 100)
        context.evaluateScript("FooObject.staticValue = 0.3")
        Assert.assertEquals(context.evaluateScript("FooObject.staticValue")?.toDouble() as Double, 0.3, 0.01)
        FooObject.staticValue = 0.4f
        Assert.assertEquals(context.evaluateScript("FooObject.staticValue")?.toDouble() as Double, 0.4, 0.01)
    }

    @Test
    fun testWrite() {
        val startTime = SystemClock.uptimeMillis()
        context.evaluateScript("var a = new BarObject; for(var i = 0; i < 10000; i++) { a.intValue = 233 }")
        val usedTime = SystemClock.uptimeMillis() - startTime
        Assert.assertTrue(usedTime < 100)
    }

    @Test
    fun testStaticWrite() {
        val startTime = SystemClock.uptimeMillis()
        context.evaluateScript("for(var i = 0; i < 10000; i++) { FooObject.staticValue = 233 }")
        val usedTime = SystemClock.uptimeMillis() - startTime
        Assert.assertTrue(usedTime < 100)
    }

    @Test
    fun testValueCache() {
        context.evaluateScript("var b = new PropertyTestObject")
        val bValue: PropertyTestObject = kotlin.run {
            val b = context.evaluateScript("b")
            return@run EDOObjectTransfer.convertToJavaObjectWithJSValue(b!!, b!!) as PropertyTestObject
        }
        Assert.assertTrue(context.evaluateScript("b.rectValue.x == 0.0")?.toBool()!!)
        bValue.rectValue = CGRect(2.0, 3.0, 4.0, 5.0)
        Assert.assertTrue(context.evaluateScript("b.rectValue.x == 2.0")?.toBool()!!)
        context.evaluateScript("b.rectValue = {x: 3.0, y: 4.0, width: 5.0, height: 6.0}")
        Assert.assertEquals(bValue.rectValue.x, 3.0, 0.1)
    }

}