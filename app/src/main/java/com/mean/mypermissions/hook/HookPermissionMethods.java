package com.mean.mypermissions.hook;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AndroidAppHelper;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import com.mean.mypermissions.receiver.RequestPermissionReceiver;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookPermissionMethods extends HookMethods{

    public static XC_MethodReplacement permissionRequestMethodReplacement(final XC_LoadPackage.LoadPackageParam lpparam){
        return new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                Context context = (Context) param.thisObject;
                String[] permissions = (String[])param.args[0];
                int requestCode = (int)param.args[1];
                if(permissions!=null){
                    int[] grantResults = new int[permissions.length];
                    for (int g:grantResults) {
                        g = PackageManager.PERMISSION_GRANTED;
                    }
                    //onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
                    XposedHelpers.callMethod(param.thisObject,
                                             "onRequestPermissionsResult",
                                             requestCode, permissions, grantResults);
                    sendBroadCast(context,lpparam.packageName,permissions[0]); //不妨只传一个
                }
                return null;
            }
        };
    }

    public static XC_MethodHook permissionCheckMethodHook(final XC_LoadPackage.LoadPackageParam lpparam){
        return new XC_MethodHook() {
            //hook回调函数
            @Override
            protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
                XposedBridge.log("HOOK: permission: name: " + param.args[0]);
                Context context = (Context) param.thisObject;
                if (null == context) {
                    XposedBridge.log("ERROR: context is NULL");
                    return;
                }
                if(null == lpparam){
                    XposedBridge.log("ERROR: lpparam is NULL");
                    return;
                }
                sendBroadCast(context,lpparam.packageName,param.args[0].toString());
                XposedBridge.log("HOOK: permission: Broadcast send");
                param.setResult(PackageManager.PERMISSION_GRANTED);  //TODO 根据配置文件决定结果
            }
        };
    }

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

    //TODO 获取不到activity。。。
    private static void showDialog(Activity context, XC_LoadPackage.LoadPackageParam lpparam, final XC_MethodHook.MethodHookParam param){
        String msg = String.format("%s 请求权限：\n%s",lpparam.packageName,param.args[0].toString());
        AlertDialog.Builder builder = new AlertDialog.Builder((Activity)context);
        final AlertDialog dialog = builder.setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("交由系统", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // go to system
                    }
                })
                .setNeutralButton("假允许", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        param.setResult(PackageManager.PERMISSION_GRANTED);
                    }
                })
                .setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        param.setResult(PackageManager.PERMISSION_DENIED);
                    }
                }).create();
        dialog.show();
    }
}
