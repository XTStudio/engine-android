package com.xt.endo

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Handler
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RelativeLayout
import com.xt.jscore.JSContext
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class EDODebugger(val activity: Activity, remoteAddress: String? = null) {

    private var remoteAddress: String = remoteAddress ?: activity.getSharedPreferences("com.xt.engine", Context.MODE_PRIVATE)?.getString("debugger.address", null) ?: "10.0.2.2:8090"
    private var httpClient = OkHttpClient()
    private var closed = false
    private var lastTag: String? = null
    private var currentContext: JSContext? = null
    private var contextInitializer: (() -> JSContext)? = null

    init {
        this.addConsoleHandler()
    }

    fun addConsoleHandler() {
        EDOLogger.sharedHandler = object :EDOLoggerHandler {
            override fun onDebug(arguments: List<String>) {
                try {
                    val bodyObject = JSONObject()
                    bodyObject.put("type", "debug")
                    val bodyArguments = JSONArray()
                    arguments.forEach { bodyArguments.put(it) }
                    bodyObject.put("values", bodyArguments)
                    this@EDODebugger.httpClient.newCall(
                            Request.Builder()
                                    .url("http://$remoteAddress/console")
                                    .post(RequestBody.create(MediaType.parse("text/plain"), bodyObject.toString()))
                                    .build()
                    ).enqueue(object : Callback {
                        override fun onFailure(call: Call?, e: IOException?) { }
                        override fun onResponse(call: Call?, response: Response?) { }
                    })
                } catch (e: Exception) { }
            }
            override fun onError(arguments: List<String>) {
                try {
                    val bodyObject = JSONObject()
                    bodyObject.put("type", "error")
                    val bodyArguments = JSONArray()
                    arguments.forEach { bodyArguments.put(it) }
                    bodyObject.put("values", bodyArguments)
                    this@EDODebugger.httpClient.newCall(
                            Request.Builder()
                                    .url("http://$remoteAddress/console")
                                    .post(RequestBody.create(MediaType.parse("text/plain"), bodyObject.toString()))
                                    .build()
                    ).enqueue(object : Callback {
                        override fun onFailure(call: Call?, e: IOException?) { }
                        override fun onResponse(call: Call?, response: Response?) { }
                    })
                } catch (e: Exception) { }
            }
            override fun onInfo(arguments: List<String>) {
                try {
                    val bodyObject = JSONObject()
                    bodyObject.put("type", "info")
                    val bodyArguments = JSONArray()
                    arguments.forEach { bodyArguments.put(it) }
                    bodyObject.put("values", bodyArguments)
                    this@EDODebugger.httpClient.newCall(
                            Request.Builder()
                                    .url("http://$remoteAddress/console")
                                    .post(RequestBody.create(MediaType.parse("text/plain"), bodyObject.toString()))
                                    .build()
                    ).enqueue(object : Callback {
                        override fun onFailure(call: Call?, e: IOException?) { }
                        override fun onResponse(call: Call?, response: Response?) { }
                    })
                } catch (e: Exception) { }
            }
            override fun onVerbose(arguments: List<String>) {
                try {
                    val bodyObject = JSONObject()
                    bodyObject.put("type", "log")
                    val bodyArguments = JSONArray()
                    arguments.forEach { bodyArguments.put(it) }
                    bodyObject.put("values", bodyArguments)
                    this@EDODebugger.httpClient.newCall(
                            Request.Builder()
                                    .url("http://$remoteAddress/console")
                                    .post(RequestBody.create(MediaType.parse("text/plain"), bodyObject.toString()))
                                    .build()
                    ).enqueue(object : Callback {
                        override fun onFailure(call: Call?, e: IOException?) { }
                        override fun onResponse(call: Call?, response: Response?) { }
                    })
                } catch (e: Exception) { }
            }
            override fun onWarn(arguments: List<String>) {
                try {
                    val bodyObject = JSONObject()
                    bodyObject.put("type", "warn")
                    val bodyArguments = JSONArray()
                    arguments.forEach { bodyArguments.put(it) }
                    bodyObject.put("values", bodyArguments)
                    this@EDODebugger.httpClient.newCall(
                            Request.Builder()
                                    .url("http://$remoteAddress/console")
                                    .post(RequestBody.create(MediaType.parse("text/plain"), bodyObject.toString()))
                                    .build()
                    ).enqueue(object : Callback {
                        override fun onFailure(call: Call?, e: IOException?) { }
                        override fun onResponse(call: Call?, response: Response?) { }
                    })
                } catch (e: Exception) { }
            }
        }
    }

    fun setContextInitializer(value: () -> JSContext) {
        this.contextInitializer = value
    }

    fun connect(callback: (context: JSContext) -> Unit, fallback: () -> Unit) {
        try {
            val clazz = Class.forName(this.activity.packageName + ".BuildConfig")
            val field = clazz.getField("DEBUG")
            if (field.get(clazz) as? Boolean != true) {
                fallback()
                return
            }
        } catch (e: Exception) { }
        val dialog = this.displayConnectingDialog(callback, fallback)
        this.httpClient.newCall(Request.Builder()
                .url("http://$remoteAddress/source")
                .get()
                .build()).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                if (this@EDODebugger.lastTag == null) {
                    return
                }
                Handler(activity.mainLooper).post {
                    dialog.hide()
                    fallback()
                }
            }
            override fun onResponse(call: Call?, response: Response?) {
                if (this@EDODebugger.closed) { return }
                val script = response?.body()?.string() ?: return
                Handler(activity.mainLooper).post {
                    val context = this@EDODebugger.contextInitializer?.invoke() ?: JSContext()
                    this@EDODebugger.currentContext = context
                    EDOExporter.sharedExporter.exportWithContext(context)
                    context.evaluateScript(script)
                    dialog.hide()
                    callback(context)
                    this@EDODebugger.fetchUpdate(callback)
                }
            }
        })
    }

    fun livereload(callback: (context: JSContext) -> Unit, fallback: () -> Unit) {
        try {
            val clazz = Class.forName(this.activity.packageName + ".BuildConfig")
            val field = clazz.getField("DEBUG")
            if (field.get(clazz) as? Boolean != true) {
                fallback()
                return
            }
        } catch (e: Exception) { }
        val dialog = this.displayConnectingDialog(callback, fallback)
        this.httpClient.newCall(Request.Builder()
                .url("http://$remoteAddress/livereload")
                .get()
                .build()).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                if (this@EDODebugger.lastTag == null) {
                    return
                }
                Handler(activity.mainLooper).post {
                    dialog.hide()
                    fallback()
                }
            }
            override fun onResponse(call: Call?, response: Response?) {
                if (this@EDODebugger.closed) { return }
                val script = response?.body()?.string() ?: return
                Handler(activity.mainLooper).post {
                    this@EDODebugger.currentContext?.let {
                        it.evaluateScript(script)
                    }
                    this@EDODebugger.fetchUpdate(callback)
                }
            }
        })
    }

    fun fetchUpdate(callback: (context: JSContext) -> Unit) {
        Handler(activity.mainLooper).postDelayed({
            this.httpClient.newCall(Request.Builder()
                    .url("http://$remoteAddress/version")
                    .get()
                    .build()).enqueue(object : Callback {
                override fun onFailure(call: Call?, e: IOException?) {
                    this@EDODebugger.fetchUpdate(callback)
                }
                override fun onResponse(call: Call?, response: Response?) {
                    if (this@EDODebugger.closed) { return }
                    val tag = response?.body()?.string() ?: return this@EDODebugger.fetchUpdate(callback)
                    if (this@EDODebugger.lastTag == null) {
                        this@EDODebugger.lastTag = tag
                        this@EDODebugger.fetchUpdate(callback)
                    }
                    else if (this@EDODebugger.lastTag != tag) {
                        this@EDODebugger.lastTag = tag
                        if (tag.contains(".reload")) {
                            Handler(activity.mainLooper).post {
                                this@EDODebugger.livereload(callback, {})
                            }
                        }
                        else {
                            Handler(activity.mainLooper).post {
                                this@EDODebugger.connect(callback, {})
                            }
                        }
                    }
                    else {
                        this@EDODebugger.fetchUpdate(callback)
                    }
                }
            })
        }, 500)
    }

    private fun displayConnectingDialog(callback: (context: JSContext) -> Unit, fallback: () -> Unit): AlertDialog {
        val dialog = AlertDialog.Builder(activity)
                .setTitle("XT Debugger")
                .setMessage("connecting to " + this.remoteAddress)
                .setNegativeButton("FORCE CLOSE") { _, _ ->
                    this.closed = true
                    fallback()
                }
                .setPositiveButton("MODIFY") { _, _ ->
                    this.displayModifyDialog(callback, fallback)
                }
                .create()
        dialog.show()
        return dialog
    }

    private fun displayModifyDialog(callback: (context: JSContext) -> Unit, fallback: () -> Unit) {
        val layout = RelativeLayout(activity)
        val editText = EditText(activity)
        editText.hint = "Input IP:Port Here"
        editText.text.append(this.remoteAddress)
        editText.setSingleLine()
        val scale = activity.resources.displayMetrics.density
        layout.setPadding((20 * scale).toInt(), 0, (20 * scale).toInt(), 0)
        layout.addView(editText, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        AlertDialog.Builder(activity)
                .setMessage("Enter XT Debugger Address")
                .setView(layout)
                .setNegativeButton("Cancel") { _, _ ->
                    this.connect(callback, fallback)
                }
                .setPositiveButton("OK") { _, _ ->
                    this.remoteAddress = editText.text.toString()
                    this.connect(callback, fallback)
                }
                .show()
    }

}