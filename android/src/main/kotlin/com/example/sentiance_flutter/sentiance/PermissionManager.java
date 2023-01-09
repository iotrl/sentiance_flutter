package com.example.sentiance_flutter.sentiance;

import android.Manifest;
import android.app.Activity;
import android.os.Build;
import androidx.annotation.RequiresApi;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PermissionManager {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 15440;
    private static final int FG_LOCATION_PERMISSION_REQUEST_CODE = 15441;
    private static final int BG_LOCATION_PERMISSION_REQUEST_CODE = 15442;
    private static final int ACTIVITY_PERMISSION_REQUEST_CODE = 15443;
    private static final String TITLE_LOCATION = "Location permission";
    private static final String MESSAGE_LOCATION = "SafetyConnect will use your location in the background to provide insights that help improve Driving Safety.";
    private static final String TITLE_ACTIVITY = "Motion activity permission";
    private static final String MESSAGE_ACTIVITY = "The EHS app needs access to your activity data in order to build your profile.";

    private Activity mActivity;

    public PermissionManager (Activity activity) {
        mActivity = activity;

        updateCanShowAgainPermissions();
    }

    public List<Permission> getNotGrantedPermissions() {
        List<Permission> notGrantedPermissions = new ArrayList<>();

        for (Permission p : getAllPermissions()) {
            if (!p.isGranted(mActivity) && p.getCanShowAgain(mActivity)) {
                notGrantedPermissions.add(p);
            }
        }

        return notGrantedPermissions;
    }

    private void updateCanShowAgainPermissions () {
        for (Permission p : getAllPermissions()) {
            if (p.isGranted(mActivity)) {
                // Permission is granted. Reset the show rationale and can show again prefs.
                p.setCanShowAgain(mActivity, true);
                p.clearShowRationale(mActivity);
                continue;
            }

            if (!p.shouldShowRationale(mActivity)) {
                if (p.isShowRationaleSet(mActivity)) {
                    // We were allowed to show a rationale before, but not anymore.
                    // This is a result of the "don't ask again" option selected by the user.
                    p.setShowRationale(mActivity, false);
                    p.setCanShowAgain(mActivity, false);
                }
            }
            else {
                // We can show a rational. This is when our permission request
                // was shot down by the user the first time we asked.
                p.setShowRationale(mActivity, true);
            }
        }
    }

    private boolean isQPlus() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
    }

    private boolean isRPlus() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R;
    }

    private List<Permission> getAllPermissions () {
        List<Permission> list = new ArrayList<>();

        if (isRPlus()) {
            list.addAll(getRPlusLocationPermissions());
        } else {
            list.addAll(getPreRLocationPermissions());
        }

        // if (isQPlus()) {
        //     list.add(new Permission("Activity", new String[] {Manifest.permission.ACTIVITY_RECOGNITION},
        //             ACTIVITY_PERMISSION_REQUEST_CODE,
        //             TITLE_ACTIVITY, MESSAGE_ACTIVITY));
        // }


        return list;
    }
    private List<Permission> getPreRLocationPermissions() {
        List<Permission> permissions = new ArrayList<>();

        String[] permissionStrings = isQPlus() ? new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
        } : new String[] { Manifest.permission.ACCESS_FINE_LOCATION };

        permissions.add(new Permission("Location", permissionStrings, LOCATION_PERMISSION_REQUEST_CODE,
                TITLE_LOCATION, MESSAGE_LOCATION));
        return permissions;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private List<Permission> getRPlusLocationPermissions() {
        List<Permission> permissions = new ArrayList<>();

        Permission fgLocation = new Permission("Foreground Location",
                new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                FG_LOCATION_PERMISSION_REQUEST_CODE, TITLE_LOCATION, MESSAGE_LOCATION);

        // The background location permission has a dependency on the foreground location permission.
        Permission bgLocation = new Permission("Background Location",
                new String[] { Manifest.permission.ACCESS_BACKGROUND_LOCATION },
                BG_LOCATION_PERMISSION_REQUEST_CODE, TITLE_LOCATION, MESSAGE_LOCATION,
                Collections.singletonList(fgLocation));

        permissions.add(fgLocation);
        permissions.add(bgLocation);
        return permissions;
    }


    public void bgPermission(){
        ActivityCompat.requestPermissions(this,
                                    new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_PERMISSION_CODE);
    }



    }





