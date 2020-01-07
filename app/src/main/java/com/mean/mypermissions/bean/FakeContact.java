package com.mean.mypermissions.bean;
import java.io.Serializable;

public class FakeContact implements Serializable {
    private String name;   // 联系人名称
    private String phone;  //实际的phone可有多条，这里为方便只设一条
    private String phoneType; //phone类型

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhoneType() {
        return phoneType;
    }

    public void setPhoneType(String phoneType) {
        this.phoneType = phoneType;
    }
}
