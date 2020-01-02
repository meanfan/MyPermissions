package com.mean.mypermissions.hook;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;

import com.mean.mypermissions.MainActivity;

import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookEntry implements IXposedHookLoadPackage, IXposedHookZygoteInit {
    public final static String modulePackageName = "com.mean.mypermissions";
    public static final int CURSOR_MODE_EMPTY = 0;
    public static final int CURSOR_MODE_FAKE = 1;

    private XSharedPreferences sharedPreferences;
    private Context context = null;
    protected static Activity currentActivity = null;
    protected static Hashtable<Cursor,Integer> cursors;
    //private final ImplantReceiver receiver = new ImplantReceiver();
    //public static volatile Activity currentActivity = null;
    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        /*
         * desc:   hook自身，用于检查是否已启用
         * class:  MainActivity
         * method: isModuleActive();
         * type:   MethodReplacement
         * ref:
         */

        if(lpparam.packageName.equals(modulePackageName)){
            XposedHelpers.findAndHookMethod(MainActivity.class.getName(),
                                            lpparam.classLoader,
                                            "isModuleActive",
                                            XC_MethodReplacement.returnConstant(true));
            XposedBridge.log("self hooked~~~~~");
            return;
        }

        if(!lpparam.packageName.equals("com.mean.permissionexample")){
            return;
        }

        XposedBridge.log("load app:"+lpparam.packageName);

        cursors = new Hashtable<>();

        XposedHelpers.findAndHookMethod(Activity.class,
                                        "onResume",
                                        new XC_MethodHook() {
                                            @Override
                                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                                if(param.thisObject == null){
                                                    XposedBridge.log("ERROR: activity is NULL");
                                                }else {
                                                    XposedBridge.log("HOOK: activity got");
                                                    currentActivity = (Activity) param.thisObject;
                                                }
                                            }
                                        });

        /*
         * desc:   应用权限检查1
         * class:  android.content.ContextWrapper
         * method: checkPermission(String var1, int var2, int var3);
         * type:   MethodHook
         * ref:
         */
        XposedHelpers.findAndHookMethod("android.content.ContextWrapper",
                                        lpparam.classLoader,
                                        "checkSelfPermission",
                                        String.class,
                                        HookPermissionMethods.checkPermission(lpparam));
        /*
         * desc:   应用权限检查2
         * class:  android.app.Activity
         * method: shouldShowRequestPermissionRationale(String var1);
         * type:   MethodHook
         * ref:
         */
        XposedHelpers.findAndHookMethod("android.app.Activity",
                                        lpparam.classLoader,
                                        "shouldShowRequestPermissionRationale",
                                        String.class,
                                        XC_MethodReplacement.returnConstant(false));

        /*
         * desc:   应用权限检查3
         * class:  android.content.ContextWrapper
         * target: checkCallingOrSelfPermission(String var1, int var2, int var3);
         * type:   MethodHook
         * ref:
         */
        XposedHelpers.findAndHookMethod("android.content.ContextWrapper",
                                        ClassLoader.getSystemClassLoader(),
                                        "checkCallingOrSelfPermission",
                                        String.class,
                                        HookPermissionMethods.checkPermission(lpparam));

        /*
         * desc:   应用权限申请
         * class:  android.app.Activity
         * method: requestPermissions(@NonNull String[] permissions, int requestCode);
         * type:   MethodHook
         * ref:
         */
        XposedHelpers.findAndHookMethod("android.app.Activity",
                                        lpparam.classLoader,
                                        "requestPermissions",
                                        String[].class,
                                        int.class,
                                        HookPermissionMethods.requestPermissions(lpparam));

        /*
         * desc:   应用权限申请结果回调
         * class:  android.app.Activity
         * method: void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
         * type:   MethodHook
         * ref:
         */
        XposedHelpers.findAndHookMethod("android.app.Activity", lpparam.classLoader,
                                        "onActivityResult",
                                        int.class, int.class, Intent.class,
                                        HookPermissionMethods.onActivityResult(lpparam));

        /*
         * desc:  通过 ContentResolver 读取内容（联系人等），获得 Cursor (由 BulkCursorToCursorAdaptor 实现）
         * class: android.database.BulkCursorToCursorAdaptor
         * method: int getCount()
         * ref: 通过调试设备得到 Cursor 的实现为 BulkCursorToCursorAdaptor
         */
        XposedHelpers.findAndHookMethod("android.database.BulkCursorToCursorAdaptor", lpparam.classLoader,
                                        "getCount",
                                        HookPermissionMethods.getCount(lpparam));


    }

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) {
        this.sharedPreferences = new XSharedPreferences(modulePackageName, "default");
        //XposedBridge.log(modulePackageName+" initZygote");
    }
}
