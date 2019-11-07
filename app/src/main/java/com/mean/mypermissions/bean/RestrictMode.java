package com.mean.mypermissions.bean;

public enum RestrictMode {
    DEFAULT,  //默认（未配置）
    BYPASS,  //绕过（交由系统处理）
    REQUEST, //询问
    ALLOW,   //允许
    DENY,    //拒绝
    ALLOW_BUT_NULL, //允许但返回空值
    ALLOW_BUT_FAKE, //允许但返回假数据
}
