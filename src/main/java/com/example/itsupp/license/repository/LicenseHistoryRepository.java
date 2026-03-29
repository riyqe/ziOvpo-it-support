package com.example.itsupp.license.repository;

import com.example.itsupp.license.model.LicenseLicenseHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LicenseHistoryRepository extends JpaRepository<LicenseLicenseHistory, UUID> {
}

