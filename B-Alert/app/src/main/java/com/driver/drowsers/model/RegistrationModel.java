package com.driver.drowsers.model;

public class RegistrationModel {
    String name, email, password, image, uid, mobileNo,rePassword;

    public RegistrationModel() {
    }

    public RegistrationModel(String name, String email, String password, String image, String uid, String mobileNo, String rePassword) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.image = image;
        this.uid = uid;
        this.mobileNo = mobileNo;
        this.rePassword = rePassword;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getRePassword() {
        return rePassword;
    }

    public void setRePassword(String rePassword) {
        this.rePassword = rePassword;
    }
}
