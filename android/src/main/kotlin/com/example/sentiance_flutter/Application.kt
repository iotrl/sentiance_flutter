package com.example.sentiance_flutter

import androidx.multidex.MultiDex
import io.flutter.app.FlutterApplication
import io.flutter.plugin.common.PluginRegistry
import io.flutter.view.FlutterMain

class Application : FlutterApplication(), PluginRegistry.PluginRegistrantCallback {

    override fun onCreate() {
        super.onCreate()
        FlutterMain.startInitialization(this)
        SentianceWrapper(this).initializeSentianceSdk()
        MultiDex.install(this)
        android.util.Log.e("TAG", "onCreate:initt " )
    }

    override fun registerWith(registry: PluginRegistry) {

    }
}
