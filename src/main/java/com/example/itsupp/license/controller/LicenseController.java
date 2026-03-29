package com.example.itsupp.license.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.itsupp.license.dto.ActivateLicenseRequest;
import com.example.itsupp.license.dto.CheckLicenseRequest;
import com.example.itsupp.license.dto.CreateLicenseRequest;
import com.example.itsupp.license.dto.LicenseResponse;
import com.example.itsupp.license.dto.RenewLicenseRequest;
import com.example.itsupp.license.dto.TicketResponse;
import com.example.itsupp.license.service.LicenseService;
import com.example.itsupp.model.Users;
import com.example.itsupp.repository.UsersRepository;

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
}

