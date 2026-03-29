package com.example.itsupp.license.service;

import com.example.itsupp.license.model.LicenseLicenseType;
import com.example.itsupp.license.repository.LicenseTypeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class LicenseTypeService {

    private final LicenseTypeRepository typeRepository;

    public LicenseTypeService(LicenseTypeRepository typeRepository) {
        this.typeRepository = typeRepository;
    }

    public LicenseLicenseType getTypeOrFail(UUID typeId) {
        return typeRepository.findById(typeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "type not found"));
    }
}

