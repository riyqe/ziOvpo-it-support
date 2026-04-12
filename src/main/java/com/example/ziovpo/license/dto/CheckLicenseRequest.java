package com.example.ziovpo.license.dto;

import java.util.UUID;

public class CheckLicenseRequest {

    private String deviceMac;
    private UUID productId;

    public String getDeviceMac() {
        return deviceMac;
    }

    public void setDeviceMac(String deviceMac) {
        this.deviceMac = deviceMac;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }
}
