package com.example.ziovpo.license.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LicenseLicenseTypeRequest {
    private String name;
    private Integer defaultDurationInDays;
    private String description;
}
