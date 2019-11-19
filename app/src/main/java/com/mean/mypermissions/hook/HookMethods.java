package com.mean.mypermissions.hook;

import android.content.Context;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookMethods {
    public static XC_MethodReplacement BooleanReturnMethodReplacement(final boolean rt){
        return new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                return rt;
            }
        };
    }
}
