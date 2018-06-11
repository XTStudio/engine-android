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
        val context = V8.createV8Runtime()
        EDOExporter.sharedExporter.exportWithContext(context)
        context.executeScript("var s = new UIView(); s.alpha = 0.5")
    }

}
