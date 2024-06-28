package com.example.sentiance_flutter

import androidx.multidex.MultiDex
import io.flutter.app.FlutterApplication
import io.flutter.plugin.common.PluginRegistry
import io.flutter.view.FlutterMain
import com.sentiance.sdk.Sentiance

class Application : FlutterApplication(), PluginRegistry.PluginRegistrantCallback {

    override fun onCreate() {
        super.onCreate()
        FlutterMain.startInitialization(this)
        initializeSentiance()
        SentianceWrapper(this).initializeSentianceSdk()
        MultiDex.install(this)
        android.util.Log.e("TAG", "onCreate:initt " )
    }

    override fun registerWith(registry: PluginRegistry) {

    }

    fun initializeSentiance(context: Context) {
    val result = Sentiance.getInstance(context).initialize()
    if (result.isSuccessful) {
        //Log.d(TAG, "Initialization succeeded")
    }
}
}
