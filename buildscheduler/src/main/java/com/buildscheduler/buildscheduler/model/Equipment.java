package com.buildscheduler.buildscheduler.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

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

    @Column
    private String serialNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EquipmentType type;

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

    @JsonIgnore
    @OneToMany(mappedBy = "equipment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<EquipmentNonAvailableSlot> equipmentNonAvailableSlots = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "equipment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<EquipmentAssignment> assignments = new HashSet<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "equipmentNeeds")
    private Set<Subtask> requestedInSubtasks = new HashSet<>();

    private Boolean maintenanceDueAlert = false;


    public enum EquipmentType {
        HEAVY_MACHINERY, POWER_TOOLS, SAFETY_EQUIPMENT, VEHICLES, MEASURING_TOOLS, OTHER
    }

    public boolean isAvailable(LocalDateTime start, LocalDateTime end) {
        boolean nonAvailClear = equipmentNonAvailableSlots.stream()
                .noneMatch(s -> s.overlapsWith(start, end));

        boolean assignClear = assignments.stream()
                .noneMatch(a ->
                        a.overlapsWith(
                                start.toLocalDate(), start.toLocalTime(),
                                end.  toLocalDate(), end.  toLocalTime()
                        )
                );

        return nonAvailClear && assignClear;
    }


}
