package com.mean.mypermissions.dao;

import com.alibaba.fastjson.JSON;
import com.mean.mypermissions.bean.PermissionConfigs;

import org.greenrobot.greendao.converter.PropertyConverter;

public class PermissionConfigsConverter implements PropertyConverter<PermissionConfigs, String> {
    @Override
    public PermissionConfigs convertToEntityProperty(String databaseValue) {
        return JSON.parseObject(databaseValue,PermissionConfigs.class);
    }

    @Override
    public String convertToDatabaseValue(PermissionConfigs entityProperty) {
        return JSON.toJSONString(entityProperty);
    }
}
