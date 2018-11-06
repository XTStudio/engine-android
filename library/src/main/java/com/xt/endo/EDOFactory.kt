package com.xt.endo

import android.app.Activity
import com.xt.jscore.JSContext
import java.lang.Exception

class EDOFactory {

    companion object {

        @JvmStatic fun decodeContextFromAssets(named: String): JSContext {
            val context = JSContext()
            EDOExporter.sharedExporter.exportWithContext(context)
            try {
                EDOExporter.sharedExporter.applicationContext?.assets?.open(named)?.use {
                    val byteArray = ByteArray(it.available())
                    it.read(byteArray, 0, it.available())
                    val script = String(byteArray)
                    context.evaluateScript(script)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return context
        }

        @JvmStatic fun decodeContextFromString(script: String): JSContext {
            val context = JSContext()
            EDOExporter.sharedExporter.exportWithContext(context)
            context.evaluateScript(script)
            return context
        }

        private var sharedDebugger: EDODebugger? = null

        @JvmStatic fun decodeContextFromAssets(named: String, activity: Activity, debuggerAddress: String, onReadyBlock: (context: JSContext) -> Unit): JSContext {
            sharedDebugger = EDODebugger(activity, debuggerAddress)
            sharedDebugger?.connect({
                onReadyBlock(it)
            }, {})
            return this.decodeContextFromAssets(named)
        }

        @JvmStatic fun objectFromContext(context: JSContext, named: String): Any? {
            val value = context[named]
            return EDOObjectTransfer.convertToJavaObjectWithJSValue(value, value)
        }

    }

}