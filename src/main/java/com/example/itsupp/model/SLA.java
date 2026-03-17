package com.example.itsupp.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "sla")
public class SLA {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String level;
    private int responseHours;
    private int resolveHours;
}
