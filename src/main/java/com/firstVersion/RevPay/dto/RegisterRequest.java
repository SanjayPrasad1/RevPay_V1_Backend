package com.firstVersion.RevPay.dto;

import com.firstVersion.RevPay.entity.User.UserType;

public class RegisterRequest {
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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public RegisterRequest() {
    }

    private String email;
    private String password;
    private String fullName;
    private String phoneNumber;
    private UserType userType; // PERSONAL or BUSINESS

    public RegisterRequest(String email, String password, String fullName, String phoneNumber, UserType userType, String businessName, String taxId) {
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.userType = userType;
        this.businessName = businessName;
        this.taxId = taxId;
    }

    // Business specific (optional)
    private String businessName;
    private String taxId;
}