package com.xt.endo

/**
 * Created by cuiminghui on 2018/6/11.
 */

class EDOExportable {

    val clazz: Class<*>
    val name: String
    val superName: String
    var initializer: ((arguments: List<Object>) -> Any)? = null
    var exportedProps: List<String> = listOf()
    var bindedMethods: List<String> = listOf()
    var exportedMethods: List<String> = listOf()

    constructor(clazz: Class<*>, name: String, superName: String) {
        this.clazz = clazz
        this.name = name
        this.superName = superName
    }

}