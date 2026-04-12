package com.example.ziovpo.license.model;

import java.time.LocalDate;
import java.util.UUID;

import com.example.ziovpo.model.Users;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "license")
public class LicenseLicense {

    @Id
    private UUID id;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private LicenseProduct product;

    @ManyToOne(optional = false)
    @JoinColumn(name = "type_id", nullable = false)
    private LicenseLicenseType type;

    @ManyToOne(optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private Users owner;

    @ManyToOne(optional = true)
    @JoinColumn(name = "user_id")
    private Users user;

    @Column(name = "first_activation_date")
    private LocalDate firstActivationDate;

    @Column(name = "ending_date")
    private LocalDate endingDate;

    @Column(name = "blocked", nullable = false)
    private boolean blocked;

    @Column(name = "device_count", nullable = false)
    private int deviceCount;

    @Column(name = "description")
    private String description;

    @PrePersist
    @SuppressWarnings("unused")
    private void ensureId() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}

