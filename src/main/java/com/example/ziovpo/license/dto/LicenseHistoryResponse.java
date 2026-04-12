package com.example.ziovpo.license.dto;

import java.time.LocalDate;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LicenseHistoryResponse {
    private UUID id;
    private UUID licenseId;
    private UUID userId;
    private String status;
    private LocalDate changeDate;
    private String description;
}
