package com.example.sentiance_flutter;

import static com.sentiance.sdk.InitState.NOT_INITIALIZED;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.multidex.BuildConfig;

import com.sentiance.sdk.UserLinker;
import com.sentiance.sdk.OnInitCallback;
import com.sentiance.sdk.OnSdkStatusUpdateHandler;
import com.sentiance.sdk.OnStartFinishedHandler;
import com.sentiance.sdk.ResetCallback;
import com.sentiance.sdk.SdkConfig;
import com.sentiance.sdk.SdkStatus;
import com.sentiance.sdk.Sentiance;
import com.sentiance.sdk.Token;
import com.sentiance.sdk.TokenResultCallback;
import com.sentiance.sdk.crashdetection.api.CrashDetectionApi;
import com.sentiance.sdk.crashdetection.api.VehicleCrashEvent;
import com.sentiance.sdk.crashdetection.api.VehicleCrashListener;


// import com.sentiance.sdk.ondevicefull.crashdetection.VehicleCrashEvent;
// import com.sentiance.sdk.ondevicefull.crashdetection.VehicleCrashListener;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.flutter.plugin.common.MethodChannel;

import me.weishu.reflection.Reflection;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SentianceWrapper
        implements UserLinker, OnSdkStatusUpdateHandler, OnInitCallback, OnStartFinishedHandler {

    private static final String TAG = "SentianceWrapper";
    public static final String ACTION_SDK_STATUS_UPDATED = "com.sentiance.ACTION_SDK_STATUS_UPDATED";
    public static final String ACTION_INIT_SUCCEEDED = "com.sentiance.ACTION_INIT_SUCCEEDED";
    public static final String ACTION_INIT_FAILED = "com.sentiance.ACTION_INIT_FAILED";
    public static final String ACTION_INIT_RESET = "com.sentiance.ACTION_INIT_FAILED";

    private Context mContext;
    private Cache mCache;
    String _installId = "";
    private SimpleDateFormat dateFormatter;
    String UserLinkStatus = "";

    public SentianceWrapper(Context context) {
        dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.ENGLISH);
        mContext = context.getApplicationContext();
        mCache = new Cache(context);
    }

    public void initializeSentianceSdk() {
        if (mCache.getUserToken() == null) {
            // Cannot initialize the SDK since the user has not logged in yet.
            return;
        }

        if (mCache.getAppSecret() == null) {
            // Cannot initialize the SDK since the app secret is missing.
            return;
        }
        if (mCache.getAppId() == null) {
            // Cannot initialize the SDK since the app Id is missing.
            return;
        }
        if (mCache.getKeyUserLinkUrl() == null) {
            // Cannot initialize the SDK since the user link url is missing.
            return;
        }

        Log.e(TAG, "link: " + mCache.getAppId());
        Log.e(TAG, "link: " + mCache.getAppSecret());

        // Create the config.
        SdkConfig config = new SdkConfig.Builder(mCache.getAppId(), mCache.getAppSecret(), createNotification())
                .setOnSdkStatusUpdateHandler(this)
                .setUserLinker(this) // pass your implementation of the linker here
                .build();

        if (Sentiance.getInstance(mContext).getInitState() == NOT_INITIALIZED) {
            Log.e(TAG, "initializeSentianceSdk: " + Sentiance.getInstance(mContext).getInitState());

            Sentiance.getInstance(mContext).init(config, this);
        } else {
            Log.e(TAG, "initializeSentianceSdk: " + Sentiance.getInstance(mContext).getInitState());

        }
    }

    public void stopSentianceSdk() {
        Sentiance.getInstance(mContext).reset(new ResetCallback() {
            @Override
            public void onResetSuccess() {
                Log.e(TAG, "Sentiance SDK was successfully reset");

            }

            @Override
            public void onResetFailure(ResetFailureReason reason) {
                Log.e(TAG, "Sentiance SDK reset failed with reason " + reason.name());
            }
        });

    }

    public void startSentianceSdk() {
        Sentiance.getInstance(mContext).start(this);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(ACTION_SDK_STATUS_UPDATED));
    }

    @Override
    public void onInitSuccess() {
        // printInitSuccessLogStatements();

        mCache.setInitialize("initialized");
        // Sentiance SDK was successfully initialized, we can now start it.

        Sentiance.getInstance(mContext).start(this);
        // initialze crash Detection

        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(ACTION_INIT_SUCCEEDED));

    }

    @Override
    public void onInitFailure(InitIssue initIssue, @Nullable Throwable throwable) {
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(ACTION_INIT_FAILED));
        Log.e(TAG, "Could not initialize SDK: " + initIssue);

        switch (initIssue) {
            case INVALID_CREDENTIALS:
                Log.e(TAG, "Make sure SENTIANCE_APP_ID and SENTIANCE_SECRET are set correctly.");
                break;
            case CHANGED_CREDENTIALS:
                Log.e(TAG,
                        "The app ID and secret have changed; this is not supported. If you meant to change the app credentials, please uninstall the app and try again.");
                break;
            case SERVICE_UNREACHABLE:
                Log.e(TAG,
                        "The Sentiance API could not be reached. Double-check your internet connection and try again.");
                break;
            case LINK_FAILED:
                Log.e(TAG, "An issue was encountered trying to link the installation ID to the metauser.");
                break;
            case INITIALIZATION_ERROR:
                Log.e(TAG, "An unexpected exception or an error occurred during initialization.", throwable);
                break;
        }
    }

    @Override
    public void onStartFinished(SdkStatus sdkStatus) {
        Log.i(TAG, "SDK start finished with status: " + sdkStatus.startStatus);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(ACTION_SDK_STATUS_UPDATED));
    }

    @Override
    public void onSdkStatusUpdate(SdkStatus sdkStatus) {
        Log.i(TAG, "SDK status updated:" + sdkStatus.toString());

        // The status update is broadcast internally; this is so the other components of
        // the app
        // (specifically MainActivity) can react on this.
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(ACTION_SDK_STATUS_UPDATED));
        updateToServer(sdkStatus);

    }

    private void initializeCrashDetection() {

        CrashDetectionApi.getInstance(mContext).setVehicleCrashListener(new VehicleCrashListener() {


            @Override
            public void onVehicleCrash(VehicleCrashEvent crashEvent) {
                // Handle the vehicle crash event
                Location location = crashEvent.getLocation();
                String time = convertEpocTODateTime(crashEvent.getTime());
                String lat = String.valueOf(location.getLatitude());
                String lon = String.valueOf(location.getLongitude());
                saveCrashDetectionData(time, lat, lon);

            }
        });

    }

    public void createDummyCrash() {
        CrashDetectionApi.getInstance(mContext).invokeDummyVehicleCrash();
    }

    private void saveCrashDetectionData(String time, String lat, String lon) {

        OkHttpClient client = new OkHttpClient();

        String jsonBody = "{\"userId\":\"" + mCache.getUserId() + "\",\"customerId\":\"" + mCache.getCustomerId()
                + "\",\"location\":{\"lat\":\"" + lat + "\",\"lng\":\"" + lon + "\"},\"time\":\"" + time + "\"}";
        Log.e("Crash Detection Json", jsonBody);

        Request request1 = new Request.Builder()
                .url(mCache.getCrashDetectionUrl())
                .header("Authorization", getAuthHeader())
                .post(RequestBody.create(MediaType.parse("application/json"), jsonBody))
                .build();

        Log.e("Crash Detection data pri -", mCache.getCrashDetectionUrl());

        client.newCall(request1).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, e.toString());
                Log.e("CrashDetectionF", "Failed");

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.w(TAG, response.body().string());

                Log.e("CrashDetectionRes", "Success");
            }

        });
    }

    private String convertEpocTODateTime(long time) {
        String date = new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new java.util.Date(time));
        return date;
    }

    private void updateToServer(SdkStatus sdkStatus) {

        OkHttpClient client = new OkHttpClient();
        SdkStatus sdkstats = sdkStatus;

        Boolean isAutoStartAvl = Autostart.isXiaomi();
        Boolean isAutoStartEnabled = false;
        try {
            isAutoStartEnabled = getAutoStartStatus();
        } catch (Exception e) {
            isAutoStartEnabled = false;
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("isGooglePlayServicesMissing", sdkstats.isGooglePlayServicesMissing);
            jsonObject.put("BRAND", Build.BRAND);
            jsonObject.put("DEVICE", Build.DEVICE);
            jsonObject.put("MANUFACTURER", Build.MANUFACTURER);
            jsonObject.put("SDKstartStatus", sdkstats.startStatus);
            jsonObject.put("androidsdkVersion", Build.VERSION.RELEASE);
            jsonObject.put("appVersion", BuildConfig.VERSION_NAME);
            jsonObject.put("canDetect", sdkstats.canDetect);
            jsonObject.put("diskQuotaStatus", sdkstats.diskQuotaStatus);
            jsonObject.put("isAccelPresent", sdkstats.isAccelPresent);
            jsonObject.put("isActivityRecognitionPermGranted", sdkstats.isActivityRecognitionPermGranted);
            jsonObject.put("isAirplaneModeEnabled", sdkstats.isAirplaneModeEnabled);
            jsonObject.put("isBackgroundProcessingRestricted", sdkstats.isBackgroundProcessingRestricted);
            jsonObject.put("isBatteryOptimizationEnabled", sdkstats.isBatteryOptimizationEnabled);
            jsonObject.put("isBatterySavingEnabled", sdkstats.isBatterySavingEnabled);
            jsonObject.put("isGpsPresent", sdkstats.isGpsPresent);
            jsonObject.put("isGyroPresent", sdkstats.isGyroPresent);
            jsonObject.put("isLocationAvailable", sdkstats.isLocationAvailable);
            jsonObject.put("isLocationPermGranted", sdkStatus.locationPermission);
            jsonObject.put("isRemoteEnabled", sdkstats.isRemoteEnabled);
            jsonObject.put("locationSetting", sdkstats.locationSetting);
            jsonObject.put("mobileQuotaStatus", sdkstats.mobileQuotaStatus);
            jsonObject.put("modelName", Build.MODEL);
            jsonObject.put("osVersion", Build.VERSION.SDK_INT);
            jsonObject.put("install_id", mCache.getInstallId());
            jsonObject.put("sentiance_user_id", mCache.getInstallId());
            jsonObject.put("sdkUserID", Sentiance.getInstance(mContext).getUserId());
            jsonObject.put("wifiQuotaStatus", sdkstats.wifiQuotaStatus);
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
                UserLinkStatus = "User linking Failed";
                Log.e("fail", e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                UserLinkStatus = "User linking Success";
                Log.e("updated mobile api", response.body().string());
                mCache.setMobileHealthData(jsonObject);
            }
        });

    }

    private Notification createNotification() {

        String channelId = "trips";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Trips",
                    NotificationManager.IMPORTANCE_MIN);
            channel.setShowBadge(false);
            NotificationManager notificationManager = (NotificationManager) mContext
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }

        return new NotificationCompat.Builder(mContext, channelId)
                .setContentTitle("Safetyconnect" + " is running")
                .setContentText("Touch to open.")
                .setShowWhen(false)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .build();
    }

    // print success log statements
    private void printInitSuccessLogStatements() {
        Log.i(TAG, "Sentiance SDK initialized, version: " + Sentiance.getInstance(mContext).getVersion());
        Log.i(TAG, "Sentiance platform user id for this install: " + Sentiance.getInstance(mContext).getUserId());

        OkHttpClient client = new OkHttpClient();

        String jsonBody = "{ \"installId\": \"" + Sentiance.getInstance(mContext).getUserId() + "\"}";
        Log.e(TAG, "sentiance json " + jsonBody);

        Request request1 = new Request.Builder()
                .url(mCache.getKeyUserLinkUrl())
                .header("Authorization", getAuthHeader())
                .put(RequestBody.create(MediaType.parse("application/json"), jsonBody))
                .build();

        client.newCall(request1).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, e.toString());

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.w(TAG, response.body().string());

            }

        });

        Sentiance.getInstance(mContext).getUserAccessToken(new TokenResultCallback() {
            @Override
            public void onSuccess(Token token) {

                Log.i(TAG, "Access token to query the HTTP API: Bearer " + token.getTokenId());
                // Using this token, you can query the Sentiance API.
            }

            @Override
            public void onFailure() {
                Log.e(TAG, "Couldn't get access token");
            }
        });
    }

    @Override
    public boolean link(String installId) {
        _installId = installId;
        mCache.setInstallId(installId);

        Log.e(TAG, "link: " + _installId);
        String jsonBody = "{ \"installId\": \"" + installId + "\"}";
        Log.e(TAG, "link: " + jsonBody);
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, jsonBody);
        Log.e(TAG, "link: " + body.toString());
        Request request = new Request.Builder()
                .url(mCache.getKeyUserLinkUrl())
                .put(body)
                .header("Authorization", mCache.getUserToken())
                .addHeader("Content-Type", "application/json")
                .build();
        Log.e(TAG, "link: " + request);
        try {
            Response response = getClient().newCall(request).execute();
            Log.e(TAG, "link: " + response);
            return response.isSuccessful();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage() + " failure" + "\n" + Log.getStackTraceString(e));
        }
        return false;
    }

    private String getAuthHeader() {
        return mCache.getUserToken(); // replace with your app's authorization token
    }

    private OkHttpClient getClient() {
        return new OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    public void getStatus(@NotNull MethodChannel.Result result) {
        result.success(UserLinkStatus);
    }

    public boolean getAutoStartStatus() throws Exception {

        if (Autostart.isXiaomi()) {
            Autostart autostart = new Autostart(mContext);
            Autostart.State state = autostart.getAutoStartState();

            if (state == Autostart.State.ENABLED) {
                // now we are also sure that autostart is enabled
                return true;
            }
        }
        return false;
    }

    public boolean getAutoStartAvl() throws Exception {
        if (Autostart.isXiaomi()) {
            return true;
        }
        return false;
    }
}

