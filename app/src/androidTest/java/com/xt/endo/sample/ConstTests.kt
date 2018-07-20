package com.xt.endo.sample

import android.support.test.runner.AndroidJUnit4
import com.eclipsesource.v8.V8
import com.xt.endo.EDOExporter
import com.xt.jscore.JSContext
import org.junit.runner.RunWith
import org.junit.Assert.*
import org.junit.Test

/**
 * Created by cuiminghui on 2018/7/19.
 */
@RunWith(AndroidJUnit4::class)
class ConstTests {

    init {
        EDOExporter.sharedExporter.exportConst("kTestConst", "const value")
        EDOExporter.sharedExporter.exportConst("kTestNumberConst", 123)
        EDOExporter.sharedExporter.exportConst("kTestObjectConst", FooObject())
    }

    @Test
    fun testConsts() {
        val context = JSContext()
        EDOExporter.sharedExporter.exportWithContext(context)
        assertEquals(context.evaluateScript("kTestConst")?.toString(), "const value")
        assertEquals(context.evaluateScript("kTestNumberConst")?.toInt(), 123)
        assertTrue(context.evaluateScript("kTestObjectConst instanceof FooObject")?.toBool() ?: false)
    }

}