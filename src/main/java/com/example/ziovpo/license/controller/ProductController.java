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

import com.example.ziovpo.license.dto.LicenseProductRequest;
import com.example.ziovpo.license.model.LicenseProduct;
import com.example.ziovpo.license.repository.LicenseProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final LicenseProductRepository productRepository;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LicenseProduct> createProduct(@RequestBody LicenseProductRequest request) {
        log.info("Creating new product: {}", request.getName());
        
        LicenseProduct product = new LicenseProduct();
        product.setName(request.getName());
        product.setBlocked(Boolean.TRUE.equals(request.getIsBlocked()));
        
        LicenseProduct saved = productRepository.save(product);
        log.info("Product created with ID: {}", saved.getId());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<LicenseProduct>> getAllProducts() {
        log.info("Fetching all products");
        List<LicenseProduct> products = productRepository.findAll();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<LicenseProduct> getProductById(@PathVariable UUID id) {
        log.info("Fetching product with ID: {}", id);
        Optional<LicenseProduct> product = productRepository.findById(id);
        
        if (product.isPresent()) {
            return ResponseEntity.ok(product.get());
        } else {
            log.warn("Product not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LicenseProduct> updateProduct(
            @PathVariable UUID id,
            @RequestBody LicenseProductRequest request) {
        log.info("Updating product with ID: {}", id);
        
        Optional<LicenseProduct> productOpt = productRepository.findById(id);
        
        if (productOpt.isPresent()) {
            LicenseProduct product = productOpt.get();
            if (request.getName() != null) {
                product.setName(request.getName());
            }
            if (request.getIsBlocked() != null) {
                product.setBlocked(request.getIsBlocked());
            }
            
            LicenseProduct updated = productRepository.save(product);
            log.info("Product updated with ID: {}", id);
            return ResponseEntity.ok(updated);
        } else {
            log.warn("Product not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        log.info("Deleting product with ID: {}", id);
        
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            log.info("Product deleted with ID: {}", id);
            return ResponseEntity.noContent().build();
        } else {
            log.warn("Product not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }
}
