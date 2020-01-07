package com.mean.mypermissions.hook;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.CursorWindow;

import androidx.core.app.ActivityCompat;

import com.mean.mypermissions.bean.RestrictMode;
import com.mean.mypermissions.receiver.RequestPermissionReceiver;
import com.mean.mypermissions.utils.ReflectUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import static android.app.Activity.RESULT_OK;

public class HookPermissionMethods extends HookMethods{
    public static final String PERMISSION_SPECIAL_SKIP_CONTROL = "com.mean.permission.special.SKIP_CONTROL";

    public static XC_MethodHook requestPermissions(final XC_LoadPackage.LoadPackageParam lpparam, final HookEntry hookEntry){
        return new XC_MethodHook() {

            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Context context = (Context) param.thisObject;
                String[] permissions = (String[])param.args[0];

                if(permissions!=null){
                    boolean isWhite = false;
                    for(String p:permissions){
                        if(hookEntry.isPermissionOnWhiteList(p)){
                            isWhite = true;
                            break;
                        }else {
                            XposedBridge.log("HOOK: requestPermissions:" + p);
                        }

                    }
                    if(!isWhite){
                        if(permissions.length>0 && !permissions[0].equals(PERMISSION_SPECIAL_SKIP_CONTROL)){
                            Intent intent = new Intent();
                            intent.setAction("com.mean.mypermissions.intent.permissions.REQUEST");
                            intent.addCategory("android.intent.category.DEFAULT");
                            intent.putExtra("packageName",context.getPackageName());
                            intent.putExtra("permissionNames",permissions);
                            intent.putExtra("rawRequestCode",(int)param.args[1]);
                            Activity activity = findActivity(context);
                            if(activity!=null){
                                activity.startActivityForResult(intent, HookEntry.INTENT_REQUEST_CODE_PERMISSION_REQUEST);
                            }else {
                                XposedBridge.log("ERROR: requestPermissionsHook: activity is NULL");
                            }
                            param.setResult(null);  //结束方法
                        }else if(permissions.length>=2){
                            String[] newPermissions = new String[permissions.length-1];
                            System.arraycopy(permissions,1,newPermissions,0,permissions.length-1);
                            param.args[0] = newPermissions; //交由系统
                        }
                    }
                }
            }
        };
    }

    public static XC_MethodHook checkPermission(final XC_LoadPackage.LoadPackageParam lpparam, final HookEntry hookEntry){
        return new XC_MethodHook() {
            //hook回调函数
            @Override
            protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
                String permission = (String)param.args[0];
                if(permission!= null && !hookEntry.isPermissionOnWhiteList(permission)){
                    XposedBridge.log("HOOK: checkPermission: name: " + param.args[0]);
                    int mode = hookEntry.getRuntimePermissionStatus(permission);
                    XposedBridge.log("HOOK: checkPermission: getRuntimePermissionStatus == "+mode);
                    if(mode!=-1){
                        if(mode != RestrictMode.DENY){
                            param.setResult(PackageManager.PERMISSION_GRANTED);
                            XposedBridge.log("HOOK: checkPermission: rst == PERMISSION_GRANTED");
                        }else {
                            param.setResult(PackageManager.PERMISSION_DENIED);
                            XposedBridge.log("HOOK: checkPermission: rst == PERMISSION_DENIED");
                        }
                    }else {
                        param.setResult(PackageManager.PERMISSION_DENIED);
                        XposedBridge.log("HOOK: checkPermission: rst == PERMISSION_DENIED");
                    }
                }else {
                    XposedBridge.log("HOOK: checkPermission: permission on whitelist");
                }
            }
        };
    }

    public static XC_MethodHook onActivityResult(final XC_LoadPackage.LoadPackageParam lpparam, final HookEntry hookEntry){
        return new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Activity activity = (Activity) param.thisObject;
                int requestCode = (int)param.args[0];
                int resultCode = (int)param.args[1];
                Intent intent = (Intent)param.args[2];
                List<String> permissionName2Sys = new ArrayList<>();
                permissionName2Sys.add(PERMISSION_SPECIAL_SKIP_CONTROL);//用于跳过拦截
                XposedBridge.log("onActivityResult received: requestCode: "+requestCode);
                if(requestCode == HookEntry.INTENT_REQUEST_CODE_PERMISSION_REQUEST){
                    if(resultCode == RESULT_OK){
                        if(intent!=null){
                            int rawRequestCode = intent.getIntExtra("rawRequestCode",0);
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
                                    hookEntry.updateRuntimePermissionStatus(permissionNames[i], permissionModes[i]);
                                    //XposedBridge.log("onActivityResult: updateRuntimePermissionStatus:"+permissionNames[i]+","+permissionModes[i]);
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
                }else if(requestCode == HookEntry.INTENT_REQUEST_CODE_FAKE_CONTACT){
                    if(resultCode == RESULT_OK){
                        String fakeContactName = intent.getStringExtra("fakeContactName");
                        String fakeContactPhone = intent.getStringExtra("fakeContactPhone");
                        if(fakeContactName!=null && fakeContactPhone!=null){
                            hookEntry.fakeContactName = fakeContactName;
                            hookEntry.fakeContactPhone = fakeContactPhone;
                        }else {
                            XposedBridge.log("ERROR: got fakeContact is null");
                        }
                    }
                }
            }
        };
    }

    public static XC_MethodHook BulkCursorToCursorAdaptor$getCount(final XC_LoadPackage.LoadPackageParam lpparam, final HookEntry hookEntry){
        return new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Cursor cursor = (Cursor) param.thisObject;  //实际为 BulkCursorToCursorAdaptor
                if(cursor!=null){
                    XposedBridge.log("HOOK: BulkCursorToCursorAdaptor$getCount: hit cursor");
                    CursorWindow cursorWindow = (CursorWindow)ReflectUtil.getSuperClassesField(cursor,"mWindow"); //通过反射查找cursor的私有变量mWindow（来自父类）
                    if(cursorWindow!=null){
                        XposedBridge.log("HOOK: BulkCursorToCursorAdaptor$getCount: cursorWindow found");
                        String mName = (String)ReflectUtil.getSuperClassesField(cursorWindow,"mName");  //通过反射查找cursorWindow的私有变量mName（来自父类）
                        if(mName!=null && mName.contains("com.android.providers.contacts")){ //cursor.mWindow.mName包含了这个cursor的信息
                            XposedBridge.log("HOOK: BulkCursorToCursorAdaptor$getCount: contacts cursor match");
                            int mode = hookEntry.getRuntimePermissionStatus(Manifest.permission.READ_CONTACTS);
                            XposedBridge.log("HOOK: BulkCursorToCursorAdaptor$getCount: contacts permission mode "+ mode);
                            switch (mode){
                                case RestrictMode.ALLOW_BUT_FAKE:
                                    if(hookEntry.fakeContactName!=null && hookEntry.fakeContactPhone!=null) {
                                        param.setResult(1);
                                        XposedBridge.log("HOOK: [Contact] Cursor.BulkCursorToCursorAdaptor$getCount() == " + 1);
                                    }else {
                                        XposedBridge.log("ERROR: [Contact] fakeContacts not found");
                                    }
                                    break;
                                case RestrictMode.ALLOW_BUT_NULL:
                                    XposedBridge.log("HOOK: [Contact] Cursor.BulkCursorToCursorAdaptor$getCount() == 0");
                                    param.setResult(0);
                                    break;

                            }

                            Method method = ReflectUtil.getSuperClassesMethod(cursor,"getString",int.class);
                            if(method!=null) {
                                XposedBridge.hookMethod(method, AbstractCursor$getString(lpparam, hookEntry));
                            }
                        }
                    }
                }
            }
        };
    }


    private static XC_MethodHook AbstractCursor$getString(final XC_LoadPackage.LoadPackageParam lpparam, final HookEntry hookEntry){
        return new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Cursor cursor = (Cursor) param.thisObject;  //实际为 BulkCursorToCursorAdaptor
                if(cursor!=null) {
                    XposedBridge.log("hit cursor");
                    CursorWindow cursorWindow = (CursorWindow) ReflectUtil.getSuperClassesField(cursor, "mWindow"); //通过反射查找cursor的私有变量mWindow（来自父类）
                    if (cursorWindow != null) {
                        XposedBridge.log("cursorWindow found");
                        String mName = (String) ReflectUtil.getSuperClassesField(cursorWindow, "mName");  //通过反射查找cursorWindow的私有变量mName（来自父类）
                        if (mName != null && mName.contains("com.android.providers.contacts")) { //cursor.mWindow.mName包含了这个cursor的信息
                            int mode = hookEntry.getRuntimePermissionStatus(Manifest.permission.READ_CONTACTS);
                            int columnIndex =(int)param.args[0];
                            switch (mode) {
                                case RestrictMode.ALLOW_BUT_FAKE:
                                    if (columnIndex == 1) {
                                        param.setResult(hookEntry.fakeContactName);
                                        XposedBridge.log("HOOK: [Contact] Cursor.BulkCursorToCursorAdaptor$getString == " + hookEntry.fakeContactName);
                                    }
                                    break;
                                case RestrictMode.ALLOW_BUT_NULL:
                                    if (columnIndex == 1) {
                                        param.setResult("");
                                        XposedBridge.log("HOOK: [Contact] Cursor.BulkCursorToCursorAdaptor$getString == \"\"");
                                    }
                                    break;
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
