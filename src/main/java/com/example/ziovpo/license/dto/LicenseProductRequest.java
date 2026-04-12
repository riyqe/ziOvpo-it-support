package com.example.ziovpo.license.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LicenseProductRequest {
    private String name;
    private Boolean isBlocked;
}
