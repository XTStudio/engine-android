package com.xt.endo.sample

import android.app.Application
import com.xt.endo.EDOExporter

/**
 * Created by cuiminghui on 2018/7/17.
 */
class MainApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        EDOExporter.sharedExporter.initializer(this.applicationContext)
    }

}