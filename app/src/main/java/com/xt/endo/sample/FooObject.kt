package com.xt.endo.sample

/**
 * Created by cuiminghui on 2018/7/19.
 */
open class FooObject {

    var floatValue: Float = 0.1f

    companion object {

        @JvmStatic var staticValue: Float = 0.2f

        @JvmStatic val staticFoo2: FooObject = FooObject()

    }

}