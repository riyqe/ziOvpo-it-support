package com.example.itsupp.license.repository;

import com.example.itsupp.license.model.LicenseDevice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LicenseDeviceRepository extends JpaRepository<LicenseDevice, UUID> {
    Optional<LicenseDevice> findByMacAddress(String macAddress);
}
