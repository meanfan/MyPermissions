package com.mean.mypermissions.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReflectUtil {

    public static Object getSuperClassesField(Object originObj,String fieldName){
        Class clazz = originObj.getClass();
        Field field = null;
        Object obj = null;
        while (clazz != null){
            try {
                field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                obj = field.get(originObj);
                break;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return obj;
    }

    public static Method getSuperClassesMethod(Object originObj, String methodName,Class<?>... parameterTypes){
        Class clazz = originObj.getClass();
        Method method = null;
        while (clazz != null){
            try {
                method = clazz.getDeclaredMethod(methodName,parameterTypes);
                method.setAccessible(true);
                break;
            } catch (NoSuchMethodException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return method;
    }
}
