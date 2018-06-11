package com.xt.endo

/**
 * Created by cuiminghui on 2018/6/11.
 */

interface EDONativeObject {

    val objectRef: String
        get() {
            return System.identityHashCode(this).toString()
        }

    fun sss() {
        System.out.println(objectRef)
    }

}