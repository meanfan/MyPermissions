package com.mean.mypermissions.hook;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;

import com.mean.mypermissions.App;
import com.mean.mypermissions.MainActivity;
import com.mean.mypermissions.receiver.ImplantReceiver;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookEntry implements IXposedHookLoadPackage, IXposedHookZygoteInit {
    private final static String modulePackageName = App.class.getPackage().getName();
    private XSharedPreferences sharedPreferences;
    private Context context;
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
        }

        if(!lpparam.packageName.equals("com.mean.permissionexample")){
            return;
        }
        XposedBridge.log("load app:"+lpparam.packageName);

        /*
         * desc:   注入广播
         * class:  android.app.Application
         * method: onCreate();
         * type:   MethodHook
         * ref:
         */
        XposedHelpers.findAndHookMethod("android.app.Application",
                                        lpparam.classLoader,
                                        "onCreate",
                                        new XC_MethodHook() {
                                            @Override
                                            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                                context = (Context) param.thisObject;
                                                if(context == null){
                                                    XposedBridge.log("context is null");
                                                }
                                                IntentFilter filter = new IntentFilter();
                                                filter.addAction(ImplantReceiver.ACTION);
                                                context.registerReceiver(new ImplantReceiver(), filter);
                                            }

                                            @Override
                                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                                super.afterHookedMethod(param);
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
                                        HookPermission.permissionXCMethodHook(lpparam));
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
                                        new XC_MethodReplacement() {
                                            @Override
                                            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                                                return false;
                                            }
                                        });

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
                                        HookPermission.permissionXCMethodHook(lpparam));

        /*
         * desc:   应用权限申请
         * class:  android.app.Activity
         * method: requestPermissions(@NonNull String[] permissions, int requestCode);
         * type:   MethodReplacement
         * ref:
         */
        XposedHelpers.findAndHookMethod("android.app.Activity",
                                        lpparam.classLoader,
                                        "requestPermissions",
                                        String[].class,
                                        int.class,
                                        new XC_MethodReplacement() {
                                            @Override
                                            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                                                String[] permissions = (String[])param.args[0];
                                                int requestCode = (int)param.args[1];
                                                if(permissions!=null){
                                                    int[] grantResults = new int[permissions.length];
                                                    for (int g:grantResults) {
                                                        g = PackageManager.PERMISSION_GRANTED;
                                                    }
                                                    XposedHelpers.callMethod(param.thisObject,
                                                                             "onRequestPermissionsResult",
                                                                             requestCode,permissions,grantResults);
                                                }
                                                return null;
                                            }

                                        });

    }

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) {
        this.sharedPreferences = new XSharedPreferences(modulePackageName, "default");
        //XposedBridge.log(modulePackageName+" initZygote");
    }
}
