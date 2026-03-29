package com.example.itsupp.license.repository;

import com.example.itsupp.license.model.LicenseLicenseType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LicenseTypeRepository extends JpaRepository<LicenseLicenseType, UUID> {
}