// package xyz.kumaraswamy.autostart;
//
// import android.content.Context;
// import android.os.Build;
// import android.util.Log;
// import me.weishu.reflection.Reflection;
// import org.jetbrains.annotations.Nullable;
//
// import java.lang.reflect.Field;
// import java.lang.reflect.Method;
// import java.util.ArrayList;

@SuppressWarnings("unused")
class Autostart {

    private static final String TAG = "Autostart";

    private static final String XIAOMI_NAME = "xiaomi";

    private static final String MIUI_CLAZZ = "android.miui.AppOpsUtils";
    private static final String POLICY_CLAZZ = "miui.content.pm.PreloadedAppPolicy";

    private static boolean isReflectionEnabled = false;

    private static final int STATE_ENABLED = 0;
    private static final int STATE_DISABLED = 1;

    private final Context context;

    /**
     * State for the autostart
     * ENABLED and DISABLED are the expected results for a MIUI device
     */

    public enum State {
        ENABLED, DISABLED, NO_INFO, UNEXPECTED_RESULT
    }

    public static boolean isXiaomi() {
        return Build.MANUFACTURER.equalsIgnoreCase(XIAOMI_NAME);
    }

    /**
     * Create an instance of Autostart, the phone must be Xiaomi device
     *
     * @param context Application context
     * @throws Exception phone is not Xiaomi device!
     */

