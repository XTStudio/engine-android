package com.xt.endo.sample

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.eclipsesource.v8.V8
import com.xt.endo.EDOExporter
import com.xt.endo.EDONativeObject


class UIView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), EDONativeObject {

    fun helloWorld(a: Int, b: Int): Map<String, UIView> {
        return mapOf(Pair("e", this))
    }

}

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        EDOExporter.sharedExporter.exportClass(UIView::class.java, "UIView")
        EDOExporter.sharedExporter.exportInitializer(UIView::class.java, {
            return@exportInitializer UIView(this)
        })
        EDOExporter.sharedExporter.exportProperty(UIView::class.java, "alpha")
        EDOExporter.sharedExporter.exportMethodToJavaScript(UIView::class.java, "helloWorld")
        val context = V8.createV8Runtime()
        EDOExporter.sharedExporter.exportWithContext(context)
        context.executeScript("var e = new UIView(); e.helloWorld(1, 2)['e'].helloWorld(3, 4)")
    }

}
