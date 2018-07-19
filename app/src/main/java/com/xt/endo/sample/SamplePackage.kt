package com.xt.endo.sample

import com.xt.endo.EDOExporter
import com.xt.endo.EDOPackage

/**
 * Created by cuiminghui on 2018/7/17.
 */
class SamplePackage : EDOPackage() {

    private val exporter = EDOExporter.sharedExporter

    override fun install() {
        super.install()
        installPropertyTestObject()
        installObjectTestObjects()
        installReturnTestObject()
        installArgumentTestObject()
    }

    private fun installObjectTestObjects() {
        exporter.exportClass(FooObject::class.java, "FooObject")
        exporter.exportProperty(FooObject::class.java, "floatValue")
        exporter.exportClass(BarObject::class.java, "BarObject", "FooObject")
        exporter.exportProperty(BarObject::class.java, "intValue")
        exporter.exportInitializer(BarObject::class.java, {
            val instance = BarObject()
            if (0 < it.count() && it[0] is Number) {
                instance.intValue = (it[0] as Number).toInt()
            }
            return@exportInitializer instance
        })
        exporter.bindMethodToJavaScript(BarObject::class.java, "bindTest")
    }

    private fun installPropertyTestObject() {
        val clazz = PropertyTestObject::class.java
        exporter.exportClass(clazz, "PropertyTestObject")
        exporter.exportProperty(clazz, "intValue")
        exporter.exportProperty(clazz, "floatValue")
        exporter.exportProperty(clazz, "doubleValue")
        exporter.exportProperty(clazz, "boolValue")
        exporter.exportProperty(clazz, "rectValue")
        exporter.exportProperty(clazz, "sizeValue")
        exporter.exportProperty(clazz, "affineTransformValue")
        exporter.exportProperty(clazz, "stringValue")
        exporter.exportProperty(clazz, "arrayValue")
        exporter.exportProperty(clazz, "dictValue")
        exporter.exportProperty(clazz, "nilValue")
        exporter.exportProperty(clazz, "objectValue")
        exporter.exportProperty(clazz, "readonlyIntValue", true)
    }

    private fun installReturnTestObject() {
        val clazz = ReturnTestObject::class.java
        exporter.exportClass(clazz, "ReturnTestObject")
        exporter.exportMethodToJavaScript(clazz, "intValue")
        exporter.exportMethodToJavaScript(clazz, "floatValue")
        exporter.exportMethodToJavaScript(clazz, "doubleValue")
        exporter.exportMethodToJavaScript(clazz, "boolValue")
        exporter.exportMethodToJavaScript(clazz, "rectValue")
        exporter.exportMethodToJavaScript(clazz, "sizeValue")
        exporter.exportMethodToJavaScript(clazz, "affineTransformValue")
        exporter.exportMethodToJavaScript(clazz, "stringValue")
        exporter.exportMethodToJavaScript(clazz, "arrayValue")
        exporter.exportMethodToJavaScript(clazz, "dictValue")
        exporter.exportMethodToJavaScript(clazz, "nilValue")
        exporter.exportMethodToJavaScript(clazz, "jsValue")
        exporter.exportMethodToJavaScript(clazz, "objectValue")
        exporter.exportMethodToJavaScript(clazz, "unexportdClassValue")
        exporter.exportMethodToJavaScript(clazz, "errorValue")
    }

    private fun installArgumentTestObject() {
        val clazz = ArgumentTestObject::class.java
        exporter.exportClass(clazz, "ArgumentTestObject")
        exporter.exportMethodToJavaScript(clazz, "testIntValue")
        exporter.exportMethodToJavaScript(clazz, "testFloatValue")
        exporter.exportMethodToJavaScript(clazz, "testDoubleValue")
        exporter.exportMethodToJavaScript(clazz, "testBoolValue")
        exporter.exportMethodToJavaScript(clazz, "testRectValue")
        exporter.exportMethodToJavaScript(clazz, "testSizeValue")
        exporter.exportMethodToJavaScript(clazz, "testAffineTransformValue")
        exporter.exportMethodToJavaScript(clazz, "testStringValue")
        exporter.exportMethodToJavaScript(clazz, "testArrayValue")
        exporter.exportMethodToJavaScript(clazz, "testDictValue")
        exporter.exportMethodToJavaScript(clazz, "testNilValue")
        exporter.exportMethodToJavaScript(clazz, "testObjectValue")
        exporter.exportMethodToJavaScript(clazz, "testBlockValue")
    }

}