    public Autostart(Context context) throws Exception {
        if (!isXiaomi()) {
            throw new Exception("Not a Xiaomi device");
        }
        this.context = context;

        if (!isReflectionEnabled) {
            Reflection.unseal(context);
            isReflectionEnabled = true;
        }
    }

    /**
     * Checks if autostart is enabled.
     * May also return false (even if its enabled)
     */

    public boolean isAutoStartEnabled() throws Exception {
        return getAutoStartState() == State.ENABLED;
    }

    /**
     * Returns the state of the autostart
     *
     * <p>
     * ENABLED / DISABLED = autostart state
     * NOINFO = something went wrong when attempting to get the state
     * UNEXPECTED_RESULT = we got the state, but it is not right
     * </p>
     */

    public State getAutoStartState() throws Exception {
        final Class<?> clazz = getClazz(MIUI_CLAZZ);

        if (clazz == null) {
            return State.NO_INFO;
        }

        final Method method = findMethod(clazz);

        if (method == null) {
            // the method does not exist!
            return State.NO_INFO;
        }

        final Object result = method.invoke(null,
                context, context.getPackageName());

        // it must be an integer, else things are changed

        if (!(result instanceof Integer)) {
            return State.UNEXPECTED_RESULT;
        }
        final int _int = (int) result;
        if (_int == STATE_ENABLED) {
            return State.ENABLED;
        } else if (_int == STATE_DISABLED) {
            return State.DISABLED;
        }
        return State.UNEXPECTED_RESULT;
    }

