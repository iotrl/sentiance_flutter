package com.example.sentiance_flutter;

import org.json.JSONObject;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.Nullable;

class Cache {
    private static final String PREF_NAME = "app";
    private static final String KEY_AUTH_TOKEN = "token";
    private static final String KEY_INITIALIZE = "initialize";
    private static final String KEY_APP_SECRET = "app_secret";
    private static final String KEY_APP_ID = "app_id";
    private static final String KEY_USER_LINK_URL = "user_link_url";
    private static final String KEY_MOBILE_HEALTH_URL = "mobile_health_url";
    private static final String KEY_CRASH_DETECTION_URL = "crash_detection_url";
    private static final String KEY_USER_INSTALL_ID = "user_install_id";
    private static final String KEY_USER_MOBILE_HEALTH_DATA ="mobile_health_data";
    private static final String KEY_CUSTOMER_ID = "customer_id";
    private static final String KEY_USER_ID ="user_id";

    private Context mContext;

    

    Cache(Context context) {
        mContext = context;
    }

    @Nullable
    String getKeyUserLinkUrl() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_USER_LINK_URL, null);
    }

    void setKeyUserLinkUrl(String url) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(KEY_USER_LINK_URL, url).apply();
    }

    @Nullable
    String getMobileHealthUrl() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_MOBILE_HEALTH_URL, null);
    }

    void setMobileHealthUrl(String mobileHealthUrl) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(KEY_MOBILE_HEALTH_URL, mobileHealthUrl).apply();
    }

    @Nullable
    String getCrashDetectionUrl() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_CRASH_DETECTION_URL, null);
    }

    void setCrashDetectionUrl(String crashDetectionUrl) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(KEY_CRASH_DETECTION_URL, crashDetectionUrl).apply();
    }

    @Nullable
    String getUserToken() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_AUTH_TOKEN, null);
    }

    void setUserToken(String token) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(KEY_AUTH_TOKEN, token).apply();
    }

    @Nullable
    String getInstallId() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_USER_INSTALL_ID, null);
    }

    void setInstallId(String installId) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(KEY_USER_INSTALL_ID, installId).apply();
    }

    @Nullable
    String getAppId() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_APP_ID, null);
    }

    void setKeyAppId(String appId) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(KEY_APP_ID, appId).apply();
    }

    @Nullable
    String getAppSecret() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_APP_SECRET, null);
    }

    void setAppSecret(String secret) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(KEY_APP_SECRET, secret).apply();
    }

    @Nullable
    String getInitialize() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_INITIALIZE, null);
    }

    void setInitialize(String initialize) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(KEY_INITIALIZE, initialize).apply();
    }

    void setMobileHealthData(JSONObject data)
    {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(KEY_USER_MOBILE_HEALTH_DATA,data.toString()).apply();
    }

    String getMobileHealthData()
    {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_USER_MOBILE_HEALTH_DATA, null);
    }
    
    void setCustomerId(String customerId)
    {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(KEY_CUSTOMER_ID, customerId).apply(); 
    }
    String getCustomerId()
    {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_CUSTOMER_ID, null);
    }
    void setUserId(String customerId)
    {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(KEY_USER_ID, user_id).apply(); 
    }
    String getUserId()
    {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_USER_ID, null);
    }
}

