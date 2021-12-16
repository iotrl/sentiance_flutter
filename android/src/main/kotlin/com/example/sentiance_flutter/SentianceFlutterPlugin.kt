package com.example.sentiance_flutter

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.IntentFilter
import android.util.Log
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.LifecycleEventObserver
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.sentiance_flutter.sentiance.PermissionCheckActivity
import com.example.sentiance_flutter.sentiance.PermissionManager
import com.example.sentiance_flutter.model.SentianceDataModel
import com.example.sentiance_flutter.model.SentianceDataStatus
import com.sentiance.sdk.*

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar


/** FluttertoastPlugin */
class SentianceFlutterPlugin: FlutterPlugin, MethodCallHandler, ActivityAware {
  private lateinit var channel : MethodChannel
  private lateinit var activity:Activity
  private lateinit var context: Context
  private lateinit var sentianceToken : String
  private lateinit var sentianceUserId : String
  private lateinit var sentianceStartStatus : String
  private lateinit var sentiancedataStatus : SentianceDataStatus

  private val statusUpdateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      refreshStatus()
    }
  }

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.flutterEngine.dartExecutor, "sentiance_flutter")
    channel.setMethodCallHandler(this)
    this.context = flutterPluginBinding.applicationContext
  }

  companion object {
    @JvmStatic
    fun registerWith(registrar: Registrar) {
      val channel = MethodChannel(registrar.messenger(), "sentiance_flutter")
      channel.setMethodCallHandler(SentianceFlutterPlugin())
    }
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    if (call.method == "enableLocation") {
      // Toast.makeText(activity,"Hello!",Toast.LENGTH_SHORT).show()
      if (!PermissionManager(activity).getNotGrantedPermissions().isEmpty()) {
        val intent = Intent(activity, PermissionCheckActivity::class.java)
        intent.flags = FLAG_ACTIVITY_NEW_TASK
        startActivity(context,intent, null);
      }
      //result.success("Android ${android.os.Build.VERSION.RELEASE}")
    } else if(call.method == "intialiseSdk"){
      if (!PermissionManager(activity).getNotGrantedPermissions().isEmpty()) {
        val intent = Intent(activity, PermissionCheckActivity::class.java)
        intent.flags = FLAG_ACTIVITY_NEW_TASK
        startActivity(context,intent, null);
      }


      var data = call.arguments as HashMap<String, Any>

      var data1= data["data"] as HashMap<String,Any>

      if(data1["email"]!=null && data1["email"].toString().isNotEmpty()== true){


        val cache = Cache(activity)

          cache.setUserId(data1["email"].toString())
          cache.setUserToken(data1["token"].toString())
          cache.setAppSecret(data1["sentiance_secret"].toString())
          cache.setKeyAppId(data1["app_id"].toString())
          cache.setKeyUserLinkUrl(data1["user_link_url"].toString())
        SentianceWrapper(context).initializeSentianceSdk()
      }

    }else if(call.method == "getSentianceData"){
      if (Sentiance.getInstance(context).initState == InitState.INITIALIZED) {
        refreshStatus()
       // var data = SentianceDataModel(sentianceUserId:Sentiance.getInstance(context).userId,sentianceStartStatus:Sentiance.getInstance(context).sdkStatus.startStatus.name ,sentianceToken: sentianceToken)
        result.success(Sentiance.getInstance(context).sdkStatus.startStatus.name)
      }else{
        result.success("NOT_INITIALIZED")
      }
    }else if(call.method == "stopSdk"){
      if (Sentiance.getInstance(context).initState == InitState.INITIALIZED) {
        SentianceWrapper(context).stopSentianceSdk()
      }
    }else if(call.method == "startSdk"){
      if (Sentiance.getInstance(context).initState == InitState.INITIALIZED) {
        SentianceWrapper(context).startSentianceSdk()
      }else{

      }
    }else if(call.method == "statusSdk"){
      if (Sentiance.getInstance(context).initState == InitState.INITIALIZED) {
        var data = Sentiance.getInstance(context).sdkStatus;
        if(Sentiance.getInstance(context).sdkStatus == SdkStatus.StartStatus.STARTED){
          sentiancedataStatus = SentianceDataStatus(Sentiance.getInstance(context).userId, data.startStatus.name,
            data.canDetect, data.isRemoteEnabled, data.isLocationPermGranted,
            data.isActivityRecognitionPermGranted, data.isAirplaneModeEnabled, data.isLocationAvailable,
            data.isAccelPresent, data.isGyroPresent, data.isGpsPresent, data.isGooglePlayServicesMissing,
            data.isBatteryOptimizationEnabled, data.isBatterySavingEnabled,
            data.isBackgroundProcessingRestricted
          )
         result.success(sentiancedataStatus);
        }else{
          sentiancedataStatus = SentianceDataStatus(Sentiance.getInstance(context).userId, data.startStatus.name,
            data.canDetect, data.isRemoteEnabled, data.isLocationPermGranted,
            data.isActivityRecognitionPermGranted, data.isAirplaneModeEnabled, data.isLocationAvailable,
            data.isAccelPresent, data.isGyroPresent, data.isGpsPresent, data.isGooglePlayServicesMissing,
            data.isBatteryOptimizationEnabled, data.isBatterySavingEnabled,
            data.isBackgroundProcessingRestricted
          )
          result.success(sentiancedataStatus);
        }
      }else{
        result.success("NOT_INITIALIZED");
      }

    }else {
      result.notImplemented()
    }

  }

  fun refreshStatus() {
    if (Sentiance.getInstance(context).initState == InitState.INITIALIZED) {
      getToken()
      //sentianceUserId= Sentiance.getInstance(context)!.userId;
     // sentianceStartStatus = Sentiance.getInstance(context).sdkStatus.startStatus.name
     // Log.e("TAG", "refreshStatus: "+ SentainceDataModel(Sentiance.getInstance(context).userId, Sentiance.getInstance(this).sdkStatus.startStatus.name, sentianceToken).toJSON());
    }else{
      Log.e("TAG", "refreshStatus: notinittt " )
    }
  }

  //get token
  private fun getToken() {

    Sentiance.getInstance(context).getUserAccessToken(object : TokenResultCallback {
      override fun onSuccess(token: Token) {
        sentianceToken = token.tokenId.toString()
        Log.e("TAG", "onSuccess: "+sentianceToken )
        Log.e("TAG", "onSuccess: "+Sentiance.getInstance(context).userId )
        Log.e("TAG", "onSuccess: "+Sentiance.getInstance(context).sdkStatus.startStatus.name )
      }

      override fun onFailure() {
      }
    })

  }


  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }
  override fun onDetachedFromActivity() {}

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    onAttachedToActivity(binding)
  }
  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
//    (binding.lifecycle as HiddenLifecycleReference)
//      .lifecycle
//      .addObserver(LifecycleEventObserver { source, event ->
//        Log.e("Activity state: ", event.toString())
//       // updateActivityState(event.toString());
//      })
    this.activity = binding.activity

  }

//  private fun updateActivityState(event: String) {
//    when (event) {
//      "ON_RESUME" ->   //Log.e("Activity state: resume")  //LocalBroadcastManager.getInstance(context).registerReceiver(statusUpdateReceiver, IntentFilter(SdkStatusUpdateHandler.ACTION_SENTIANCE_STATUS_UPDATE))
//
//      "ON_PAUSE" ->      Log.e("Activity state: pause")//LocalBroadcastManager.getInstance(context).unregisterReceiver(statusUpdateReceiver)
//
//
//      else -> Log.e("TAG", "updateActivityState:  default" )
//    }
//  }

  override fun onDetachedFromActivityForConfigChanges() {}
}
