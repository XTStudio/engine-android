package com.xt.endo.sample

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.eclipsesource.v8.V8
import com.xt.endo.EDOExporter

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class PropertyTests {

    @Test
    fun testProperties() {
        val context = V8.createV8Runtime()
        EDOExporter.sharedExporter.exportWithContext(context)
        context.executeScript("var obj = new PropertyTestObject")
        context.executeScript("obj.intValue = 2")
        assertTrue(true)
    }

}
