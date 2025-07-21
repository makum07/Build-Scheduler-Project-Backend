package com.buildscheduler.buildscheduler.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "equipment")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Equipment extends BaseEntity {

    public enum EquipmentStatus {
        AVAILABLE, IN_USE, UNDER_MAINTENANCE, DECOMMISSIONED
    }

    public enum EquipmentType {
        HEAVY_MACHINERY, POWER_TOOLS, SAFETY_EQUIPMENT,
        VEHICLES, MEASURING_TOOLS, OTHER
    }

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String model;

    private String serialNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EquipmentType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EquipmentStatus status = EquipmentStatus.AVAILABLE;

    @ManyToOne
    @JoinColumn(name = "manager_id")
    private User equipmentManager;

    @Column(precision = 10, scale = 2)
    private BigDecimal purchasePrice;

    @Column(nullable = false)
    private Integer warrantyMonths = 12;

    @Column(nullable = false)
    private Integer maintenanceIntervalDays = 30;

    private String location;

    @Column(length = 1000)
    private String notes;

    @OneToMany(
            mappedBy = "equipment",
            cascade = { CascadeType.PERSIST, CascadeType.MERGE },
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private Set<EquipmentNonAvailableSlot> nonAvailableSlots = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "equipment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<EquipmentAssignment> assignments = new HashSet<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "equipmentNeeds")
    private Set<Subtask> requestedInSubtasks = new HashSet<>();
//
//    public boolean isAvailable(LocalDateTime start, LocalDateTime end) {
//        if (status != EquipmentStatus.AVAILABLE) {
//            return false;
//        }
//
//        boolean nonAvailConflict = nonAvailableSlots.stream()
//                .anyMatch(slot -> slot.overlapsWith(start, end));
//
//        boolean assignmentConflict = assignments.stream()
//                .anyMatch(assignment -> assignment.overlapsWith(start, end));
//
//        return !nonAvailConflict && !assignmentConflict;
//    }
    // Add early exit in equipment check
    public boolean isAvailable(LocalDateTime start, LocalDateTime end) {
        if (status != EquipmentStatus.AVAILABLE) return false; // Early exit

        return !nonAvailableSlots.stream().anyMatch(slot -> slot.overlapsWith(start, end))
                && !assignments.stream().anyMatch(a -> a.overlapsWith(start, end));
    }
}