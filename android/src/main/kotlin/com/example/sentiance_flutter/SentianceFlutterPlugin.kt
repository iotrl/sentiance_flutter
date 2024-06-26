package com.example.sentiance_flutter

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context

import org.json.JSONObject;
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Build
import android.util.Log
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.startActivity
import com.example.sentiance_flutter.sentiance.PermissionCheckActivity
import com.example.sentiance_flutter.sentiance.PermissionManager
import com.sentiance.sdk.*
import io.flutter.embedding.android.FlutterActivity

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar
import com.sentiance.sdk.Sentiance
import com.sentiance.sdk.usercreation.UserCreationOptions


/** FluttertoastPlugin */
class SentianceFlutterPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {
    private lateinit var channel: MethodChannel
    private lateinit var activity: Activity
    private lateinit var context: Context
    private lateinit var sentianceToken: String

    private val statusUpdateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            refreshStatus()
        }
    }

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel =
            MethodChannel(flutterPluginBinding.flutterEngine.dartExecutor, "sentiance_flutter")
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
 

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        Log.e("LogS", "onMethodCall");
        if (call.method == "enableLocation") {
            if (!PermissionManager(activity).getNotGrantedPermissions().isEmpty()) {
                val intent = Intent(activity, PermissionCheckActivity::class.java)
                intent.flags = FLAG_ACTIVITY_NEW_TASK
                startActivity(context, intent, null);
            }
            
        } else if (call.method == "intialiseSdk") {
            

            var data = call.arguments as HashMap<String, Any>

            var data1 = data["data"] as HashMap<String, Any>

            if (data1["token"] != null && data1["token"].toString().isNotEmpty() == true) {

                val cache = Cache(activity)
                cache.setUserToken(data1["token"].toString())
                cache.setAppSecret(data1["sentiance_secret"].toString())
                cache.setKeyAppId(data1["app_id"].toString())
                cache.setKeyUserLinkUrl(data1["user_link_url"].toString())
                cache.setMobileHealthUrl(data1["mobile_health_url"].toString())
                cache.setCrashDetectionUrl(data1["crash_detection_url"].toString())
                cache.setUserId(data1["user_id"].toString())
                cache.setCustomerId(data1["customer_id"].toString())
                SentianceWrapper(context).initializeSentianceSdk()
                SentianceWrapper(context).getStatus(result)
                result.success(Sentiance.getInstance(context).sdkStatus.detectionStatus.name)
            }
          

        } else if (call.method == "getSentianceData") {
            if (Sentiance.getInstance(context).initState == InitState.INITIALIZED) {
                refreshStatus()
             
                result.success(Sentiance.getInstance(context).sdkStatus.detectionStatus.name)
            } else {
              
                result.success("NOT_INITIALIZED")
            }
        } else if (call.method == "stopSdk") {
         
            SentianceWrapper(context).stopSentianceSdk()
            result.success(Sentiance.getInstance(context).sdkStatus.detectionStatus.name)
        }else if (call.method == "getPermissions") {

            if (!PermissionManager(activity).getNotGrantedPermissions().isEmpty()) {
                val intent = Intent(activity, PermissionCheckActivity::class.java)
                intent.flags = FLAG_ACTIVITY_NEW_TASK
                startActivity(context, intent, null);
            }
           
            result.success(Sentiance.getInstance(context).sdkStatus.detectionStatus.name)
        } else if (call.method == "startSdk") {
          
            if (Sentiance.getInstance(context).initState == InitState.INITIALIZED) {
                SentianceWrapper(context).startSentianceSdk()
                result.success(Sentiance.getInstance(context).sdkStatus.detectionStatus.name)
            } else {
              
                result.success("NOT_INITIALIZED")
            }
        } else if (call.method == "statusSdk") {
         
            if (Sentiance.getInstance(context).initState == InitState.INITIALIZED) {
               
                var data = Sentiance.getInstance(context).sdkStatus;
                result.success(data);
            } else {
             
                result.success("NOT_INITIALIZED");
            }

        }
        else if (call.method == "createUser") {
            val authenticationCode = call.argument<String>("authenticationCode")
            if (authenticationCode != null) {
                createUserWithSentiance(context, authenticationCode)
                //createUser(authenticationCode)
                result.success("User creation initiated")
            } else {
                result.error("INVALID_ARGUMENT", "Authentication code is required", null)
            }
        }
        else if(call.method == "dummycrash")
        {
            SentianceWrapper(context).createDummyCrash();
            result.success("dummycrash");

        }
        else if(call.method == "getMobileHealthData")
        {
        
          if (Sentiance.getInstance(context).initState == InitState.INITIALIZED) {

           val cache = Cache(activity)
           var data = cache.getMobileHealthData();
          
            result.success(data);
          } else {
          
              result.success("NOT_INITIALIZED")
          }
        }
        else if(call.method == "disableBatteryOptimization"){
        
         Sentiance.getInstance(context).disableBatteryOptimization();
            result.success("true");
            
        }
        else if(call.method == "sdkStartStatus"){
        
            result.success(Sentiance.getInstance(context).sdkStatus.startStatus.name);
             
         }
        // else if(call.method == "getAutoStartStatus"){
        //     result.success(SentianceWrapper(context).getAutoStartStatus());
            
        // }
        // else if(call.method == "getAutoStartAvl"){
        //     result.success(SentianceWrapper(context).getAutoStartAvl());
            
        // }

        else {
            
            result.notImplemented()
        }

    }

    fun createUserWithSentiance(context: Context, authenticationCode: String) {
    val options = UserCreationOptions.Builder(authenticationCode).build()

    Sentiance.getInstance(context).createUser(options).addOnCompleteListener { operation ->
        if (operation.isSuccessful) {
            val userInfo = operation.result.userInfo
            Log.e("TAG",  "Created a user with ID: ${userInfo.userId}")
        } else {
            val error = operation.error
            Log.e("TAG", "User creation failed with reason ${error.reason.name}. Details: ${error.details}")
        }
    }
}


    fun refreshStatus() {
        if (Sentiance.getInstance(context).initState == InitState.INITIALIZED) {
            getToken()
            updateDataToApi();
        } else {
            Log.e("TAG", "refreshStatus: NOT_INITIALIZED")
        }
    }

    fun updateDataToApi() {

        val alarmManager = context.getSystemService(FlutterActivity.ALARM_SERVICE) as AlarmManager
        val i = Intent(context, MyAlarmReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE);

        if (Build.VERSION.SDK_INT >= 19) {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis(),
                60 * 60 * 1000,
                pendingIntent
            );
            Log.e("TAG", "onCreate: ");
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pendingIntent);
        }
    }

    //get token
    private fun getToken() {

        Sentiance.getInstance(context).getUserAccessToken(object : TokenResultCallback {
            override fun onSuccess(token: Token) {
                sentianceToken = token.tokenId.toString()
                Log.e("TAG", "onSuccess: " + sentianceToken)
                Log.e("TAG", "onSuccess: " + Sentiance.getInstance(context).userId)
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
        this.activity = binding.activity

    }


    override fun onDetachedFromActivityForConfigChanges() {}
}

