package com.example.ziovpo.license.repository;

import com.example.ziovpo.license.model.LicenseProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LicenseProductRepository extends JpaRepository<LicenseProduct, UUID> {
}

