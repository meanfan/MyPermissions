package com.mean.mypermissions.bean;

import android.content.pm.PermissionInfo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class PermissionConfigs implements Serializable {
    private Map<String, RestrictMode> permissions;

    public PermissionConfigs() {
        permissions= new HashMap<>();
    }

    public void restoreAllMode(){
        for(String key:permissions.keySet()){
            permissions.put(key,RestrictMode.DEFAULT);
        }
    }

    public Map<String, RestrictMode> getAll(){
        return permissions;
    }

    public void add(String name, RestrictMode mode){
        permissions.put(name,mode);
    }
    public void add(String name){
        permissions.put(name,RestrictMode.DEFAULT);
    }

    public RestrictMode get(String name){
        return permissions.get(name);
    }
}