    @SuppressWarnings("SuspiciousToArrayCall")
    public String[] defaultWhiteListedPackages() throws NoSuchFieldException, IllegalAccessException {
        final Class<?> clazz = getClazz(POLICY_CLAZZ);
        if (clazz == null) {
            // just return an empty array
            return new String[0];
        }
        final Field field = clazz.getDeclaredField("sProtectedDataApps");
        field.setAccessible(true);

        // we pass null to `Field.get()` because the field
        // is statically defined
        final Object result = field.get(null);
        if (result instanceof ArrayList<?>) {
            return ((ArrayList<?>) result).toArray(new String[0]);
        }
        final String message = "defaultWhiteListedPackages() unexpected result type";
        if (result == null) {
            return new String[0];
        }
        Log.e(TAG, message + " " + result.getClass());
        return new String[0];
    }

    private Class<?> getClazz(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException ignored) {
            // we couldn't find the class name
            // we are out of luck
            return null;
        }
    }

    /**
     * Finds the method of the MIUI clazz
     *
     * @return returns the method, is null
     *         if method is not found
     */

    private @Nullable Method findMethod(final Class<?> clazz) {
        final String methodName = "getApplicationAutoStart";

        try {
            final Method method = clazz.getDeclaredMethod("getApplicationAutoStart",
                    Context.class, String.class);

            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.getName().equals(methodName)) {
                    Log.i(TAG, "Found a new method matching method name");
                }
            }
            // we could not find the method name
            // we were near to get the autostart
            return null;
        }
    }
}
