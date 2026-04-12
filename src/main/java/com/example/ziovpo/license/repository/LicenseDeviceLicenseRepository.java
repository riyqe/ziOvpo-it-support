package com.example.ziovpo.license.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ziovpo.license.model.LicenseDeviceLicense;

public interface LicenseDeviceLicenseRepository extends JpaRepository<LicenseDeviceLicense, UUID> {
    long countByLicense_Id(UUID licenseId);
    boolean existsByLicense_IdAndDevice_Id(UUID licenseId, UUID deviceId);
}
