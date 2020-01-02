package com.mean.mypermissions.hook;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

import com.mean.mypermissions.bean.RestrictMode;
import com.mean.mypermissions.receiver.RequestPermissionReceiver;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import static android.app.Activity.RESULT_OK;

public class HookPermissionMethods extends HookMethods{
    public static final int REQUEST_CODE_REQUESTPERMISSIONS = 2342;
    public static final String PERMISSION_SPECIAL_SKIP_CONTROL = "com.mean.permission.special.SKIP_CONTROL";

    public static XC_MethodHook requestPermissions(final XC_LoadPackage.LoadPackageParam lpparam){
        return new XC_MethodHook() {

            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                Context context = (Context) param.thisObject;
                String[] permissions = (String[])param.args[0];

                if(permissions!=null){
                    for(String p:permissions){
                        XposedBridge.log("-------------------------");
                        XposedBridge.log("HOOK: requestPermissions:" + p);
                    }
                }
                if(permissions.length>0 && !permissions[0].equals(PERMISSION_SPECIAL_SKIP_CONTROL)){
                    Intent intent = new Intent();
                    intent.setAction("com.mean.mypermissions.intent.permissions.REQUEST");
                    intent.addCategory("android.intent.category.DEFAULT");
                    intent.putExtra("packageName",context.getPackageName());
                    intent.putExtra("permissionNames",permissions);
                    intent.putExtra("rawRequestCode",(int)param.args[1]);
                    Activity activity = findActivity(context);
                    if(activity!=null){
                        activity.startActivityForResult(intent, REQUEST_CODE_REQUESTPERMISSIONS);
                    }else {
                        XposedBridge.log("ERROR: activity is NULL");
                    }
                    param.setResult(null);
                }else if(permissions.length>=2){
                    String[] newPermissions = new String[permissions.length-1];
                    System.arraycopy(permissions,1,newPermissions,0,permissions.length-1);
                    param.args[0] = newPermissions; //交由系统
                }

            }
        };
    }

    public static XC_MethodHook checkPermission(final XC_LoadPackage.LoadPackageParam lpparam){
        return new XC_MethodHook() {
            //hook回调函数
            @Override
            protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
                XposedBridge.log("HOOK: checkPermission: name: " + param.args[0]);
                Context context = (Context) param.thisObject;
                String permissions = (String)param.args[0];
                Intent intent = new Intent();
                intent.setAction("com.mean.mypermissions.intent.permissions.CHECK");
                intent.addCategory("android.intent.category.DEFAULT");
                intent.putExtra("packageName",context.getPackageName());
                intent.putExtra("permissionNames",permissions);
                Activity activity = findActivity(context);
                if(activity!=null){
                    //activity.startActivityForResult(intent,REQUEST_CODE_REQUESTPERMISSIONS);
                }else {
                    XposedBridge.log("ERROR: activity is NULL");
                }

                //sendBroadCast(context,lpparam.packageName,param.args[0].toString());
                XposedBridge.log("HOOK: permission: Broadcast send");
                //showDialog((Activity) param.thisObject,lpparam,param);
                param.setResult(PackageManager.PERMISSION_GRANTED);  //TODO 根据配置文件决定结果
            }
        };
    }

    public static XC_MethodHook onActivityResult(final XC_LoadPackage.LoadPackageParam lpparam){
        return new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Activity activity = (Activity) param.thisObject;
                int requestCode = (int)param.args[0];
                int resultCode = (int)param.args[1];
                Intent intent = (Intent)param.args[2];
                int rawRequestCode = intent.getIntExtra("rawRequestCode",0);
                List<String> permissionName2Sys = new ArrayList<>();
                permissionName2Sys.add(PERMISSION_SPECIAL_SKIP_CONTROL);//用于跳过拦截
                if(requestCode == REQUEST_CODE_REQUESTPERMISSIONS){
                    if(resultCode == RESULT_OK){
                        if(intent!=null){
                            String[] permissionNames = intent.getStringArrayExtra("permissionNames");
                            int[] permissionModes = intent.getIntArrayExtra("permissionModes");
                            if(permissionNames!=null && permissionModes!=null){
                                int[] grantResults = new int[permissionNames.length];
                                for(int i=0;i<permissionNames.length;i++){
                                    switch (permissionModes[i]){
                                        case RestrictMode.ALLOW:   //ALLOW等同于DEFAULT
                                        case RestrictMode.DEFAULT:
                                            permissionName2Sys.add(permissionNames[i]);
                                            grantResults[i] = PackageManager.PERMISSION_GRANTED;
                                            break;
                                        case RestrictMode.DENY:
                                            grantResults[i] = PackageManager.PERMISSION_DENIED;
                                            break;
                                        case RestrictMode.ALLOW_BUT_NULL:
                                        case RestrictMode.ALLOW_BUT_FAKE:
                                            grantResults[i] = PackageManager.PERMISSION_GRANTED;
                                            break;
                                    }
                                }
                                if(permissionName2Sys.size()>=2){
                                    ActivityCompat.requestPermissions(activity,permissionName2Sys.toArray(new String[permissionName2Sys.size()]), rawRequestCode);
                                }

                                for(int i=0;i<permissionNames.length;i++){
                                    XposedBridge.log(String.format(Locale.CHINA,"Permission Result: %d %s %d", rawRequestCode, permissionNames[i], grantResults[i]));
                                }

                                //target: onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
                                XposedHelpers.callMethod(param.thisObject,
                                                         "onRequestPermissionsResult",
                                                         rawRequestCode, permissionNames, grantResults);
                                //sendBroadCast(context,lpparam.packageName,permissions[0]); //广播弃用
                            }
                        }

                    }
                }
            }
        };
    }

    @Deprecated
    private static void sendBroadCast(Context context, String packageName, String permissionName) {
        Intent intent = new Intent();
        intent.setAction("com.mean.mypermissions.action.REQUEST");
        intent.setComponent(new ComponentName("com.mean.mypermissions",
                                              "com.mean.mypermissions.receiver.RequestPermissionReceiver"));
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.putExtra(RequestPermissionReceiver.INTENT_EXTRA_APP_NAME, packageName);
        intent.putExtra(RequestPermissionReceiver.INTENT_EXTRA_PERMISSION_NAME,permissionName);
        context.sendBroadcast(intent);
    }

    public static Activity findActivity(Context context) {
        if (context instanceof Activity) {
            return (Activity) context;
        }
        if (context instanceof ContextWrapper) {
            ContextWrapper wrapper = (ContextWrapper) context;
            return findActivity(wrapper.getBaseContext());
        } else {
            return null;
        }
    }
}
