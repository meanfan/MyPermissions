package com.mean.mypermissions.hook;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;

import com.mean.mypermissions.MainActivity;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookEntry implements IXposedHookLoadPackage, IXposedHookZygoteInit {
    private static String MODULE_PATH = null;
    public final static String modulePackageName = "com.mean.mypermissions";
    public static final int INTENT_REQUEST_CODE_PERMISSION_REQUEST = 2342;
    public static final int INTENT_REQUEST_CODE_PERMISSION_CHECK = 2343;
    public static final int INTENT_REQUEST_CODE_FAKE_CONTACT = 2344;

    public static final Set<String> permissionWhiteSet = new HashSet<>();
    protected static Activity currentActivity = null;
    private Hashtable<String,Integer> runtimePermissionStatus = new Hashtable<>();
    public String fakeContactName;
    public String fakeContactPhone;

    private static final String[] permissionWhiteList= new String[]{
            "android.permission.INTERACT_ACROSS_USERS",
            "android.permission.INTERACT_ACROSS_USERS_FULL",
    };

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

        permissionWhiteSet.addAll(Arrays.asList(permissionWhiteList));

        XposedHelpers.findAndHookMethod(Activity.class,
                                        "onStart",
                                        new XC_MethodHook() {
                                            @Override
                                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                                Activity activity = (Activity)param.thisObject;
                                                if(param.thisObject == null){
                                                    XposedBridge.log("ERROR: activity is NULL");
                                                }else {
                                                    currentActivity = activity;
                                                    if(fakeContactName == null || fakeContactPhone == null){
                                                        Intent intent = new Intent();
                                                        intent.putExtra("packageName",lpparam.packageName);
                                                        intent.setAction("com.mean.mypermissions.intent.permissions.fake.CONTACT");
                                                        intent.addCategory("android.intent.category.DEFAULT");
                                                        XposedBridge.log("HOOK: send intent to get fakeContact");
                                                        activity.startActivityForResult(intent, INTENT_REQUEST_CODE_FAKE_CONTACT);
                                                    }
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
                                        "checkPermission",
                                        String.class, int.class,int.class,
                                        HookPermissionMethods.checkPermission(lpparam,this));

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

        /*XposedHelpers.findAndHookMethod("android.content.ContextWrapper",
                                        ClassLoader.getSystemClassLoader(),
                                        "checkCallingOrSelfPermission",
                                        String.class,int.class,int.class,
                                        HookPermissionMethods.checkPermission(lpparam,this));*/


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
                                        HookPermissionMethods.requestPermissions(lpparam,this));

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
                                        HookPermissionMethods.onActivityResult(lpparam,this));

        /*
         * desc:  通过 ContentResolver 读取内容（联系人等），获得 Cursor (由 BulkCursorToCursorAdaptor 实现）
         * class: android.database.BulkCursorToCursorAdaptor
         * method: int BulkCursorToCursorAdaptor$getCount()
         * ref: 通过调试设备得到 Cursor 的实现为 BulkCursorToCursorAdaptor
         */
        XposedHelpers.findAndHookMethod("android.database.BulkCursorToCursorAdaptor", lpparam.classLoader,
                                        "getCount",
                                        HookPermissionMethods.BulkCursorToCursorAdaptor$getCount(lpparam, this));
    }

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) {
        MODULE_PATH = startupParam.modulePath;
    }

    public void updateRuntimePermissionStatus(String permissionName, int mode){
        if(permissionName!=null){
            runtimePermissionStatus.put(permissionName,mode);
        }
    }
    public int getRuntimePermissionStatus(String permissionName){
        if(permissionName!=null){
            Integer mode = runtimePermissionStatus.get(permissionName);
            if(mode!=null){
                return mode;
            }
        }
        return -1;
    }

    public boolean isPermissionOnWhiteList(String permission){
        XposedBridge.log("isPermissionOnWhiteList: arg permission: " + permission);
        if(permission!=null){

            return permissionWhiteSet.contains(permission);
        }else {
            XposedBridge.log("ERROR: isPermissionOnWhiteList: arg permission is null");
            return false;
        }

    }
}
