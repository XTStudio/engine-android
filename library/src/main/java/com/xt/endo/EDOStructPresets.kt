package com.xt.endo

import com.eclipsesource.v8.V8
import com.eclipsesource.v8.V8Array
import com.eclipsesource.v8.V8Object
import com.eclipsesource.v8.V8Value

/**
 * Created by cuiminghui on 2018/7/18.
 */

interface EDOStruct {

    fun toJSObject(context: V8): V8Object

}

class EDOCallback(private val scriptObject: V8Object?, private val idx: Int) {

    private var nativeBlock: ((arguments: List<Any>) -> Unit)? = null

    fun invoke(vararg arguments: Any): Any? {
        nativeBlock?.let { it.invoke(arguments.toList()); return null }
        val scriptObject = scriptObject ?: return null
        val v8Array = V8Array(scriptObject.runtime)
        v8Array.push(idx)
        v8Array.push(EDOObjectTransfer.convertToJSArrayWithJavaList(arguments.toList(), scriptObject.runtime))
        val result = try {
            scriptObject.executeFunction("__invokeCallback", v8Array)
        } catch (e: Exception) { V8.getUndefined() }
        v8Array.release()
        return EDOObjectTransfer.convertToJavaObjectWithJSValue(result, result as? V8Object)
    }

    internal fun invokeAndReturnV8Object(vararg arguments: Any): V8Value? {
        nativeBlock?.let { it.invoke(arguments.toList()); return null }
        val scriptObject = scriptObject ?: return null
        val v8Array = V8Array(scriptObject.runtime)
        v8Array.push(idx)
        v8Array.push(EDOObjectTransfer.convertToJSArrayWithJavaList(arguments.toList(), scriptObject.runtime))
        val result = try {
            scriptObject.executeFunction("__invokeCallback", v8Array)
        } catch (e: Exception) { V8.getUndefined() }
        v8Array.release()
        return result as? V8Value
    }

    companion object {

        fun createWithBlock(block: (arguments: List<Any>) -> Unit): EDOCallback {
            val callback = EDOCallback(null, -1)
            callback.nativeBlock = block
            return callback
        }

    }

}

class CGRect(val x: Double, val y: Double, val width: Double, val height: Double): EDOStruct {

    override fun toJSObject(context: V8): V8Object {
        val v8Object = V8Object(context)
        v8Object.add("x", x)
        v8Object.add("y", y)
        v8Object.add("width", width)
        v8Object.add("height", height)
        return v8Object
    }


    companion object {

        @JvmStatic fun fromJSObject(v8Object: V8Object): EDOStruct {
            return CGRect(
                    v8Object.getDouble("x"),
                    v8Object.getDouble("y"),
                    v8Object.getDouble("width"),
                    v8Object.getDouble("height")
            )
        }

    }

}

class CGPoint(val x: Double, val y: Double): EDOStruct {

    override fun toJSObject(context: V8): V8Object {
        val v8Object = V8Object(context)
        v8Object.add("x", x)
        v8Object.add("y", y)
        return v8Object
    }


    companion object {

        @JvmStatic fun fromJSObject(v8Object: V8Object): EDOStruct {
            return CGPoint(
                    v8Object.getDouble("x"),
                    v8Object.getDouble("y")
            )
        }

    }

}

class CGSize(val width: Double, val height: Double): EDOStruct {

    override fun toJSObject(context: V8): V8Object {
        val v8Object = V8Object(context)
        v8Object.add("width", width)
        v8Object.add("height", height)
        return v8Object
    }


    companion object {

        @JvmStatic fun fromJSObject(v8Object: V8Object): EDOStruct {
            return CGSize(
                    v8Object.getDouble("width"),
                    v8Object.getDouble("height")
            )
        }

    }

}

class CGAffineTransform(val a: Double, val b: Double, val c: Double, val d: Double, val tx: Double, val ty: Double): EDOStruct {

    override fun toJSObject(context: V8): V8Object {
        val v8Object = V8Object(context)
        v8Object.add("a", a)
        v8Object.add("b", b)
        v8Object.add("c", c)
        v8Object.add("d", d)
        v8Object.add("tx", tx)
        v8Object.add("ty", ty)
        return v8Object
    }


    companion object {

        @JvmStatic fun fromJSObject(v8Object: V8Object): EDOStruct {
            return CGAffineTransform(
                    v8Object.getDouble("a"),
                    v8Object.getDouble("b"),
                    v8Object.getDouble("c"),
                    v8Object.getDouble("d"),
                    v8Object.getDouble("tx"),
                    v8Object.getDouble("ty")
            )
        }

    }

}

class UIEdgeInsets(val top: Double, val left: Double, val bottom: Double, val right: Double): EDOStruct {

    override fun toJSObject(context: V8): V8Object {
        val v8Object = V8Object(context)
        v8Object.add("top", top)
        v8Object.add("left", left)
        v8Object.add("bottom", bottom)
        v8Object.add("right", right)
        return v8Object
    }


    companion object {

        @JvmStatic fun fromJSObject(v8Object: V8Object): EDOStruct {
            return UIEdgeInsets(
                    v8Object.getDouble("top"),
                    v8Object.getDouble("left"),
                    v8Object.getDouble("bottom"),
                    v8Object.getDouble("right")
            )
        }

    }

}

class UIRange(val location: Int, val length: Int): EDOStruct {

    override fun toJSObject(context: V8): V8Object {
        val v8Object = V8Object(context)
        v8Object.add("location", location)
        v8Object.add("length", length)
        return v8Object
    }


    companion object {

        @JvmStatic fun fromJSObject(v8Object: V8Object): EDOStruct {
            return UIRange(
                    v8Object.getInteger("location"),
                    v8Object.getInteger("length")
            )
        }

    }

}