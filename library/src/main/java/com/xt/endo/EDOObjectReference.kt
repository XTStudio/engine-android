package com.xt.endo

import com.eclipsesource.v8.V8Object

/**
 * Created by cuiminghui on 2018/6/11.
 */

class EDOObjectReference {

    val value: EDONativeObject
    val metaClassManagedValue: V8Object
    var retainCount: Int = 0

    constructor(value: EDONativeObject, metaClassManagedValue: V8Object) {
        this.value = value
        this.metaClassManagedValue = metaClassManagedValue
    }

}