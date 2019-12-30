package com.mean.mypermissions;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import com.mean.mypermissions.bean.AppConfig;
import com.mean.mypermissions.dao.DaoMaster;
import com.mean.mypermissions.dao.DaoSession;

import java.util.List;

public class App extends Application {
    public static List<AppConfig> appConfigs;
    private static DaoSession daoSession;

    @Override
    public void onCreate() {
        super.onCreate();
        initGreenDAO();
    }

    private void initGreenDAO(){
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "appConfigs-db", null);
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
    }

    public static DaoSession getDaoSession() {
        return daoSession;
    }
}
