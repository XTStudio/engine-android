package com.xt.endo.sample

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.eclipsesource.v8.V8
import com.xt.endo.EDOExporter
import com.xt.endo.EDONativeObject


class UIView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), EDONativeObject {

    override fun deinit() {
        super.deinit()
        removeAllViews()
    }

    fun addSubview(subview: UIView) {
        this.addView(subview)
    }

    fun removeFromSuperview() {
        (this.parent as? ViewGroup)?.removeView(this)
    }

    fun layoutSubviews() {
        this.invokeBindingMethod("layoutSubviews", kotlin.collections.listOf(this))
    }

    override fun onViewAdded(child: View?) {
        super.onViewAdded(child)
        (child as? UIView)?.retain()
    }

    override fun onViewRemoved(child: View?) {
        super.onViewRemoved(child)
        (child as? UIView)?.release()
    }

}

class MainActivity : AppCompatActivity() {

    var context: V8? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        EDOExporter.sharedExporter.exportClass(UIView::class.java, "UIView")
        EDOExporter.sharedExporter.exportInitializer(UIView::class.java, {
            return@exportInitializer UIView(this)
        })
        EDOExporter.sharedExporter.exportProperty(UIView::class.java, "alpha")
        EDOExporter.sharedExporter.bindMethodToJavaScript(UIView::class.java, "layoutSubviews")
        EDOExporter.sharedExporter.exportMethodToJavaScript(UIView::class.java, "addSubview")
        EDOExporter.sharedExporter.exportMethodToJavaScript(UIView::class.java, "removeFromSuperview")
        val context = V8.createV8Runtime()
        EDOExporter.sharedExporter.exportWithContext(context, this)
        context.executeScript("var e = new UIView(); (function(){ var ww = new UIView(); e.addSubview(ww); ww.removeFromSuperview(); })();")
        this.context = context
        EDOExporter.sharedExporter.runGC(true)
    }

}
