package com.example.ziovpo.license.service;

import com.example.ziovpo.license.model.LicenseProduct;
import com.example.ziovpo.license.repository.LicenseProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class LicenseProductService {

    private final LicenseProductRepository productRepository;

    public LicenseProductService(LicenseProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public LicenseProduct getProductOrFail(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "product not found"));
    }
}

