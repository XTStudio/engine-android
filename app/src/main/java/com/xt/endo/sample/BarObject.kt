package com.xt.endo.sample

import com.xt.endo.EDOJavaHelper

/**
 * Created by cuiminghui on 2018/7/19.
 */
open class BarObject: FooObject() {

    var intValue: Int = 1
        set(value) {
            field = value
            EDOJavaHelper.valueChanged(this, "intValue")
        }

    fun bindTest(value: Int) {
        EDOJavaHelper.invokeBindedMethod(this, "bindTest", value)
    }

}