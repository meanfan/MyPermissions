package com.mean.mypermissions.hook;

import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.content.ContextCompat;


import com.mean.mypermissions.MainActivity;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class SupportChecker implements IXposedHookLoadPackage {
    private static final String TAG = "SupportChecker";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        XposedBridge.log("Loaded app: " + lpparam.packageName);

        //check
        String appPackageName = "com.mean.mypermissions";
        String appTargetClassName = "com.mean.mypermissions.MainActivity";
        if(lpparam.packageName.equals(appPackageName)){
            XposedHelpers.findAndHookMethod(appTargetClassName,
                                            lpparam.classLoader,
                                            "isModuleActive",
                                            XC_MethodReplacement.returnConstant(true));
            XposedBridge.log("self hooked");
        }

//    findAndHookMethod(ContextCompat.class,
//                          "checkSelfPermission",
//                      XC_MethodReplacement.returnConstant(PackageManager.PERMISSION_GRANTED));
}


}
