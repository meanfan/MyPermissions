package com.mean.mypermissions.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.mean.mypermissions.bean.AppConfig;
import com.mean.mypermissions.bean.PermissionConfigs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PermissionUtil {
    public static final String TAG = "PermissionUtil";
    public static List<PackageInfo> getAllAppInfo(Context context) {
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> packages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS);
        return packages;
    }

    public static Drawable getAppIcon(Context context, String appPackageName) {
        PackageManager pm = context.getPackageManager();
        try {
            Drawable drawable = pm.getApplicationIcon(appPackageName);
            return drawable;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    public static String getAppName(Context context, ApplicationInfo info) {
        PackageManager pm = context.getPackageManager();
         return pm.getApplicationLabel(info).toString();
    }

    public static List<AppConfig> getAllUserAppConfigs(Context context) {
        List<PackageInfo> packageInfos = getAllAppInfo(context);
        List<AppConfig> appConfigs = new ArrayList<>();
        for(PackageInfo packageInfo:packageInfos){
            //TODO 跳过系统应用
//            if(packageInfo.applicationInfo.flags == ApplicationInfo.FLAG_SYSTEM ||
//                    packageInfo.applicationInfo.flags == ApplicationInfo.FLAG_UPDATED_SYSTEM_APP){
//                break;
//            }
            AppConfig appConfig = new AppConfig();
            if(setAppConfigByPackageInfo(context,appConfig,packageInfo)) {
                appConfigs.add(appConfig);
            }else {
                Log.e(TAG, "setAppConfigByPackageInfo failure" );
            }
        }
        return appConfigs;
    }

    public static boolean setAppConfigByPackageInfo(Context context,AppConfig appConfig, PackageInfo packageInfo){
        if(appConfig == null || packageInfo == null){
            return false;
        }
        appConfig.setAppName(getAppName(context,packageInfo.applicationInfo));
        appConfig.setAppPackageName(packageInfo.packageName);
        String[] permissions = packageInfo.requestedPermissions;
        if(permissions != null && permissions.length != 0){
            PermissionConfigs permissionConfigs = new PermissionConfigs();
            for (String info : permissions) {
                permissionConfigs.add(info);
            }
            appConfig.setPermissionConfigs(permissionConfigs);
        }
        return  true;
    }
}
