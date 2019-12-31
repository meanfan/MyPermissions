package com.mean.mypermissions.bean;

import android.content.Context;

import com.mean.mypermissions.R;

public class RestrictMode {
    public static final int DEFAULT = 0;  //默认（交由系统处理）
    public static final int REQUEST = 1; //询问
    public static final int ALLOW =2;   //允许
    public static final int DENY = 3;    //拒绝
    public static final int ALLOW_BUT_NULL = 4; //允许但返回空值
    public static final int ALLOW_BUT_FAKE = 5; //允许但返回假数据

    public static String parse2String(Context context, int restrictMode){
        switch (restrictMode){
            case ALLOW:
                return context.getString(R.string.restrict_mode_allow);
            case DENY:
                return context.getString(R.string.restrict_mode_deny);
            case DEFAULT:
                return context.getString(R.string.restrict_mode_default);
            case REQUEST:
                return context.getString(R.string.restrict_mode_request);
            case ALLOW_BUT_FAKE:
                return context.getString(R.string.restrict_mode_allow_but_fake);
            case ALLOW_BUT_NULL:
                return context.getString(R.string.restrict_mode_allow_but_null);
            default:
                return null;
        }
    }
}
