package com.mean.mypermissions.bean;

import org.greenrobot.greendao.annotation.Entity;

import java.io.Serializable;
import java.util.Hashtable;

@Entity
public class PermissionConfigs extends Hashtable<String,Integer> implements Serializable {
    static final long serialVersionUID = 1;
    public void restoreAllMode(){
        for(String key:keySet()){
            put(key,RestrictMode.DEFAULT);
        }
    }

    public void add(String name, Integer mode){
        put(name,mode);
    }
    public void add(String name){
        put(name,RestrictMode.DEFAULT);
    }

    public RestrictMode get(String name){
        return get(name);
    }


}
