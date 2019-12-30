package com.mean.mypermissions.utils;

import com.mean.mypermissions.App;
import com.mean.mypermissions.bean.AppConfig;
import com.mean.mypermissions.dao.DaoSession;

import java.util.List;

public class AppConfigDBUtil {
    public static void insert(AppConfig appConfig){
        DaoSession daoSession = App.getDaoSession();
        daoSession.insert(appConfig);
    }

    public static void delete(AppConfig appConfig){
        DaoSession daoSession = App.getDaoSession();
    }

    public static void update(AppConfig appConfig){
        DaoSession daoSession = App.getDaoSession();
        daoSession.update(appConfig);
    }

    public static AppConfig query(String appPackageName) {
        DaoSession daoSession = App.getDaoSession();
        List<AppConfig> appConfigs = daoSession.queryRaw(AppConfig.class, " where APP_PACKAGE_NAME = ?", appPackageName);
        if(appConfigs!=null && appConfigs.size()>0) {
            return appConfigs.get(0);
        }else{
            return null;
        }
    }

}
