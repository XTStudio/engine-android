package com.xt.endo

import com.eclipsesource.v8.V8
import com.eclipsesource.v8.V8Array
import com.eclipsesource.v8.V8Object
import com.eclipsesource.v8.V8Value

/**
 * Created by cuiminghui on 2018/6/8.
 */

class EDOExporter {

    private var exportables: Map<String, EDOExportable> = mapOf()
    private var references: MutableMap<String, EDOObjectReference> = mutableMapOf()
    private var scriptObjects: MutableMap<String, V8Value> = mutableMapOf()

    fun exportWithContext(context: V8) {
        var script = "var __extends=(this&&this.__extends)||(function(){var extendStatics=Object.setPrototypeOf||({__proto__:[]}instanceof Array&&function(d,b){d.__proto__=b})||function(d,b){for(var p in b)if(b.hasOwnProperty(p))d[p]=b[p]};return function(d,b){extendStatics(d,b);function __(){this.constructor=d}d.prototype=b===null?Object.create(b):(__.prototype=b.prototype,new __())}})();var _EDO_MetaClass = /** @class */ (function () { function _EDO_MetaClass(classname, objectRef) { this.classname = classname; this.objectRef = objectRef; } return _EDO_MetaClass; }());var _EDO_Callback=(function(){function _EDO_Callback(func){this.func=func;this._meta_class={classname:\"__Function\"}}return _EDO_Callback}());var EDOObject=(function(){function EDOObject(){this.__callbacks=[]}EDOObject.prototype.__convertToJSValue=function(parameter){if(typeof parameter===\"function\"){var callback=new _EDO_Callback(parameter);this.__callbacks.push(callback);callback._meta_class.idx=this.__callbacks.length-1;return callback}return parameter};EDOObject.prototype.__invokeCallback=function(idx,args){if(this.__callbacks[idx]){this.__callbacks[idx].func.apply(this,args)}};return EDOObject}());"
        try {
            context.executeScript(script)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val exportables = this.exportables.toMutableMap()
        val exported: MutableList<String> = mutableListOf()
        exported.add("EDOObject")
        var exportingLoopCount = 0
        while (exportables.count() > 0) {
            exportingLoopCount = 0
            exportables.toMap().forEach {
                if (!exported.contains(it.value.superName)) {
                    return@forEach
                }
                val constructorScript = "function Initializer(){var _this = _super.call(this, ${it.key}) || this;if(arguments[0]instanceof _EDO_MetaClass){_this._meta_class=arguments[0]}else{var args=[];for(var key in arguments){args.push(_this.__convertToJSValue(arguments[key]))}_this._meta_class=ENDO.createInstance(typeof arguments[0] === \"string\" ? arguments[0] : \"${it.key}\",args,_this)}return _this;}"
                val propsScript = it.value.exportedProps.map {
                    return@map "Object.defineProperty(Initializer.prototype,\"${it.replace("edo_", "")}\",{get:function(){return ENDO.valueWithPropertyName(\"$it\",this)},set:function(value){ENDO.setValueWithPropertyName(\"$it\",value,this)},enumerable:false,configurable:true});"
                }.joinToString(";")
                val clazzScript = ";var ${it.key} = /** @class */ (function (_super) {;__extends(Initializer, _super) ;$constructorScript; $propsScript ;return Initializer; }(${it.value.superName}));"
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
        context.add("ENDO", endoV8Object)
        try {
            context.executeScript(script)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        endoV8Object.release()
    }

    fun exportClass(clazz: Class<*>, name: String, superName: String = "EDOObject") {
        val exportable = EDOExportable(clazz, name, superName)
        this.exportables = kotlin.run {
            val mutable = this.exportables.toMutableMap()
            mutable[name] = exportable
            return@run mutable.toMap()
        }
    }

    fun exportInitializer(clazz: Class<*>, initializer: (arguments: List<Object>) -> Any) {
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

    fun createInstance(name: String, arguments: V8Array, owner: V8Object): V8Value {
        this.exportables[name]?.let { exportable ->
            val newInstance = exportable.initializer?.let { it(listOf()) } ?: kotlin.run {
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
        objectMetaClass.setWeak()
        val objectReference = EDOObjectReference(anObject, objectMetaClass)
        synchronized(this, {
            this.references[anObject.objectRef] = objectReference
            owner?.let { owner ->
                this.scriptObjects[anObject.objectRef] = owner.twin()
            }
        })
        return objectMetaClass
    }

    fun valueWithPropertyName(name: String, owner: V8Object): Any {
        EDOObjectTransfer.convertToNSValueWithJSValue(owner, owner)?.let { ownerObject ->
            try {
                return ownerObject::class.java.getMethod("get" + name.substring(0, 1).toUpperCase() + name.substring(1)).invoke(ownerObject)
            } catch (e: Exception) {

            }
            try {
                return ownerObject::class.java.getField("m" + name.substring(0, 1).toUpperCase() + name.substring(1)).get(ownerObject)
            } catch (e: Exception) {

            }
            try {
                return ownerObject::class.java.getField(name).get(ownerObject)
            } catch (e: Exception) {

            }
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

    fun nsValueWithObjectRef(objectRef: String): EDONativeObject? {
        return this.references[objectRef]?.value
    }

    companion object {

        val sharedExporter = EDOExporter()

    }


}