package com.buildscheduler.buildscheduler.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification extends BaseEntity {
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(nullable = false, length = 500)
    private String message;
    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();


    @Enumerated(EnumType.STRING)
    private NotificationType type;
    public enum NotificationType {
        ASSIGNMENT,
        MAINTENANCE,
        APPROVAL,
        SYSTEM,
        EQUIPMENT_REQUEST
    }
}