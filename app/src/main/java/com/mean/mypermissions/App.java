package com.mean.mypermissions;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.mean.mypermissions.bean.AppConfig;
import com.mean.mypermissions.dao.DaoMaster;
import com.mean.mypermissions.dao.DaoSession;
import com.mean.mypermissions.utils.AppUtil;

import java.io.File;
import java.util.List;

public class App extends Application {
    public static List<AppConfig> appConfigs;
    private static DaoSession daoSession;

    @Override
    public void onCreate() {
        super.onCreate();
        initFile();
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

    public static void initAppConfig(Context context){
        App.appConfigs = AppUtil.getAllUserAppConfigs(context);
    }

    private void initFile(){
        AppUtil.copyAssetFile(this,"contacts2_fake.db",
                              getExternalFilesDir(null)+ File.separator+"fakedb","contacts2_fake.db");
    }

}
