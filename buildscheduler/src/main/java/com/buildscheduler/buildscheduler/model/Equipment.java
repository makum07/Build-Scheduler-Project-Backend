package com.buildscheduler.buildscheduler.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "equipment")
@Data
@EqualsAndHashCode(callSuper = true)
public class Equipment extends BaseEntity {

    @NotBlank
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    private EquipmentStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_manager_id")
    private User equipmentManager;

    // Other fields as needed

    public enum EquipmentStatus {
        AVAILABLE, IN_USE, UNDER_MAINTENANCE, OUT_OF_SERVICE
    }
}