package com.xt.endo

import android.content.ComponentCallbacks
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import com.eclipsesource.v8.V8
import com.eclipsesource.v8.V8Array
import com.eclipsesource.v8.V8Object
import com.eclipsesource.v8.V8Value
import com.xt.jscore.JSContext
import com.xt.uulog.UULog
import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 * Created by cuiminghui on 2018/6/8.
 */

class EDOExporter {

    var applicationContext: Context? = null
        private set(value) {
            if (field != null) { return }
            field = value
            value?.registerComponentCallbacks(object : ComponentCallbacks {
                override fun onLowMemory() {
                    this@EDOExporter.activeContexts.filter { !it.runtime.isReleased }.forEach { it.runtime.lowMemoryNotification() }
                }
                override fun onConfigurationChanged(newConfig: Configuration?) { }
            })
        }

    fun initializer(applicationContext: Context) {
        if (this.applicationContext == null) {
            this.applicationContext = applicationContext
            this.loadPackages()
        }
    }

    private fun loadPackages() {
        var applicationContext = this.applicationContext ?: return
        val applicationInfo = applicationContext.packageManager.getApplicationInfo(applicationContext.packageName, PackageManager.GET_META_DATA)
        val metaData = applicationInfo.metaData
        metaData.keySet().filter { metaData[it] == "EDOPackage" }.forEach { pkgName ->
            try {
                val clazz = Class.forName(pkgName)
                val instance = clazz.getConstructor().newInstance() as? EDOPackage ?: return@forEach
                instance.install()
            } catch (e: Exception) {}
        }
    }

    private val activeContexts: MutableSet<JSContext> = mutableSetOf()
    internal var exportables: Map<String, EDOExportable> = mapOf()
        private set
    private var exportedKeys: Set<String> = setOf()
    private var exportedKeysCache2: MutableMap<String, Boolean> = mutableMapOf()
    private var exportedConstants: Map<String, Any> = mapOf()
    private var methodsCache: MutableMap<String, Method> = mutableMapOf()
    private var fieldsCache: MutableMap<String, Field> = mutableMapOf()
    private var typesCache: MutableMap<String, Class<*>> = mutableMapOf()
    private var fastRejects: MutableMap<String, Boolean> = mutableMapOf()

