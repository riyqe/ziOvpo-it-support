package com.example.ziovpo.license.service;

import com.example.ziovpo.license.model.LicenseLicenseType;
import com.example.ziovpo.license.repository.LicenseTypeRepository;
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

