package com.example.sentiance_flutter

import androidx.multidex.MultiDex
import io.flutter.app.FlutterApplication
import io.flutter.plugin.common.PluginRegistry
import io.flutter.view.FlutterMain

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

    private fun initializeSentiance() {
        val result = Sentiance.getInstance(this).initialize()
        if (result.isSuccessful) {
            Log.d(TAG, "Initialization succeeded")
        } else {
            Log.e(TAG, "Initialization failed with reason ${result.failureReason!!.name}", result.throwable)
        }
    }
}
