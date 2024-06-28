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

    fun initializeSentiance() {
    val result = Sentiance.getInstance(this).initialize()
    if (result.isSuccessful) {
        Log.e("TAG", "Initialization succeeded")
    }else {
    Log.e("TAG", "Intialization failed with reason ${result.failureReason!!.name}");
}
}
}
