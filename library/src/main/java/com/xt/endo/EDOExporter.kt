package com.xt.endo

import android.content.ComponentCallbacks
import android.content.Context
import android.content.res.Configuration
import android.os.Handler
import com.eclipsesource.v8.V8
import com.eclipsesource.v8.V8Array
import com.eclipsesource.v8.V8Object
import com.eclipsesource.v8.V8Value
import dalvik.system.DexFile
import dalvik.system.PathClassLoader
import java.lang.reflect.Method
import java.util.*

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
                    runGC(true)
                }
                override fun onConfigurationChanged(newConfig: Configuration?) { }
            })
        }

    private val activeContexts: MutableSet<V8> = mutableSetOf()
    private var exportables: Map<String, EDOExportable> = mapOf()
    private var references: MutableMap<String, EDOObjectReference> = mutableMapOf()
    private var scriptObjects: MutableMap<String, V8Value> = mutableMapOf()
    private val gcTimer: Timer = Timer()
    private var gcTask: TimerTask? = null

    init {
        this.runGC()
    }

    fun runGC(force: Boolean = false) {
        val handler = Handler()
        this.gcTask?.cancel()
        this.gcTask = object : TimerTask() {
            override fun run() {
                handler.post {
                    if (force || Math.random() * 100 < 20) {
                        activeContexts.filter { !it.isReleased }.forEach { it.lowMemoryNotification() }
                    }
                    else {
                        this@EDOExporter.runGC()
                        return@post
                    }
                    System.out.println("[EDOExporter] GC Running.")
                    synchronized(this@EDOExporter, {
                        val removingKeys = this@EDOExporter.references.filterValues {
                            return@filterValues it.metaClassManagedValue.runtime.isReleased || it.metaClassManagedValue.isReleased
                        }
                        removingKeys.keys.forEach {
                            this@EDOExporter.references[it]?.value?.deinit()
                            this@EDOExporter.references.remove(it)
                            this@EDOExporter.scriptObjects.remove(it)
                        }
                        System.out.println("[EDOExporter] ${removingKeys.count()} object released")
                    })
                    this@EDOExporter.runGC()
                }
            }
        }
        this.gcTimer.schedule(this.gcTask, (if (force) 0 else 10000).toLong())
    }

    fun exportWithContext(context: V8, applicationContext: Context) {
        this.activeContexts.add(context)
        this.applicationContext = applicationContext
        var script = ";var __edo_retaining_set = {}; function __edo_retain(anObject){ __edo_retaining_set[anObject] = 1; };function __edo_release(anObject){ delete __edo_retaining_set[anObject]; };var __extends=(this&&this.__extends)||(function(){var extendStatics=Object.setPrototypeOf||({__proto__:[]}instanceof Array&&function(d,b){d.__proto__=b})||function(d,b){for(var p in b)if(b.hasOwnProperty(p))d[p]=b[p]};return function(d,b){extendStatics(d,b);function __(){this.constructor=d}d.prototype=b===null?Object.create(b):(__.prototype=b.prototype,new __())}})();var _EDO_MetaClass = /** @class */ (function () { function _EDO_MetaClass(classname, objectRef) { this.classname = classname; this.objectRef = objectRef; } return _EDO_MetaClass; }());var _EDO_Callback=(function(){function _EDO_Callback(func){this.func=func;this._meta_class={classname:\"__Function\"}}return _EDO_Callback}());var EDOObject=(function(){function EDOObject(){this.__callbacks=[]}EDOObject.prototype.__convertToJSValue=function(parameter){if(typeof parameter===\"function\"){var callback=new _EDO_Callback(parameter);this.__callbacks.push(callback);callback._meta_class.idx=this.__callbacks.length-1;return callback}return parameter};EDOObject.prototype.__invokeCallback=function(idx,args){if(this.__callbacks[idx]){this.__callbacks[idx].func.apply(this,args)}};return EDOObject}());"
        val exportables = this.exportables.toMutableMap()
        val exported: MutableList<String> = mutableListOf()
        exported.add("EDOObject")
        var exportingLoopCount: Int
        while (exportables.count() > 0) {
            exportingLoopCount = 0
            exportables.toMap().forEach {
                if (!exported.contains(it.value.superName)) {
                    return@forEach
                }
                val constructorScript = "function Initializer(){var _this = _super.call(this, ${it.key}) || this;if(arguments[0]instanceof _EDO_MetaClass){_this._meta_class=arguments[0]}else{var args=[];for(var key in arguments){args.push(_this.__convertToJSValue(arguments[key]))}_this._meta_class=ENDO.createInstance(typeof arguments[0] === \"string\" ? arguments[0] : \"${it.key}\",args,_this);};return _this;}"
                val propsScript = it.value.exportedProps.map {
                    return@map "Object.defineProperty(Initializer.prototype,\"${it.replace("edo_", "")}\",{get:function(){return ENDO.valueWithPropertyName(\"$it\",this)},set:function(value){ENDO.setValueWithPropertyName(\"$it\",value,this)},enumerable:false,configurable:true});"
                }.joinToString(";")
                val bindMethodScript = it.value.bindedMethods.map {
                    return@map "Initializer.prototype.$it=function(){};Initializer.prototype.__$it=function(){this.$it.apply(this,arguments)};"
                }.joinToString(";")
                val methodScript = it.value.exportedMethods.map {
                    return@map "Initializer.prototype.${it.replace("edo_", "")} = function () {var args=[];for(var key in arguments){args.push(this.__convertToJSValue(arguments[key]))}return ENDO.callMethodWithName(\"$it\", args, this);};"
                }.joinToString(";")
                val clazzScript = ";var ${it.key} = /** @class */ (function (_super) {;__extends(Initializer, _super) ;$constructorScript; $propsScript ;$bindMethodScript ;$methodScript;return Initializer; }(${it.value.superName}));"
                script += clazzScript
                exported.add(it.value.name)
                exportables.remove(it.key)
                exportingLoopCount++
            }
            assert(exportingLoopCount > 0, { return@assert "Did you forgot to export some class superClass?" })
        }
        val endoV8Object = V8Object(context)
        endoV8Object.registerJavaMethod(this, "createInstance", "createInstance", arrayOf(String::class.java, V8Array::class.java, V8Object::class.java))
        endoV8Object.registerJavaMethod(this, "valueWithPropertyName", "valueWithPropertyName", arrayOf(String::class.java, V8Object::class.java))
        endoV8Object.registerJavaMethod(this, "setValueWithPropertyName", "setValueWithPropertyName", arrayOf(String::class.java, Object::class.java, V8Object::class.java))
        endoV8Object.registerJavaMethod(this, "callMethodWithName", "callMethodWithName", arrayOf(String::class.java, V8Array::class.java, V8Object::class.java))
        context.add("ENDO", endoV8Object)
        try {
            context.executeScript(script)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        endoV8Object.release()
    }

    fun exportPackage(pkg: EDOPackage) {
        pkg.install()
    }

    fun exportClasses(vararg classes: Class<*>) {
        classes.forEach { clazz ->
            clazz.annotations.forEach {
                (it as? EDOExportClass)?.let {
                    this.exportClass(clazz, it.name)
                }
                (it as? EDOExportExtendingClass)?.let {
                    this.exportClass(clazz, it.name, it.superName)
                }
                (it as? EDOExportProperties)?.let {
                    it.names.forEach {
                        this.exportProperty(clazz, it)
                    }
                }
            }
            clazz.declaredFields.forEach { field ->
                field.annotations.forEach {
                    (it as? EDOExportProperty)?.let {
                        this.exportProperty(clazz, field.name)
                    }
                }
            }
            clazz.declaredMethods.forEach { method ->
                method.annotations.forEach {
                    (it as? EDOExportMethod)?.let {
                        this.exportMethodToJavaScript(clazz, method.name)
                    }
                    (it as? EDOBindMethod)?.let {
                        this.bindMethodToJavaScript(clazz, method.name)
                    }
                }
            }
        }
    }

    fun exportClass(clazz: Class<*>, name: String, superName: String = "EDOObject") {
        val exportable = EDOExportable(clazz, name, superName)
        this.exportables = kotlin.run {
            val mutable = this.exportables.toMutableMap()
            mutable[name] = exportable
            return@run mutable.toMap()
        }
    }

    fun exportInitializer(clazz: Class<*>, initializer: (arguments: List<*>, applicationContext: Context) -> Any) {
        this.exportables.filter { it.value.clazz == clazz }.forEach {
            it.value.initializer = initializer
        }
    }

    fun exportProperty(clazz: Class<*>, propName: String) {
        this.exportables.filter { it.value.clazz == clazz }.forEach {
            if (it.value.exportedProps.contains(propName)) { return@forEach }
            it.value.exportedProps = kotlin.run {
                val mutable = it.value.exportedProps.toMutableList()
                mutable.add(propName)
                return@run mutable.toList()
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

    fun exportMethodToJavaScript(clazz: Class<*>, methodName: String) {
        this.exportables.filter { it.value.clazz == clazz }.forEach {
            if (it.value.exportedMethods.contains(methodName)) { return@forEach }
            it.value.exportedMethods = kotlin.run {
                val mutable = it.value.exportedMethods.toMutableList()
                mutable.add(methodName)
                return@run mutable.toList()
            }
        }
    }

    fun createInstance(name: String, arguments: V8Array, owner: V8Object): V8Value {
        this.exportables[name]?.let { exportable ->
            val newInstance = exportable.initializer?.let { it(EDOObjectTransfer.convertToNSArgumentsWithJSArguments(arguments, owner), this.applicationContext!!) } ?: kotlin.run {
                return@run try { exportable.clazz.getDeclaredConstructor().newInstance() } catch (e: Exception) { null }
            }
            (newInstance as? EDONativeObject)?.let {
                return createMetaClass(it, owner.runtime, owner)
            }
        }
        return V8.getUndefined()
    }

    fun createMetaClass(anObject: EDONativeObject, context: V8, owner: V8Object?): V8Value {
        val objectMetaClass = context.executeObjectScript("new _EDO_MetaClass('${anObject::class.java.name}', '${anObject.objectRef}')")
        val s = objectMetaClass.twin()
        s.setWeak()
        val objectReference = EDOObjectReference(anObject, s)
        synchronized(this, {
            this.references[anObject.objectRef] = objectReference
            owner?.let { owner ->
                val s = owner.twin()
                s.setWeak()
                this.scriptObjects[anObject.objectRef] = s
            }
        })
        return objectMetaClass
    }

    fun valueWithPropertyName(name: String, owner: V8Object): Any {
        EDOObjectTransfer.convertToNSValueWithJSValue(owner, owner)?.let { ownerObject ->
            try {
                val returnValue = ownerObject::class.java.getMethod("get" + name.substring(0, 1).toUpperCase() + name.substring(1)).invoke(ownerObject)
                return EDOObjectTransfer.convertToJSValueWithNSValue(returnValue, owner.runtime)
            } catch (e: Exception) { }
            try {
                val returnValue = ownerObject::class.java.getField("m" + name.substring(0, 1).toUpperCase() + name.substring(1)).get(ownerObject)
                return EDOObjectTransfer.convertToJSValueWithNSValue(returnValue, owner.runtime)
            } catch (e: Exception) { }
            try {
                val returnValue = ownerObject::class.java.getField(name).get(ownerObject)
                return EDOObjectTransfer.convertToJSValueWithNSValue(returnValue, owner.runtime)
            } catch (e: Exception) { }
        }
        return V8.getUndefined()
    }

    fun setValueWithPropertyName(name: String, value: Object, owner: V8Object) {
        EDOObjectTransfer.convertToNSValueWithJSValue(owner, owner)?.let { ownerObject ->
            var eageringType: Class<*>? = null
            val setterName = "set" + name.substring(0, 1).toUpperCase() + name.substring(1)
            try {
                ownerObject::class.java.methods.forEach {
                    if (it.name.startsWith(setterName)) {
                        eageringType = it.parameterTypes[0]
                    }
                }
            } catch (e: Exception) {}
            val nsValue = EDOObjectTransfer.convertToNSValueWithJSValue(value, owner, eageringType) ?: return
            try {
                ownerObject::class.java.getMethod("set" + name.substring(0, 1).toUpperCase() + name.substring(1), eageringType ?: nsValue::class.java).invoke(ownerObject, nsValue)
                return
            } catch (e: Exception) {}
            try {
                ownerObject::class.java.getField("m" + name.substring(0, 1).toUpperCase() + name.substring(1)).set(ownerObject, nsValue)
                return
            } catch (e: Exception) {}
            try {
                ownerObject::class.java.getField(name).set(ownerObject, nsValue)
                return
            } catch (e: Exception) {}
        }
    }

    fun callMethodWithName(name: String, arguments: V8Array, owner: V8Object): Any {
        try {
            val ownerObject = EDOObjectTransfer.convertToNSValueWithJSValue(owner, owner)
            (ownerObject as? EDONativeObject)?.let { ownerObject ->
                var eageringTypes: List<Class<*>>? = null
                var eageringMethod: Method? = null
                ownerObject::class.java.methods.forEach {
                    if (eageringTypes != null || eageringMethod != null) { return@forEach }
                    if (it.name.startsWith(name)) {
                        eageringTypes = it.parameterTypes.toList()
                        eageringMethod = it
                    }
                }
                val nsArguments = EDOObjectTransfer.convertToNSArgumentsWithJSArguments(arguments, owner, eageringTypes)
                val returnValue = eageringMethod?.invoke(ownerObject, *nsArguments.toTypedArray())
                return EDOObjectTransfer.convertToJSValueWithNSValue(returnValue, owner.runtime)
            }
            return V8.getUndefined()
        } catch (e: Exception) {
            e.printStackTrace()
            return V8.getUndefined()
        }
    }

    fun nsValueWithObjectRef(objectRef: String): EDONativeObject? {
        return this.references[objectRef]?.value
    }

    fun scriptObjectWithObject(anObject: EDONativeObject, context: V8?): V8Value {
        this.scriptObjects[anObject.objectRef]?.let { return it }
        val context = context ?: return V8.getUndefined()
        this.exportables.forEach {
            if (it.value.clazz == anObject::class.java) {
                val scriptObject = context.executeObjectScript("new ${it.value.name}(new _EDO_MetaClass('${it.value.name}', '${anObject.objectRef}'))")
                val objectMetaClass = scriptObject.get("_meta_class") as? V8Object ?: return@forEach
                val objectReference = EDOObjectReference(anObject, objectMetaClass.twin())
                synchronized(this, {
                    this.references[anObject.objectRef] = objectReference
                    this.scriptObjects[anObject.objectRef] = scriptObject
                })
                return scriptObject
            }
        }
        return V8.getUndefined()
    }

    fun retain(anObject: EDONativeObject) {
        this.references[anObject.objectRef]?.let { ref ->
            if (ref.metaClassManagedValue.isReleased) { return }
            ref.retainCount++
            if (ref.retainCount == 1) {
                this.scriptObjectWithObject(anObject, null)?.let {
                    if (it.isReleased) { return }
                    val args = V8Array(it.runtime)
                    args.push(it)
                    it.runtime.executeJSFunction("__edo_retain", args)
                    args.release()
                }
            }
        }
    }

    fun release(anObject: EDONativeObject) {
        this.references[anObject.objectRef]?.let { ref ->
            if (ref.metaClassManagedValue.isReleased) { return }
            ref.retainCount--
            if (ref.retainCount <= 0) {
                this.scriptObjectWithObject(anObject, null)?.let {
                    if (it.isReleased) { return }
                    val args = V8Array(it.runtime)
                    args.push(it)
                    it.runtime.executeJSFunction("__edo_release", args)
                    args.release()
                }
            }
        }
    }

    companion object {

        val sharedExporter = EDOExporter()

    }


}

fun V8.attach(applicationContext: Context): V8 {
    EDOExporter.sharedExporter.exportWithContext(this, applicationContext)
    return this
}

fun V8.fetchValue(key: String): Any? {
    val value = this.get(key) as? V8Object ?: return null
    val returnValue = EDOObjectTransfer.convertToNSValueWithJSValue(value, value)
    value.release()
    return returnValue
}