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

    fun storeScriptObject(anObject: Any, scriptObject: V8Object) {
        soManagedValue[anObject] = scriptObject.twin().setWeak() as? V8Object
    }

    fun createMetaClass(anObject: Any): V8Value {
        val runtime = value.get() ?: return V8.getUndefined()
        return runtime.executeObjectScript("new _EDO_MetaClass('${anObject::class.java.name}', '${anObject.edo_objectRef()}')")
    }

    fun scriptObjectWithJavaObject(anObject: Any): V8Value {
        this.soManagedValue[anObject]?.takeIf { !it.isReleased }?.let { return it }
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