package com.xt.endo

import android.os.Handler
import android.os.StrictMode
import com.eclipsesource.v8.*
import com.eclipsesource.v8.utils.V8ObjectUtils
import com.xt.jscore.JSContext
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.lang.Exception
import java.util.concurrent.TimeUnit

class XTSHttpRequest {

    companion object {

        fun attachTo(context: JSContext) {
            this.attachTo(context.runtime)
        }

        fun attachTo(context: V8) {
            context.registerJavaMethod({ sender, parameters ->
                val handler = Handler()
                StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().permitAll().build())
                val callback = parameters.get(1) as? V8Function
                (parameters.get(0) as? V8Object)?.let {
                    val method = it.getString("method")
                    val url = it.getString("url")
                    val header = it.getString("header")
                    val timeout = it.get("timeout") as? Int ?: 60000
                    val body = it.get("body") as? String ?: ""
                    val async = it.get("async") as? Boolean ?: false
                    val headerBuilder = Headers.Builder()
                    try {
                        val obj = JSONObject(header)
                        obj.keys().forEach {
                            headerBuilder.add(it, obj.optString(it, ""))
                        }
                    } catch (e: Exception) { }
                    val request = Request.Builder()
                            .method(method, if (method == "POST") RequestBody.create(MediaType.parse("text/plain; charset=utf-8"), body) else null)
                            .headers(headerBuilder.build())
                            .url(url)
                            .build()
                    if (!async) {
                        try {
                            val response = OkHttpClient.Builder()
                                    .connectTimeout(timeout.toLong(), TimeUnit.MILLISECONDS)
                                    .readTimeout(timeout.toLong(), TimeUnit.MILLISECONDS)
                                    .writeTimeout(timeout.toLong(), TimeUnit.MILLISECONDS)
                                    .build()
                                    .newCall(request).execute()
                            callback?.call(sender, V8ObjectUtils.toV8Array(context, listOf(response.code(), response.body()?.string() ?: "")))
                        } catch (e: Exception) {
                            callback?.call(sender, V8ObjectUtils.toV8Array(context, listOf(0, "")))
                        }
                    }
                    else {
                        val twinSender = sender.twin()
                        val twinCallback = callback?.twin()
                        OkHttpClient.Builder()
                                .connectTimeout(timeout.toLong(), TimeUnit.MILLISECONDS)
                                .readTimeout(timeout.toLong(), TimeUnit.MILLISECONDS)
                                .writeTimeout(timeout.toLong(), TimeUnit.MILLISECONDS)
                                .build()
                                .newCall(request)
                                .enqueue(object : Callback {
                                    override fun onFailure(call: Call, e: IOException) {
                                        handler.post {
                                            twinCallback?.call(twinSender, V8ObjectUtils.toV8Array(context, listOf(0, "")))
                                            twinSender.release()
                                            twinCallback?.release()
                                        }
                                    }
                                    override fun onResponse(call: Call, response: Response) {
                                        handler.post {
                                            twinCallback?.call(twinSender, V8ObjectUtils.toV8Array(context, listOf(response.code(), response.body()?.string() ?: "")))
                                            twinSender.release()
                                            twinCallback?.release()
                                        }
                                    }
                                })
                    }
                }
            }, "_XTSHttpRequest_send")
            context.executeScript("class XTSHttpRequest {\n" +
                    "\n" +
                    "    constructor() {\n" +
                    "        this.header = {}\n" +
                    "    }\n" +
                    "\n" +
                    "    open(method, url, async) {\n" +
                    "        this.method = method\n" +
                    "        this.url = url\n" +
                    "        this.async = async\n" +
                    "    }\n" +
                    "\n" +
                    "    setRequestHeader(aKey, value) {\n" +
                    "        this.header[aKey] = value\n" +
                    "    }\n" +
                    "\n" +
                    "    send(data) {\n" +
                    "        _XTSHttpRequest_send({ method: this.method, url: this.url, async: this.async, timeout: this.timeout, header: JSON.stringify(this.header), body: data }, (status, responseText) => {\n" +
                    "            this.status = status\n" +
                    "            this.responseText = responseText\n" +
                    "            if (this.onloadend) { this.onloadend(); }" +
                    "        })\n" +
                    "    }\n" +
                    "\n" +
                    "}")
        }

    }

}