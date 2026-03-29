package com.example.itsupp.license.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.itsupp.license.model.LicenseDeviceLicense;

public interface LicenseDeviceLicenseRepository extends JpaRepository<LicenseDeviceLicense, UUID> {
    long countByLicense_Id(UUID licenseId);
    boolean existsByLicense_IdAndDevice_Id(UUID licenseId, UUID deviceId);
}
