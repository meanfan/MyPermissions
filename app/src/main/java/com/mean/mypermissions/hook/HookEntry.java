package com.mean.mypermissions.hook;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.mean.mypermissions.MainActivity;

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
    private XSharedPreferences sharedPreferences;
    private Context context = null;
    protected static Activity currentActivity = null;
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

    }

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) {
        this.sharedPreferences = new XSharedPreferences(modulePackageName, "default");
        //XposedBridge.log(modulePackageName+" initZygote");
    }
}
