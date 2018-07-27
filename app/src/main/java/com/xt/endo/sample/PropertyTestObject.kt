package com.xt.endo.sample

import com.xt.endo.CGAffineTransform
import com.xt.endo.CGRect
import com.xt.endo.CGSize

enum class TestEnum {
    top,
    left,
    bottom,
    right
}

/**
 * Created by cuiminghui on 2018/7/17.
 */
class PropertyTestObject {

    var intValue: Int = 0
    var floatValue: Float = 0.0f
    var doubleValue: Double = 0.0
    var boolValue: Boolean = false
    var rectValue: CGRect = CGRect(0.0, 0.0, 0.0, 0.0)
    var sizeValue: CGSize = CGSize(0.0, 0.0)
    var affineTransformValue: CGAffineTransform = CGAffineTransform(1.0, 0.0, 0.0, 1.0, 0.0, 0.0)
    var stringValue: String = ""
    var arrayValue: List<Any> = listOf()
    var dictValue: Map<String, Any> = mapOf()
    var nilValue: Any? = null
    var objectValue: FooObject? = null
    var readonlyIntValue: Int = 0
    var enumValue: TestEnum = TestEnum.top

}