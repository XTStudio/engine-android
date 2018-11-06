package com.xt.endo

import android.util.Log
import com.eclipsesource.v8.V8
import com.eclipsesource.v8.V8Array
import com.eclipsesource.v8.V8Object
import com.eclipsesource.v8.utils.V8ObjectUtils
import com.xt.jscore.JSContext

/**
 * Created by cuiminghui on 2018/7/19.
 */

interface EDOLoggerHandler {
    fun onVerbose(arguments: List<String>)
    fun onError(arguments: List<String>)
    fun onWarn(arguments: List<String>)
    fun onInfo(arguments: List<String>)
    fun onDebug(arguments: List<String>)
}

class EDOLogger {

    companion object {

        var sharedHandler: EDOLoggerHandler? = null

        fun attachTo(context: JSContext) {
            this.attachTo(context.runtime)
        }

        fun attachTo(context: V8) {
            val console = V8Object(context)
            console.registerJavaMethod(this, "verbose", "_verbose", arrayOf(V8Array::class.java))
            console.registerJavaMethod(this, "error", "_error", arrayOf(V8Array::class.java))
            console.registerJavaMethod(this, "warn", "_warn", arrayOf(V8Array::class.java))
            console.registerJavaMethod(this, "info", "_info", arrayOf(V8Array::class.java))
            console.registerJavaMethod(this, "debug", "_debug", arrayOf(V8Array::class.java))
            context.add("console", console)
            context.executeScript("function _EDOLogger_ConvertObject(obj) { if (obj === null) { return 'null'; } if (obj === undefined) { return 'undefined'; } return typeof obj === 'object' ? JSON.stringify(obj, undefined, '    ') : obj; } ")
            context.executeScript("(function(){ console.log = function(){ var args = []; for(var i = 0;i < arguments.length;i++){args.push(_EDOLogger_ConvertObject(arguments[i]))}; console._verbose.call(this, args); } })()")
            context.executeScript("(function(){ console.error = function(){ var args = []; for(var i = 0;i < arguments.length;i++){args.push(_EDOLogger_ConvertObject(arguments[i]))}; console._error.call(this, args); } })()")
            context.executeScript("(function(){ console.warn = function(){ var args = []; for(var i = 0;i < arguments.length;i++){args.push(_EDOLogger_ConvertObject(arguments[i]))}; console._warn.call(this, args); } })()")
            context.executeScript("(function(){ console.info = function(){ var args = []; for(var i = 0;i < arguments.length;i++){args.push(_EDOLogger_ConvertObject(arguments[i]))}; console._info.call(this, args); } })()")
            context.executeScript("(function(){ console.debug = function(){ var args = []; for(var i = 0;i < arguments.length;i++){args.push(_EDOLogger_ConvertObject(arguments[i]))}; console._debug.call(this, args); } })()")
        }

        fun verbose(arguments: V8Array) {
            Log.v("EDOLogger", V8ObjectUtils.toList(arguments).joinToString("\n"))
            this.sharedHandler?.onVerbose(V8ObjectUtils.toList(arguments).map { it.toString() })
        }

        fun error(arguments: V8Array) {
            Log.e("EDOLogger", V8ObjectUtils.toList(arguments).joinToString("\n"))
            this.sharedHandler?.onError(V8ObjectUtils.toList(arguments).map { it.toString() })
        }

        fun warn(arguments: V8Array) {
            Log.w("EDOLogger", V8ObjectUtils.toList(arguments).joinToString("\n"))
            this.sharedHandler?.onWarn(V8ObjectUtils.toList(arguments).map { it.toString() })
        }

        fun info(arguments: V8Array) {
            Log.i("EDOLogger", V8ObjectUtils.toList(arguments).joinToString("\n"))
            this.sharedHandler?.onInfo(V8ObjectUtils.toList(arguments).map { it.toString() })
        }

        fun debug(arguments: V8Array) {
            Log.d("EDOLogger", V8ObjectUtils.toList(arguments).joinToString("\n"))
            this.sharedHandler?.onDebug(V8ObjectUtils.toList(arguments).map { it.toString() })
        }

    }

}