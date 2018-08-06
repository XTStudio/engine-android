package com.xt.endo

import com.eclipsesource.v8.V8
import com.eclipsesource.v8.V8Object
import com.eclipsesource.v8.V8Value
import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.HashMap

private val refMapping: WeakHashMap<Any, String> = WeakHashMap()
private val weakValueRefMapping: HashMap<String, WeakReference<Any>> = HashMap()

private fun Any.edo_objectRef(): String {
    return refMapping[this] ?: kotlin.run {
        val objectRef = UUID.randomUUID().toString()
        refMapping[this] = objectRef
        weakValueRefMapping[objectRef] = WeakReference(this)
        return@run objectRef
    }
}

/**
 * Created by cuiminghui on 2018/7/17.
 */
class EDOV8ExtRuntime(val value: WeakReference<V8>) {

    private val soManagedValue: WeakHashMap<Any, V8Object> = WeakHashMap()
    private val soManagedValue2: WeakHashMap<Any, V8Object> = WeakHashMap()

    fun storeScriptObject(anObject: Any, scriptObject: V8Object) {
        soManagedValue[anObject] = scriptObject.twin().setWeak() as? V8Object
    }

    fun createMetaClass(anObject: Any): V8Value {
        try {
            val runtime = value.get() ?: return V8.getUndefined()
            return runtime.executeObjectScript("new _EDO_MetaClass('${anObject::class.java.name}', '${anObject.edo_objectRef()}')")
        } catch (e: Exception) { return V8.getUndefined() }
    }

    fun scriptObjectWithJavaObject(anObject: Any, createdIfNeed: Boolean = true, initializer: EDOCallback? = null): V8Value {
        this.soManagedValue[anObject]?.takeIf { !it.isReleased }?.let { return it }
        this.soManagedValue2[anObject]?.takeIf { !it.isReleased }?.let { return it }
        if (!createdIfNeed) { return V8.getUndefined() }
        val context = this.value.get() ?: return V8.getUndefined()
        var target: EDOExportable? = null
        var forEachEnded = false
        EDOExporter.sharedExporter.exportables.forEach {
            if (forEachEnded) { return@forEach }
            if (it.value.clazz == anObject::class.java) {
                target = it.value
                forEachEnded = true
                return@forEach
            }
            else if (it.value.clazz.isAssignableFrom(anObject::class.java)) {
                if (target?.clazz?.isAssignableFrom(it.value.clazz) == true) {
                    target = it.value
                }
                else if (target == null) {
                    target = it.value
                }
            }
        }
        target?.let { target ->
            initializer?.let { initializer ->
                val objectMetaClass = context.executeObjectScript("new _EDO_MetaClass('${target.name}', '${anObject.edo_objectRef()}')")
                val scriptObject = initializer.invokeAndReturnV8Object(objectMetaClass) ?: return V8.getUndefined()
                soManagedValue[anObject] = scriptObject.twin().setWeak() as? V8Object
                soManagedValue2[anObject] = scriptObject.twin().setWeak() as? V8Object
                return scriptObject
            } ?: kotlin.run {
                val scriptObject = context.executeObjectScript("new ${target.name}(new _EDO_MetaClass('${target.name}', '${anObject.edo_objectRef()}'))")
                soManagedValue[anObject] = scriptObject.twin().setWeak() as? V8Object
                soManagedValue2[anObject] = scriptObject.twin().setWeak() as? V8Object
                return scriptObject
            }
        }
        return V8.getUndefined()
    }

    companion object {

        private val runtimeMapping: WeakHashMap<V8, EDOV8ExtRuntime> = WeakHashMap()

        fun extRuntime(runtime: V8): EDOV8ExtRuntime {
            return runtimeMapping[runtime] ?: kotlin.run {
                val extRuntime = EDOV8ExtRuntime(WeakReference(runtime))
                runtimeMapping[runtime] = extRuntime
                return@run extRuntime
            }
        }

        fun javaObjectWithObjectRef(objectRef: String): Any? {
            return weakValueRefMapping[objectRef]?.get()
        }

    }

}