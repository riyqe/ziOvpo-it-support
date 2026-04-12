package com.example.ziovpo.license.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ziovpo.license.dto.LicenseLicenseTypeRequest;
import com.example.ziovpo.license.model.LicenseLicenseType;
import com.example.ziovpo.license.repository.LicenseTypeRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/license-types")
@RequiredArgsConstructor
public class LicenseTypeController {

    private final LicenseTypeRepository licenseTypeRepository;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LicenseLicenseType> createLicenseType(@RequestBody LicenseLicenseTypeRequest request) {
        log.info("Creating new license type: {}", request.getName());
        
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            log.warn("License type name is required");
            return ResponseEntity.badRequest().build();
        }
        
        if (request.getDefaultDurationInDays() == null || request.getDefaultDurationInDays() <= 0) {
            log.warn("License type duration must be positive");
            return ResponseEntity.badRequest().build();
        }
        
        LicenseLicenseType licenseType = new LicenseLicenseType();
        licenseType.setName(request.getName());
        licenseType.setDefaultDurationInDays(request.getDefaultDurationInDays());
        licenseType.setDescription(request.getDescription());
        
        LicenseLicenseType saved = licenseTypeRepository.save(licenseType);
        log.info("License type created with ID: {}", saved.getId());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<LicenseLicenseType>> getAllLicenseTypes() {
        log.info("Fetching all license types");
        List<LicenseLicenseType> licenseTypes = licenseTypeRepository.findAll();
        return ResponseEntity.ok(licenseTypes);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<LicenseLicenseType> getLicenseTypeById(@PathVariable UUID id) {
        log.info("Fetching license type with ID: {}", id);
        Optional<LicenseLicenseType> licenseType = licenseTypeRepository.findById(id);
        
        if (licenseType.isPresent()) {
            return ResponseEntity.ok(licenseType.get());
        } else {
            log.warn("License type not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LicenseLicenseType> updateLicenseType(
            @PathVariable UUID id,
            @RequestBody LicenseLicenseTypeRequest request) {
        log.info("Updating license type with ID: {}", id);
        
        if (request.getDefaultDurationInDays() != null && request.getDefaultDurationInDays() <= 0) {
            log.warn("License type duration must be positive");
            return ResponseEntity.badRequest().build();
        }
        
        Optional<LicenseLicenseType> licenseTypeOpt = licenseTypeRepository.findById(id);
        
        if (licenseTypeOpt.isPresent()) {
            LicenseLicenseType licenseType = licenseTypeOpt.get();
            if (request.getName() != null) {
                licenseType.setName(request.getName());
            }
            if (request.getDefaultDurationInDays() != null) {
                licenseType.setDefaultDurationInDays(request.getDefaultDurationInDays());
            }
            if (request.getDescription() != null) {
                licenseType.setDescription(request.getDescription());
            }
            
            LicenseLicenseType updated = licenseTypeRepository.save(licenseType);
            log.info("License type updated with ID: {}", id);
            return ResponseEntity.ok(updated);
        } else {
            log.warn("License type not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteLicenseType(@PathVariable UUID id) {
        log.info("Deleting license type with ID: {}", id);
        
        if (licenseTypeRepository.existsById(id)) {
            licenseTypeRepository.deleteById(id);
            log.info("License type deleted with ID: {}", id);
            return ResponseEntity.noContent().build();
        } else {
            log.warn("License type not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }
}
