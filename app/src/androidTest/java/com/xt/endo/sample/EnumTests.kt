package com.xt.endo.sample

import android.support.test.runner.AndroidJUnit4
import com.eclipsesource.v8.V8
import com.xt.endo.EDOExporter
import com.xt.endo.EDOObjectTransfer
import com.xt.jscore.JSContext
import com.xt.jscore.JSValue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/**
 * Created by cuiminghui on 2018/7/19.
 */

enum class KotlinEnum {
    top,
    left,
    bottom,
    right
}

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
        EDOExporter.sharedExporter.exportEnum("KotlinEnum", mapOf(
                Pair("top", KotlinEnum.top),
                Pair("left", KotlinEnum.left),
                Pair("bottom", KotlinEnum.bottom),
                Pair("right", KotlinEnum.right)
        ))
    }

    @Test
    fun testEnum() {
        val context = JSContext()
        EDOExporter.sharedExporter.exportWithContext(context)
        assertEquals(context.evaluateScript("UIViewContentMode.top")?.toInt(), 1)
        assertEquals(context.evaluateScript("UIViewContentMode.left")?.toInt(), 2)
        assertEquals(context.evaluateScript("UIViewContentMode.bottom")?.toInt(), 3)
        assertEquals(context.evaluateScript("UIViewContentMode.right")?.toInt(), 4)
        assertEquals(context.evaluateScript("UIEnumAsString.c")?.toString(), "c")
        val enumJSValue = context.evaluateScript("KotlinEnum.top") as JSValue
        assertEquals(EDOObjectTransfer.convertToJavaObjectWithJSValue(enumJSValue, enumJSValue), KotlinEnum.top)
    }

}