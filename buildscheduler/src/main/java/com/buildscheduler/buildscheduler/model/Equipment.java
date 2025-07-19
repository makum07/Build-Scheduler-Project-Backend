package com.buildscheduler.buildscheduler.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "equipment")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Equipment extends BaseEntity {
    @NotBlank
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String model;

    @Column(unique = true)
    private String serialNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EquipmentStatus status = EquipmentStatus.AVAILABLE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EquipmentType type;

    @ManyToOne
    @JoinColumn(name = "manager_id")
    private User equipmentManager;


    @Column(precision = 10, scale = 2)
    private BigDecimal purchasePrice;

    @Column(precision = 10, scale = 2)
    private BigDecimal currentValue;

    @Column(nullable = false)
    private Integer warrantyMonths = 12;

    @Column(nullable = false)
    private Integer maintenanceIntervalDays = 30;

    private LocalDate lastMaintenanceDate;
    private LocalDate nextMaintenanceDate;

    private String location;

    @Column(length = 1000)
    private String notes;

    // Relationships
//    @OneToMany(mappedBy = "equipment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    private Set<MaintenanceSchedule> maintenanceSchedules = new HashSet<>();

    @OneToMany(mappedBy = "equipment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<EquipmentAssignment> assignments = new HashSet<>();

//    @OneToMany(mappedBy = "equipment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    private Set<EquipmentRequest> requests = new HashSet<>();

    // Helper methods
//    public boolean isAvailable(LocalDateTime start, LocalDateTime end) {
//        return status == EquipmentStatus.AVAILABLE &&
//                maintenanceSchedules.stream()
//                        .noneMatch(schedule -> schedule.overlapsWith(start, end)) &&
//                assignments.stream()
//                        .noneMatch(assignment -> assignment.overlapsWith(start, end));
//    }

    public boolean isMaintenanceDue() {
        return nextMaintenanceDate != null &&
                nextMaintenanceDate.isBefore(LocalDate.now());
    }

    public enum EquipmentStatus {
        AVAILABLE, IN_USE, UNDER_MAINTENANCE, DECOMMISSIONED, BROKEN
    }

    public enum EquipmentType {
        HEAVY_MACHINERY, POWER_TOOLS, SAFETY_EQUIPMENT, VEHICLES, MEASURING_TOOLS, OTHER
    }
}