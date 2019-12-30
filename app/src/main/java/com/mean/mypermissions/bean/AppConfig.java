package com.mean.mypermissions.bean;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.mean.mypermissions.dao.PermissionConfigsConverter;
import com.mean.mypermissions.utils.AppUtil;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;

import java.io.Serializable;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class AppConfig implements Serializable {
    static  final long serialVersionUID = 1;
    @Id
    private String appPackageName;
    private String appName;
    private boolean isEnabled;
    @Convert(converter = PermissionConfigsConverter.class,columnType = String.class)
    private PermissionConfigs permissionConfigs;

    @Generated(hash = 598311941)
    public AppConfig(String appPackageName, String appName, boolean isEnabled,
            PermissionConfigs permissionConfigs) {
        this.appPackageName = appPackageName;
        this.appName = appName;
        this.isEnabled = isEnabled;
        this.permissionConfigs = permissionConfigs;
    }

    @Generated(hash = 136961441)
    public AppConfig() {
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
        return AppUtil.getAppIcon(context, appPackageName);
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public PermissionConfigs getPermissionConfigs() {
        return permissionConfigs;
    }

    public void setPermissionConfigs(PermissionConfigs permissionConfigs) {
        this.permissionConfigs = permissionConfigs;
    }

    public boolean getIsEnabled() {
        return this.isEnabled;
    }

    public void setIsEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }
}
