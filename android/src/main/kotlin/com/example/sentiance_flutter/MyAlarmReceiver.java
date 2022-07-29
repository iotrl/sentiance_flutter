package com.example.sentiance_flutter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.multidex.BuildConfig;

import com.sentiance.sdk.InitState;
import com.sentiance.sdk.SdkStatus;
import com.sentiance.sdk.Sentiance;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MyAlarmReceiver extends BroadcastReceiver {
    Cache mCache;
    
    @Override
    public void onReceive(Context context, Intent intent) {
        mCache = new Cache(context);

        if (Sentiance.getInstance(context).getInitState() == InitState.INITIALIZED)  {
            updateToServer(context,Sentiance.getInstance(context).getSdkStatus());
        }else{
            Log.e("TAG", "onReceive: sdk not initialized" + Sentiance.getInstance(context).getInitState());
        }
    }

    private void updateToServer(Context context,SdkStatus sdkStatus) {
        OkHttpClient client = new OkHttpClient();
        SdkStatus sdkstats = sdkStatus;

        Boolean isAutoStartAvl = Autostart.isXiaomi();
        Boolean isAutoStartEnabled = false;
        try {
            isAutoStartEnabled = getAutoStartStatus(context);
        } catch (Exception e) {
            isAutoStartEnabled=false;
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("isGooglePlayServicesMissing", sdkstats.isGooglePlayServicesMissing );
            jsonObject.put("BRAND", Build.BRAND );
            jsonObject.put("DEVICE", Build.DEVICE );
            jsonObject.put("MANUFACTURER", Build.MANUFACTURER );
            jsonObject.put("SDKstartStatus", sdkstats.startStatus );
            jsonObject.put("androidsdkVersion", Build.VERSION.RELEASE );
            jsonObject.put("appVersion", BuildConfig.VERSION_NAME );
            jsonObject.put("canDetect", sdkstats.canDetect );
            jsonObject.put("diskQuotaStatus", sdkstats.diskQuotaStatus );
            jsonObject.put("isAccelPresent", sdkstats.isAccelPresent );
            jsonObject.put("isActivityRecognitionPermGranted", sdkstats.isActivityRecognitionPermGranted );
            jsonObject.put("isAirplaneModeEnabled", sdkstats.isAirplaneModeEnabled );
            jsonObject.put("isBackgroundProcessingRestricted", sdkstats.isBackgroundProcessingRestricted );
            jsonObject.put("isBatteryOptimizationEnabled", sdkstats.isBatteryOptimizationEnabled );
            jsonObject.put("isBatterySavingEnabled", sdkstats.isBatterySavingEnabled );
            jsonObject.put("isGpsPresent", sdkstats.isGpsPresent );
            jsonObject.put("isGyroPresent", sdkstats.isGyroPresent );
            jsonObject.put("isLocationAvailable", sdkstats.isLocationAvailable );
            jsonObject.put("isLocationPermGranted", sdkstats.isLocationPermGranted );
            jsonObject.put("isRemoteEnabled", sdkstats.isRemoteEnabled );
            jsonObject.put("locationSetting", sdkstats.locationSetting );
            jsonObject.put("mobileQuotaStatus", sdkstats.mobileQuotaStatus );
            jsonObject.put("modelName", Build.MODEL );
            jsonObject.put("osVersion", Build.VERSION.SDK_INT);
            jsonObject.put("install_id", mCache.getInstallId() );
            jsonObject.put("sentiance_user_id", Sentiance.getInstance(context).getUserId() );
            jsonObject.put("sdkUserID", Sentiance.getInstance(context).getUserId());
            jsonObject.put("wifiQuotaStatus", sdkstats.wifiQuotaStatus );
            jsonObject.put("isAutoStartAvailable", isAutoStartAvl);
            jsonObject.put("isAutoStartEnabled", isAutoStartEnabled);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.i("TAG", "Receiver" + jsonObject);
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, jsonObject.toString());

        Request request1 = new Request.Builder()
                .url(mCache.getMobileHealthUrl())
                .header("Authorization", mCache.getUserToken())
                .post(body)
                .build();

        client.newCall(request1).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("fail", e.toString());
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.e("updated mobile api", response.body().string());
            }
        });
    }

    public boolean getAutoStartStatus (Context context) throws Exception {

        if(Autostart.isXiaomi()){
            Autostart autostart = new Autostart(context);
            Autostart.State state = autostart.getAutoStartState();

            if (state == Autostart.State.ENABLED) {
                // now we are also sure that autostart is enabled
                return true;
            }
        }
        return false;
    }
}
