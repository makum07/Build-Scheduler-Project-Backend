package com.buildscheduler.buildscheduler.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate; // Import LocalDate
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

    // --- NEW FIELD FOR MAINTENANCE TRACKING ---
    @Column(nullable = true) // Can be null if no maintenance has been performed yet
    private LocalDate lastMaintenanceDate;
    // --- END NEW FIELD ---

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

    // Add early exit in equipment check
    public boolean isAvailable(LocalDateTime start, LocalDateTime checkEnd) {
        if (status != EquipmentStatus.AVAILABLE) return false; // Early exit

        return !nonAvailableSlots.stream().anyMatch(slot -> slot.overlapsWith(start, checkEnd))
                && !assignments.stream().anyMatch(a -> a.overlapsWith(start, checkEnd));
    }

    public boolean isMaintenanceDue() {
        if (lastMaintenanceDate == null) {
            return false; // Or true, depending on your business rule for new equipment
        }
        LocalDate nextMaintenanceDate = lastMaintenanceDate.plusDays(maintenanceIntervalDays);
        return !nextMaintenanceDate.isAfter(LocalDate.now()); // Due if nextMaintenanceDate is today or in the past
    }
}