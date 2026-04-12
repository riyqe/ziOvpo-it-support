package com.example.ziovpo.license.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ziovpo.license.model.LicenseLicenseHistory;

public interface LicenseHistoryRepository extends JpaRepository<LicenseLicenseHistory, UUID> {

	List<LicenseLicenseHistory> findByLicense_IdOrderByChangeDateDescIdDesc(UUID licenseId);
}

