package com.example.ziovpo.license.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ziovpo.license.dto.ActivateLicenseRequest;
import com.example.ziovpo.license.dto.CheckLicenseRequest;
import com.example.ziovpo.license.dto.CreateLicenseRequest;
import com.example.ziovpo.license.dto.LicenseHistoryResponse;
import com.example.ziovpo.license.dto.LicenseResponse;
import com.example.ziovpo.license.dto.RenewLicenseRequest;
import com.example.ziovpo.license.dto.TicketResponse;
import com.example.ziovpo.license.service.LicenseService;
import com.example.ziovpo.model.Users;
import com.example.ziovpo.repository.UsersRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/licenses")
@RequiredArgsConstructor
@Slf4j
public class LicenseController {

    private final LicenseService licenseService;
    private final UsersRepository usersRepository;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LicenseResponse> createLicense(@RequestBody CreateLicenseRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Users admin = usersRepository.findByUsername(username)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
        LicenseResponse created = licenseService.createLicense(request, admin.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LicenseResponse>> getAllLicenses() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Users admin = usersRepository.findByUsername(username)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
        return ResponseEntity.ok(licenseService.getAllLicenses(admin.getId()));
    }

    @GetMapping("/by-code/{code}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LicenseResponse> getLicenseByCode(@PathVariable String code) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Users admin = usersRepository.findByUsername(username)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
        return ResponseEntity.ok(licenseService.getLicenseByCode(code, admin.getId()));
    }

    @PostMapping("/activate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TicketResponse> activateLicense(@RequestBody ActivateLicenseRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Users user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
        log.debug("Activate request from username={} activationKey={} deviceMac={}", username, request.getActivationKey() == null ? null : "***", request.getDeviceMac());
        TicketResponse ticket = licenseService.activateLicense(request, user.getId());
        return ResponseEntity.ok(ticket);
    }

    @PostMapping("/check")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TicketResponse> checkLicense(@RequestBody CheckLicenseRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Users user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
        TicketResponse ticket = licenseService.checkLicense(request, user.getId());
        return ResponseEntity.ok(ticket);
    }

    @PostMapping("/renew")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TicketResponse> renewLicense(@RequestBody RenewLicenseRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Users user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
        log.debug("Renew request from username={} activationKey={}", username, request.getActivationKey() == null ? null : "***");
        TicketResponse ticket = licenseService.renewLicense(request, user.getId());
        return ResponseEntity.ok(ticket);
    }

    @GetMapping("/{licenseId}/history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LicenseHistoryResponse>> getLicenseHistory(@PathVariable UUID licenseId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Users user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
        return ResponseEntity.ok(licenseService.getLicenseHistory(licenseId, user.getId()));
    }
}