    fun exportWithContext(context: JSContext) {
        try {
            if (context.runtime.isReleased) { return }
            this.activeContexts.removeAll { it.runtime.isReleased }
            UULog.attachTo(context)
            this.activeContexts.add(context)
            var script = "var __EDO_SUPERCLASS_TOKEN = '__EDO_SUPERCLASS_TOKEN__';"
            script += "var __extends=this&&this.__extends||function(){var extendStatics=function(d,b){extendStatics=Object.setPrototypeOf||{__proto__:[]}instanceof Array&&function(d,b){d.__proto__=b}||function(d,b){for(var p in b)if(b.hasOwnProperty(p))d[p]=b[p]};return extendStatics(d,b)};return function(d,b){extendStatics(d,b);function __(){this.constructor=d}d.prototype=b===null?Object.create(b):(__.prototype=b.prototype,new __)}}();(function(exports){\"use strict\";function EventEmitter(){}var proto=EventEmitter.prototype;var originalGlobalValue=exports.EventEmitter;function indexOfListener(listeners,listener){var i=listeners.length;while(i--){if(listeners[i].listener===listener){return i}}return-1}function alias(name){return function aliasClosure(){return this[name].apply(this,arguments)}}proto.getListeners=function getListeners(evt){var events=this._getEvents();var response;var key;if(evt instanceof RegExp){response={};for(key in events){if(events.hasOwnProperty(key)&&evt.test(key)){response[key]=events[key]}}}else{response=events[evt]||(events[evt]=[])}return response};proto.flattenListeners=function flattenListeners(listeners){var flatListeners=[];var i;for(i=0;i<listeners.length;i+=1){flatListeners.push(listeners[i].listener)}return flatListeners};proto.getListenersAsObject=function getListenersAsObject(evt){var listeners=this.getListeners(evt);var response;if(listeners instanceof Array){response={};response[evt]=listeners}return response||listeners};function isValidListener(listener){if(typeof listener===\"function\"||listener instanceof RegExp){return true}else if(listener&&typeof listener===\"object\"){return isValidListener(listener.listener)}else{return false}}proto.addListener=function addListener(evt,listener){if(!isValidListener(listener)){throw new TypeError(\"listener must be a function\")}var listeners=this.getListenersAsObject(evt);var listenerIsWrapped=typeof listener===\"object\";var key;for(key in listeners){if(listeners.hasOwnProperty(key)&&indexOfListener(listeners[key],listener)===-1){listeners[key].push(listenerIsWrapped?listener:{listener:listener,once:false})}}ENDO.addListenerWithNameOwner(evt,this);return this};proto.on=alias(\"addListener\");proto.addOnceListener=function addOnceListener(evt,listener){return this.addListener(evt,{listener:listener,once:true})};proto.once=alias(\"addOnceListener\");proto.defineEvent=function defineEvent(evt){this.getListeners(evt);return this};proto.defineEvents=function defineEvents(evts){for(var i=0;i<evts.length;i+=1){this.defineEvent(evts[i])}return this};proto.removeListener=function removeListener(evt,listener){var listeners=this.getListenersAsObject(evt);var index;var key;for(key in listeners){if(listeners.hasOwnProperty(key)){index=indexOfListener(listeners[key],listener);if(index!==-1){listeners[key].splice(index,1)}}}return this};proto.off=alias(\"removeListener\");proto.addListeners=function addListeners(evt,listeners){return this.manipulateListeners(false,evt,listeners)};proto.removeListeners=function removeListeners(evt,listeners){return this.manipulateListeners(true,evt,listeners)};proto.manipulateListeners=function manipulateListeners(remove,evt,listeners){var i;var value;var single=remove?this.removeListener:this.addListener;var multiple=remove?this.removeListeners:this.addListeners;if(typeof evt===\"object\"&&!(evt instanceof RegExp)){for(i in evt){if(evt.hasOwnProperty(i)&&(value=evt[i])){if(typeof value===\"function\"){single.call(this,i,value)}else{multiple.call(this,i,value)}}}}else{i=listeners.length;while(i--){single.call(this,evt,listeners[i])}}return this};proto.removeEvent=function removeEvent(evt){var type=typeof evt;var events=this._getEvents();var key;if(type===\"string\"){delete events[evt]}else if(evt instanceof RegExp){for(key in events){if(events.hasOwnProperty(key)&&evt.test(key)){delete events[key]}}}else{delete this._events}return this};proto.removeAllListeners=alias(\"removeEvent\");proto.emitEvent=function emitEvent(evt,args){var listenersMap=this.getListenersAsObject(evt);var listeners;var listener;var i;var key;var response;for(key in listenersMap){if(listenersMap.hasOwnProperty(key)){listeners=listenersMap[key].slice(0);for(i=0;i<listeners.length;i++){listener=listeners[i];if(listener.once===true){this.removeListener(evt,listener.listener)}response=listener.listener.apply(this,args||[]);if(response===this._getOnceReturnValue()){this.removeListener(evt,listener.listener)}}}}return this};proto.val=function emitEventWithReturnValue(evt){var args=Array.prototype.slice.call(arguments,1);var listenersMap=this.getListenersAsObject(evt);var listeners;var listener;var i;var key;for(key in listenersMap){if(listenersMap.hasOwnProperty(key)){listeners=listenersMap[key].slice(0);for(i=0;i<listeners.length;i++){listener=listeners[i];if(listener.once===true){this.removeListener(evt,listener.listener)}return listener.listener.apply(this,args||[])}}}return undefined};proto.trigger=alias(\"emitEvent\");proto.emit=function emit(evt){var args=Array.prototype.slice.call(arguments,1);return this.emitEvent(evt,args)};proto.setOnceReturnValue=function setOnceReturnValue(value){this._onceReturnValue=value;return this};proto._getOnceReturnValue=function _getOnceReturnValue(){if(this.hasOwnProperty(\"_onceReturnValue\")){return this._onceReturnValue}else{return true}};proto._getEvents=function _getEvents(){return this._events||(this._events={})};exports.EventEmitter=EventEmitter})(this||{});var _EDO_valueMaps=new WeakMap;var _EDO_MetaClass=function(){function _EDO_MetaClass(classname,objectRef){this.classname=classname;this.objectRef=objectRef}return _EDO_MetaClass}();var _EDO_Callback=function(){function _EDO_Callback(func){this.func=func;this._meta_class={classname:\"__Function\"}}return _EDO_Callback}();var EDOObject=function(_super){__extends(EDOObject,_super);function EDOObject(){var _this=_super!==null&&_super.apply(this,arguments)||this;_this.__callbacks=[];return _this}EDOObject.prototype.__convertToJSValue=function(parameter){if(typeof parameter===\"function\"){var callback=new _EDO_Callback(parameter);this.__callbacks.push(callback);callback._meta_class.idx=this.__callbacks.length-1;return callback}if(parameter===null){return undefined}return parameter};EDOObject.prototype.__invokeCallback=function(idx,args){if(this.__callbacks[idx]){return this.__callbacks[idx].func.apply(this,args)}};EDOObject.__checkCachedValueEqualTo=function(instance,propName,newValue){var valueMap=_EDO_valueMaps.get(instance);if(valueMap!==undefined&&valueMap[propName]!==undefined){var cachedValue=valueMap[propName].value;if(cachedValue===newValue){return true}else if(cachedValue instanceof Object&&newValue instanceof Object){var result=true;for(var key in cachedValue){if(typeof cachedValue[key]===\"number\"&&typeof newValue[key]===\"number\"){if(cachedValue[key]!==newValue[key]&&Math.abs(cachedValue[key]-newValue[key])>.01){result=false;break}}else if(typeof cachedValue[key]===\"string\"){if(cachedValue[key]!==newValue[key]){result=false;break}}else if(typeof cachedValue[key]===\"boolean\"){if(cachedValue[key]!==newValue[key]){result=false;break}}else{result=false;break}}return result}}return false};EDOObject.prototype.__clearValueCache=function(propName){var valueMap=_EDO_valueMaps.get(this);if(valueMap!==undefined){delete valueMap[propName]}};return EDOObject}(EventEmitter);"
            val exportables = this.exportables.toMutableMap()
            val exported: MutableList<String> = mutableListOf()
            exported.add("EDOObject")
            var exportingLoopCount: Int
            while (exportables.count() > 0) {
                exportingLoopCount = 0
                exportables.toMap().forEach {
                    if (it.value.superName == "ENUM") {
                        script += it.value.exportedScripts.firstOrNull() ?: ""
                        exported.add(it.value.name)
                        exportables.remove(it.key)
                        exportingLoopCount++
                        return@forEach
                    }
                    if (!exported.contains(it.value.superName)) {
                        return@forEach
                    }
                    val constructorScript = "function Initializer(isParent){var _this = _super.call(this, __EDO_SUPERCLASS_TOKEN) || this;if(arguments[0]instanceof _EDO_MetaClass){_this._meta_class=arguments[0]}else if(isParent !== __EDO_SUPERCLASS_TOKEN){var args=[];for(var key in arguments){args.push(_this.__convertToJSValue(arguments[key]))}_this._meta_class=ENDO.createInstanceWithNameArgumentsOwner(\"${it.key}\",args,_this)}return _this;}"
                    val propsScript = it.value.exportedProps.map { propName ->
                        val trimmedPropName = propName.replace("edo_", "").replace("s.", "")
                        if (propName.startsWith("s.")) {
                            return@map "Object.defineProperty(Initializer,'$trimmedPropName',{get:function(){${kotlin.run {
                                if (it.value.allowGetterCacheProps.contains(propName)) {
                                    return@run "{ let valueMap = _EDO_valueMaps.get(Initializer); if (valueMap && valueMap['$trimmedPropName'] !== undefined) { return valueMap['$trimmedPropName'].value } else { let value = ENDO.valueWithPropertyNameOwner('$propName',{clazz: '${it.value.clazz.name}'}); if (valueMap === undefined) { valueMap = {}; _EDO_valueMaps.set(Initializer, valueMap); } valueMap['$trimmedPropName'] = {value: value}; return value } }"
                                }
                                else {
                                    return@run "return ENDO.valueWithPropertyNameOwner('$propName',{clazz: '${it.value.clazz.name}'})"
                                }
                            }}},set:function(value){${kotlin.run {
                                if (it.value.readonlyProps.contains(propName)) {
                                    return@run ";"
                                }
                                else {
                                    return@run "${kotlin.run {
                                        if (it.value.allowSetterCacheProps.contains(propName)) {
                                            return@run "if (EDOObject.__checkCachedValueEqualTo(Initializer, '$trimmedPropName', value)){ return; }"
                                        }
                                        else {
                                            return@run ""
                                        }
                                    }};ENDO.setValueWithPropertyNameValueOwner('$propName',value === null ? undefined : value,{clazz: '${it.value.clazz.name}'});${kotlin.run {
                                        if (it.value.allowSetterCacheProps.contains(propName)) {
                                            return@run "{ let valueMap = _EDO_valueMaps.get(Initializer); if (valueMap === undefined) { valueMap = {}; _EDO_valueMaps.set(Initializer, valueMap); } valueMap['$trimmedPropName'] = {value: value}; }"
                                        }
                                        else if (it.value.allowGetterCacheProps.contains(propName)) {
                                            return@run "{ let valueMap = _EDO_valueMaps.get(Initializer); if (valueMap !== undefined) { delete valueMap['$trimmedPropName']; } }"
                                        }
                                        else {
                                            return@run ""
                                        }
                                    }}"
                                }
                            }}},enumerable:false,configurable:true});"
                        }
                        return@map "Object.defineProperty(Initializer.prototype,'$trimmedPropName',{get:function(){${kotlin.run {
                            if (it.value.allowGetterCacheProps.contains(propName)) {
                                return@run "{ let valueMap = _EDO_valueMaps.get(this); if (valueMap && valueMap['$trimmedPropName'] !== undefined) { return valueMap['$trimmedPropName'].value } else { let value = ENDO.valueWithPropertyNameOwner('$propName',this); if (valueMap === undefined) { valueMap = {}; _EDO_valueMaps.set(this, valueMap); } valueMap['$trimmedPropName'] = {value: value}; return value } }"
                            }
                            else {
                                return@run "return ENDO.valueWithPropertyNameOwner('$propName',this)"
                            }
                        }}},set:function(value){${kotlin.run {
                            if (it.value.readonlyProps.contains(propName)) {
                                return@run ";"
                            }
                            else {
                                return@run "${kotlin.run {
                                    if (it.value.allowSetterCacheProps.contains(propName)) {
                                        return@run "if (EDOObject.__checkCachedValueEqualTo(this, '$trimmedPropName', value)){ return; }"
                                    }
                                    else {
                                        return@run ""
                                    }
                                }};ENDO.setValueWithPropertyNameValueOwner('$propName',this.__convertToJSValue(value),this);${kotlin.run {
                                    if (it.value.allowSetterCacheProps.contains(propName)) {
                                        return@run "{ let valueMap = _EDO_valueMaps.get(this); if (valueMap === undefined) { valueMap = {}; _EDO_valueMaps.set(this, valueMap); } valueMap['$trimmedPropName'] = {value: value}; }"
                                    }
                                    else if (it.value.allowGetterCacheProps.contains(propName)) {
                                        return@run "{ let valueMap = _EDO_valueMaps.get(this); if (valueMap !== undefined) { delete valueMap['$trimmedPropName']; } }"
                                    }
                                    else {
                                        return@run ""
                                    }
                                }}"
                            }
                        }}},enumerable:false,configurable:true});"
                    }.joinToString(";")
                    val bindMethodScript = it.value.bindedMethods.map {
                        return@map "Initializer.prototype.$it=function(){};Initializer.prototype.__$it=function(){this.$it.apply(this,arguments)};"
                    }.joinToString(";")
                    val methodScript = it.value.exportedMethods.map {
                        return@map "Initializer.prototype.${it.value.replace("edo_", "")} = function () {var args=[];for(var key in arguments){args.push(this.__convertToJSValue(arguments[key]))}return ENDO.callMethodWithNameArgumentsOwner(\"${it.key}\", args, this);};"
                    }.joinToString(";")
                    val innerScript = it.value.innerScripts.map {
                        return@map ";$it;"
                    }.joinToString(";")
                    val appendScript = it.value.exportedScripts.map {
                        return@map ";$it;"
                    }.joinToString(";")
                    val clazzScript = ";var ${it.key} = /** @class */ (function (_super) {;__extends(Initializer, _super) ;$constructorScript; $propsScript ;$bindMethodScript ;$methodScript; $innerScript;return Initializer; }(${it.value.superName}));$appendScript;"
                    script += clazzScript
                    exported.add(it.value.name)
                    exportables.remove(it.key)
                    exportingLoopCount++
                }
                assert(exportingLoopCount > 0, { return@assert "Did you forgot to export some class superClass?" })
            }
            val endoV8Object = V8Object(context.runtime)
            endoV8Object.registerJavaMethod(this, "createInstance", "createInstanceWithNameArgumentsOwner", arrayOf(String::class.java, V8Array::class.java, V8Object::class.java))
            endoV8Object.registerJavaMethod(this, "valueWithPropertyName", "valueWithPropertyNameOwner", arrayOf(String::class.java, V8Object::class.java))
            endoV8Object.registerJavaMethod(this, "setValueWithPropertyName", "setValueWithPropertyNameValueOwner", arrayOf(String::class.java, Object::class.java, V8Object::class.java))
            endoV8Object.registerJavaMethod(this, "callMethodWithName", "callMethodWithNameArgumentsOwner", arrayOf(String::class.java, V8Array::class.java, V8Object::class.java))
            endoV8Object.registerJavaMethod(this, "addListenerWithName", "addListenerWithNameOwner", arrayOf(String::class.java, V8Object::class.java))
            context.runtime.add("ENDO", endoV8Object)
            try {
                context.runtime.executeScript(script)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            this.exportedConstants.forEach {
                val value = EDOObjectTransfer.convertToJSValueWithJavaValue(it.value, context.runtime)
                (value as? Int)?.let { value -> context.runtime.add(it.key, value) }
                (value as? Double)?.let { value -> context.runtime.add(it.key, value) }
                (value as? String)?.let { value -> context.runtime.add(it.key, value) }
                (value as? Boolean)?.let { value -> context.runtime.add(it.key, value) }
                (value as? V8Value)?.let { value -> context.runtime.add(it.key, value) }
            }
            endoV8Object.release()
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun exportClass(clazz: Class<*>, name: String, superName: String = "EDOObject") {
        val exportable = EDOExportable(clazz, name, superName)
        this.exportables = kotlin.run {
            val mutable = this.exportables.toMutableMap()
            mutable[name] = exportable
            return@run mutable.toMap()
        }
    }

    fun exportInitializer(clazz: Class<*>, initializer: (arguments: List<*>) -> Any?) {
        this.exportables.filter { it.value.clazz == clazz }.forEach {
            it.value.initializer = initializer
        }
    }

    fun exportProperty(clazz: Class<*>, propName: String, readonly: Boolean = false, allowGetterCache: Boolean = false, allowSetterCache: Boolean = false) {
        this.exportables.filter { it.value.clazz == clazz }.forEach {
            if (it.value.exportedProps.contains(propName) || it.value.readonlyProps.contains(propName)) { return@forEach }
            it.value.exportedProps = kotlin.run {
                val mutable = it.value.exportedProps.toMutableList()
                mutable.add(propName)
                return@run mutable.toList()
            }
            if (readonly) {
                it.value.readonlyProps = kotlin.run {
                    val mutable = it.value.readonlyProps.toMutableList()
                    mutable.add(propName)
                    return@run mutable.toList()
                }
            }
            if (allowGetterCache) {
                it.value.allowGetterCacheProps = kotlin.run {
                    val mutable = it.value.allowGetterCacheProps.toMutableList()
                    mutable.add(propName)
                    return@run mutable.toList()
                }
            }
            if (allowSetterCache) {
                it.value.allowSetterCacheProps = kotlin.run {
                    val mutable = it.value.allowSetterCacheProps.toMutableList()
                    mutable.add(propName)
                    return@run mutable.toList()
                }
            }
        }
        this.exportedKeys = kotlin.run {
            val mutable = this.exportedKeys.toMutableSet()
            mutable.add("${clazz.name}.$propName")
            return@run mutable.toSet()
        }
    }

    fun exportStaticProperty(clazz: Class<*>, propName: String, readonly: Boolean = false, allowGetterCache: Boolean = false, allowSetterCache: Boolean = false) {
        this.exportProperty(clazz, "s.$propName", readonly, allowGetterCache, allowSetterCache)
    }

    fun exportScript(clazz: Class<*>, script: String, isInnerScript: Boolean = true) {
        if (isInnerScript) {
            this.exportables.filter { it.value.clazz == clazz }.forEach {
                it.value.innerScripts = kotlin.run {
                    val mutable = it.value.innerScripts.toMutableList()
                    mutable.add(script)
                    return@run mutable.toList()
                }
            }
        }
        else {
            this.exportables.filter { it.value.clazz == clazz }.forEach {
                it.value.exportedScripts = kotlin.run {
                    val mutable = it.value.exportedScripts.toMutableList()
                    mutable.add(script)
                    return@run mutable.toList()
                }
            }
        }
    }

    fun bindMethodToJavaScript(clazz: Class<*>, methodName: String) {
        this.exportables.filter { it.value.clazz == clazz }.forEach {
            if (it.value.bindedMethods.contains(methodName)) { return@forEach }
            it.value.bindedMethods = kotlin.run {
                val mutable = it.value.bindedMethods.toMutableList()
                mutable.add(methodName)
                return@run mutable.toList()
            }
        }
    }

    fun exportMethodToJavaScript(clazz: Class<*>, methodName: String, aliasName: String = methodName) {
        this.exportables.filter { it.value.clazz == clazz }.forEach {
            if (it.value.exportedMethods.contains(methodName)) { return@forEach }
            it.value.exportedMethods = kotlin.run {
                val mutable = it.value.exportedMethods.toMutableMap()
                mutable[methodName] = aliasName
                return@run mutable.toMap()
            }
        }
        this.exportedKeys = kotlin.run {
            val mutable = this.exportedKeys.toMutableSet()
            mutable.add("${clazz.name}.($methodName)")
            return@run mutable.toSet()
        }
    }

    fun exportEnum(name: String, values: Map<String, Any>) {
        val exportable = EDOExportable(Any::class.java, name, "ENUM")
        if (values.values.firstOrNull() is Enum<*>) {
            exportable.exportedScripts = listOf(
                    "var $name = {}; ${
                    values.map {
                        return@map "$name.${it.key} = {_meta_class: {classname: \"__KTENUM\", clazz: \"${it.value::class.java.name}\", value: \"${(it.value as Enum<*>).name}\"}};"
                    }.joinToString("")
                    }"
            )
        }
        else {
            exportable.exportedScripts = listOf(
                    "var $name;(function ($name) {${
                    values.map {
                        if (it.value is Number) {
                            return@map "$name[$name[\"${it.key}\"] = ${it.value}] = \"${it.key}\";"
                        }
                        else if (it.value is String) {
                            return@map "$name[$name[\"${it.key}\"] = \"${it.value}\"] = \"${it.key}\";"
                        }
                        else if (it.value is Enum<*>) {
                            val e = "$name[$name[\"${it.key}\"] = {_meta_class: {classname: \"__KTENUM\", clazz: \"${it.value::class.java.name}\", value: \"${(it.value as Enum<*>).name}\"}}] = \"${it.key}\";"
                            return@map "$name[$name[\"${it.key}\"] = {_meta_class: {classname: \"__KTENUM\", clazz: \"${it.value::class.java.name}\", value: \"${(it.value as Enum<*>).name}\"}}] = \"${it.key}\";"
                        }
                        return@map ""
                    }.joinToString(";")
                    }})($name || ($name = {}));"
            )
        }
        this.exportables = kotlin.run {
            val mutable = this.exportables.toMutableMap()
            mutable[name] = exportable
            return@run mutable.toMap()
        }
    }

    fun exportConst(name: String, value: Any) {
        this.exportedConstants = kotlin.run {
            val exportedConstants = this.exportedConstants.toMutableMap()
            exportedConstants[name] = value
            return@run exportedConstants.toMap()
        }
    }

    fun createInstance(name: String, arguments: V8Array, owner: V8Object): V8Value {
        val context = this.contextWithRuntime(owner.runtime) ?: return V8.getUndefined()
        this.exportables[name]?.let { exportable ->
            val newInstance = exportable.initializer?.let { it(EDOObjectTransfer.convertToJavaListWithJSArray(arguments, owner)) } ?: kotlin.run {
                return@run try { exportable.clazz.getDeclaredConstructor().newInstance() } catch (e: Exception) { null }
            } ?: return V8.getUndefined()
            val twinOwner = owner.twin()
            context.handler.post {
                twinOwner.registerJavaMethod(newInstance, "toString", "__mockToString", null)
                twinOwner.release()
            }
            EDOV8ExtRuntime.extRuntime(context).storeScriptObject(newInstance, owner)
            return EDOV8ExtRuntime.extRuntime(context).createMetaClass(newInstance)
        }
        return V8.getUndefined()
    }

    fun valueWithPropertyName(name: String, owner: V8Object): Any {
        if (name.startsWith("s.")) {
            return try {
                val clazz = Class.forName(owner.getString("clazz"))
                if (!this.checkExported(clazz, name)) { return V8.getUndefined() }
                val field = clazz.getDeclaredField(name.replace("s.", ""))
                field.isAccessible = true
                val returnValue = field.get(clazz)
                EDOObjectTransfer.convertToJSValueWithJavaValue(returnValue, owner.runtime)
            } catch (e: Exception) {
                V8.getUndefined()
            }
        }
        EDOObjectTransfer.convertToJavaObjectWithJSValue(owner, owner)?.let { ownerObject ->
            if (!this.checkExported(ownerObject::class.java, name)) { return V8.getUndefined() }
            val ownerClassName = ownerObject::class.java.name
            var fKey = "$ownerClassName.$name.[getValue_0]"
            kotlin.run {
                if (fastRejects[fKey] == true) { return@run }
                try {
                    val method = methodsCache[fKey] ?: ownerObject::class.java.getMethod("get" + name.substring(0, 1).toUpperCase() + name.substring(1))
                    methodsCache[fKey] = method
                    val returnValue = method.invoke(ownerObject)
                    return EDOObjectTransfer.convertToJSValueWithJavaValue(returnValue, owner.runtime)
                }
                catch (e: NoSuchMethodException) {
                    fastRejects[fKey] = true
                }
                catch (e: Exception) { }
            }
            fKey = "$ownerClassName.$name.[getValue_1]"
            kotlin.run {
                if (fastRejects[fKey] == true) { return@run }
                try {
                    val field = fieldsCache[fKey] ?: ownerObject::class.java.getField("m" + name.substring(0, 1).toUpperCase() + name.substring(1))
                    fieldsCache[fKey] = field
                    val returnValue = field.get(ownerObject)
                    return EDOObjectTransfer.convertToJSValueWithJavaValue(returnValue, owner.runtime)
                }
                catch (e: NoSuchFieldException) {
                    fastRejects[fKey] = true
                }
                catch (e: Exception) { }
            }
            fKey = "$ownerClassName.$name.[getValue_2]"
            kotlin.run {
                try {
                    val field = fieldsCache[fKey] ?: ownerObject::class.java.getField(name)
                    fieldsCache[fKey] = field
                    val returnValue = field.get(ownerObject)
                    return EDOObjectTransfer.convertToJSValueWithJavaValue(returnValue, owner.runtime)
                }
                catch (e: NoSuchFieldException) {
                    fastRejects[fKey] = true
                }
                catch (e: Exception) {

                }
            }
        }
        return V8.getUndefined()
    }

    fun setValueWithPropertyName(name: String, value: Object, owner: V8Object) {
        if (name.startsWith("s.")) {
            return try {
                val clazz = Class.forName(owner.getString("clazz"))
                if (!this.checkExported(clazz, name)) { return }
                val field = clazz.getDeclaredField(name.replace("s.", ""))
                var eageringType: Class<*> = field.type
                field.isAccessible = true
                val nsValue = EDOObjectTransfer.convertToJavaObjectWithJSValue(value, owner, eageringType) ?: return
                field.set(clazz, nsValue)
            } catch (e: Exception) { }
        }
        EDOObjectTransfer.convertToJavaObjectWithJSValue(owner, owner)?.let { ownerObject ->
            if (!this.checkExported(ownerObject::class.java, name)) { return }
            val ownerClassName = ownerObject::class.java.name
            var eageringType: Class<*>? = null
            typesCache["$ownerClassName.$name.[setValueType]"]?.let {
                eageringType = it
            } ?: kotlin.run {
                try {
                    eageringType = ownerObject::class.java.getDeclaredField(name).type
                    typesCache["$ownerClassName.$name.[setValueType]"] = eageringType!!
                } catch (e: Exception) { }
                if (eageringType != null) {
                    return@run
                }
                try {
                    ownerObject::class.java.methods.forEach {
                        if (eageringType != null) { return@forEach }
                        if (it.name == "set" + name.substring(0, 1).toUpperCase() + name.substring(1)) {
                            eageringType = it.parameterTypes[0]
                            typesCache["$ownerClassName.$name.[setValueType]"] = eageringType!!
                        }
                    }
                } catch (e: Exception) {}
                if (eageringType != null) {
                    return@run
                }
                try {
                    eageringType = ownerObject::class.java.getDeclaredField("m" + name.substring(0, 1).toUpperCase() + name.substring(1)).type
                    typesCache["$ownerClassName.$name.[setValueType]"] = eageringType!!
                } catch (e: Exception) { }
                if (eageringType != null) {
                    return@run
                }
            }
            val nsValue = EDOObjectTransfer.convertToJavaObjectWithJSValue(value, owner, eageringType)
            var fKey = "$ownerClassName.$name.[setValue_0]"
            kotlin.run {
                if (fastRejects[fKey] == true) {
                    return@run
                }
                try {
                    val field = fieldsCache[fKey] ?: ownerObject::class.java.getDeclaredField(name)
                    fieldsCache[fKey] = field
                    field.set(ownerObject, nsValue)
                    return
                }
                catch (e: NoSuchFieldException) {
                    fastRejects[fKey] = true
                }
                catch (e: Exception) { }
            }
            fKey = "$ownerClassName.$name.[setValue_2]"
            kotlin.run {
                if (fastRejects[fKey] == true) {
                    return@run
                }
                try {
                    val field = fieldsCache[fKey] ?: ownerObject::class.java.getDeclaredField("m" + name.substring(0, 1).toUpperCase() + name.substring(1))
                    fieldsCache[fKey] = field
                    field.set(ownerObject, nsValue)
                    return
                }
                catch (e: NoSuchFieldException) {
                    fastRejects[fKey] = true
                }
                catch (e: Exception) {

                }
            }
            fKey = "$ownerClassName.$name.[setValue_1]"
            kotlin.run {
                try {
                    val method = methodsCache[fKey]
                            ?: ownerObject::class.java.getMethod("set" + name.substring(0, 1).toUpperCase() + name.substring(1), eageringType
                                    ?: kotlin.run { nsValue?.let { return@run it::class.java } }
                                    ?: Object::class.java)
                    methodsCache[fKey] = method
                    method.invoke(ownerObject, nsValue)
                    return
                }
                catch (e: Exception) { }
            }
        }
    }

    fun callMethodWithName(name: String, arguments: V8Array, owner: V8Object): Any {
        try {
            val ownerObject = EDOObjectTransfer.convertToJavaObjectWithJSValue(owner, owner) ?: return V8.getUndefined()
            if (!this.checkExported(ownerObject::class.java, "($name)")) { return V8.getUndefined() }
            var eageringTypes: List<Class<*>>? = null
            var eageringMethod: Method? = null
            methodsCache["${ownerObject::class.java.name}.$name"]?.let {
                eageringTypes = it.parameterTypes.toList()
                eageringMethod = it
            } ?: kotlin.run {
                ownerObject::class.java.methods.forEach {
                    if (eageringTypes != null || eageringMethod != null) { return@forEach }
                    if (it.name.startsWith(name)) {
                        eageringTypes = it.parameterTypes.toList()
                        eageringMethod = it
                        methodsCache["${ownerObject::class.java.name}.$name"] = it
                    }
                }
            }
            val nsArguments = EDOObjectTransfer.convertToJavaListWithJSArray(arguments, owner, eageringTypes)
            val returnValue = eageringMethod?.invoke(ownerObject, *nsArguments.toTypedArray())
            return EDOObjectTransfer.convertToJSValueWithJavaValue(returnValue, owner.runtime)
        } catch (e: Exception) {
            return V8.getUndefined()
        }
    }

    fun addListenerWithName(name: String, owner: V8Object) {
        val ownerObject = EDOObjectTransfer.convertToJavaObjectWithJSValue(owner, owner) ?: return
        val values = (EDOJavaHelper.listeningEvents[ownerObject] ?: setOf()).toMutableSet()
        values.add(name)
        EDOJavaHelper.listeningEvents[ownerObject] = values.toSet()
    }

    fun javaObjectWithObjectRef(objectRef: String): Any? {
        return EDOV8ExtRuntime.javaObjectWithObjectRef(objectRef)
    }

    fun scriptObjectWithObject(anObject: Any, runtime: V8?, createdIfNeed: Boolean = false, initializer: EDOCallback? = null): V8Value {
        val context = this.contextWithRuntime(runtime) ?: return V8.getUndefined()
        return EDOV8ExtRuntime.extRuntime(context).scriptObjectWithJavaObject(anObject, createdIfNeed, initializer)
    }

    fun scriptObjectsWithObject(anObject: Any): List<V8Value> {
        return this.activeContexts.filter { !it.runtime.isReleased }.mapNotNull {
            return@mapNotNull EDOV8ExtRuntime.extRuntime(it).scriptObjectWithJavaObject(anObject, false)?.takeIf { it.v8Type == V8.V8_OBJECT }
        }
    }

    fun activeContexts(): List<JSContext> {
        return this.activeContexts.filter { !it.runtime.isReleased }
    }

    internal fun contextWithRuntime(runtime: V8?): JSContext? {
        val runtime = runtime ?: return null
        return this.activeContexts.firstOrNull { !it.runtime.isReleased && it.runtime == runtime }
    }

    private fun checkExported(clazz: Class<*>, exportedKey: String): Boolean {
        var cur: Class<*>? = clazz
        while (cur != null) {
            val fKey = "${cur.name}.$exportedKey"
            if (this.exportedKeysCache2[fKey] == true || this.exportedKeys.contains(fKey)) {
                if (cur != clazz) {
                    this.exportedKeysCache2[fKey] = true
                }
                return true
            }
            cur = cur.superclass
        }
        return false
    }

    companion object {

        val sharedExporter = EDOExporter()

    }

}