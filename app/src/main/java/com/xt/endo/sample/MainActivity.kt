package com.xt.endo.sample

import android.content.Context
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.eclipsesource.v8.V8
import com.xt.endo.*

@EDOExportClass("UIView")
@EDOExportProperties("alpha", "backgroundColor")
class UIView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), EDONativeObject {

    @EDOExportProperty
    var xxx: Int = 1

    override fun deinit() {
        super.deinit()
        removeAllViews()
    }

    @EDOExportMethod
    fun addSubview(subview: UIView) {
        this.addView(subview)
    }

    @EDOExportMethod
    fun removeFromSuperview() {
        (this.parent as? ViewGroup)?.removeView(this)
    }

    @EDOBindMethod
    fun layoutSubviews() {
        this.invokeBindingMethod("layoutSubviews")
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

class UIKit: EDOPackage() {

    override fun install() {
        super.install()
        EDOExporter.sharedExporter.exportClasses(
                UIView::class.java
        )
        EDOExporter.sharedExporter.exportInitializer(UIView::class.java, { _, applicationContext ->
            return@exportInitializer UIView(applicationContext)
        })
    }

    companion object {

        fun attach() {
            EDOExporter.sharedExporter.exportPackage(UIKit())
        }

    }

}

class MainActivity : AppCompatActivity() {

    var context: V8? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        UIKit.attach()
        this.context = V8.createV8Runtime().attach(this)
        this.context?.executeScript("var e = new UIView(); e.alpha = 1.0; (function(){ var ww = new UIView(); e.addSubview(ww); })();")
        (this.context?.fetchValue("e") as? UIView)?.let {
            setContentView(it)
        }
    }

}
