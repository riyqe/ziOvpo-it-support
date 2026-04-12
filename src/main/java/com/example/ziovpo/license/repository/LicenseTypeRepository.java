package com.example.ziovpo.license.repository;

import com.example.ziovpo.license.model.LicenseLicenseType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LicenseTypeRepository extends JpaRepository<LicenseLicenseType, UUID> {
}

