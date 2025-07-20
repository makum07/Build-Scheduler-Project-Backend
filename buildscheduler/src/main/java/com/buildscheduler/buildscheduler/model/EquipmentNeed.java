package com.buildscheduler.buildscheduler.model;

import com.buildscheduler.buildscheduler.model.Equipment.EquipmentType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "equipment_needs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentNeed extends BaseEntity {

    @Enumerated(EnumType.STRING)
    private Equipment.EquipmentType requiredType;

    private LocalDateTime requiredStartTime;

    private LocalDateTime requiredEndTime;

    private String requestNotes;

    private Integer priority;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subtask_id")
    private Subtask subtask;

}