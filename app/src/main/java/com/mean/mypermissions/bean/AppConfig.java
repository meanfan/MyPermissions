package com.mean.mypermissions.bean;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.mean.mypermissions.utils.PermissionUtil;

public class AppConfig {
    int id;
    String appName;
    String appPackageName;
    String isEnabled;
    PermissionConfigs permissionConfigs;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppPackageName() {
        return appPackageName;
    }

    public void setAppPackageName(String appPackageName) {
        this.appPackageName = appPackageName;
    }

    public Drawable getAppIconDrawable(Context context) {
        return PermissionUtil.getAppIcon(context,appPackageName);
    }

    public String getIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(String isEnabled) {
        this.isEnabled = isEnabled;
    }

    public PermissionConfigs getPermissionConfigs() {
        return permissionConfigs;
    }

    public void setPermissionConfigs(PermissionConfigs permissionConfigs) {
        this.permissionConfigs = permissionConfigs;
    }
}
