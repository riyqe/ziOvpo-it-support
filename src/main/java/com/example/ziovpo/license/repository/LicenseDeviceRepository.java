package com.example.ziovpo.license.repository;

import com.example.ziovpo.license.model.LicenseDevice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LicenseDeviceRepository extends JpaRepository<LicenseDevice, UUID> {
    Optional<LicenseDevice> findByMacAddress(String macAddress);
}
