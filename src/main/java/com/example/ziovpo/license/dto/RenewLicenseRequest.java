package com.example.ziovpo.license.dto;


public class RenewLicenseRequest {
    
    private String activationKey;
    public RenewLicenseRequest() {}

    public RenewLicenseRequest(String activationKey) {
        this.activationKey = activationKey;
    }
    public String getActivationKey() {
        return activationKey;
    }

    public void setActivationKey(String activationKey) {
        this.activationKey = activationKey;
    }
}
