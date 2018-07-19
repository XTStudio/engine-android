package com.xt.endo.sample

import android.support.test.runner.AndroidJUnit4
import com.eclipsesource.v8.V8
import com.xt.endo.EDOExporter
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/**
 * Created by cuiminghui on 2018/7/19.
 */
@RunWith(AndroidJUnit4::class)
class EnumTests {

    init {
        EDOExporter.sharedExporter.exportEnum("UIViewContentMode", mapOf(
                Pair("top", 1),
                Pair("left", 2),
                Pair("bottom", 3),
                Pair("right", 4)
        ))
        EDOExporter.sharedExporter.exportEnum("UIEnumAsString", mapOf(
                Pair("a", "a"),
                Pair("b", "b"),
                Pair("c", "c"),
                Pair("d", "d")
        ))
    }

    @Test
    fun testEnum() {
        val context = V8.createV8Runtime()
        EDOExporter.sharedExporter.exportWithContext(context)
        assertEquals(context.executeIntegerScript("UIViewContentMode.top"), 1)
        assertEquals(context.executeIntegerScript("UIViewContentMode.left"), 2)
        assertEquals(context.executeIntegerScript("UIViewContentMode.bottom"), 3)
        assertEquals(context.executeIntegerScript("UIViewContentMode.right"), 4)
        assertEquals(context.executeStringScript("UIEnumAsString.c"), "c")
    }

}