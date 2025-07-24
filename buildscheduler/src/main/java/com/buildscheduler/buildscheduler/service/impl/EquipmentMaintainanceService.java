package com.buildscheduler.buildscheduler.service.impl;

import com.buildscheduler.buildscheduler.dto.equipment_manager.EquipmentMaintenanceAlertDto;
import com.buildscheduler.buildscheduler.exception.ResourceNotFoundException;
import com.buildscheduler.buildscheduler.model.Equipment;
import com.buildscheduler.buildscheduler.model.Notification; // Import Notification
import com.buildscheduler.buildscheduler.model.User;
import com.buildscheduler.buildscheduler.repository.EquipmentRepository;
import com.buildscheduler.buildscheduler.repository.UserRepository;
import com.buildscheduler.buildscheduler.service.NotificationService; // Import NotificationService
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EquipmentMaintainanceService {

    private final EquipmentRepository equipmentRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService; // Inject NotificationService

    public EquipmentMaintainanceService(EquipmentRepository equipmentRepository, UserRepository userRepository, NotificationService notificationService) {
        this.equipmentRepository = equipmentRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService; // Initialize NotificationService
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            throw new ResourceNotFoundException("User not authenticated");
        }
        return (User) authentication.getPrincipal();
    }


    public List<EquipmentMaintenanceAlertDto> getMaintenanceAlertsForManager() {
        User currentUser = getCurrentUser();
        List<Equipment> managedEquipment = equipmentRepository.findByEquipmentManager(currentUser);

        // Filter for equipment that needs maintenance and send notifications if not already sent
        List<EquipmentMaintenanceAlertDto> alerts = managedEquipment.stream()
                .filter(Equipment::isMaintenanceDue)
                .map(equipment -> {
                    // Send notification to the equipment manager for this overdue equipment
                    // You might want to add logic here to prevent duplicate notifications (e.g., check if already notified today)
                    sendMaintenanceNotification(equipment);
                    return convertToMaintenanceAlertDto(equipment);
                })
                .collect(Collectors.toList());

        return alerts;
    }

    @Transactional(readOnly = true)
    public List<EquipmentMaintenanceAlertDto> getAllMaintenanceAlerts() {
        List<Equipment> allEquipment = equipmentRepository.findAll();

        List<EquipmentMaintenanceAlertDto> alerts = allEquipment.stream()
                .filter(Equipment::isMaintenanceDue)
                .map(equipment -> {
                    // You could send notifications here too, or decide it's only for the manager's view
                    // sendMaintenanceNotification(equipment); // Uncomment if needed for all equipment managers/admins
                    return convertToMaintenanceAlertDto(equipment);
                })
                .collect(Collectors.toList());
        return alerts;
    }

    private EquipmentMaintenanceAlertDto convertToMaintenanceAlertDto(Equipment equipment) {
        EquipmentMaintenanceAlertDto dto = new EquipmentMaintenanceAlertDto();
        dto.setEquipmentId(equipment.getId());
        dto.setEquipmentName(equipment.getName());
        dto.setSerialNumber(equipment.getSerialNumber());
        dto.setLastMaintenanceDate(equipment.getLastMaintenanceDate());
        dto.setMaintenanceIntervalDays(equipment.getMaintenanceIntervalDays());

        LocalDate nextExpectedMaintenanceDate = null;
        if (equipment.getLastMaintenanceDate() != null) {
            nextExpectedMaintenanceDate = equipment.getLastMaintenanceDate().plusDays(equipment.getMaintenanceIntervalDays());
        }
        dto.setNextExpectedMaintenanceDate(nextExpectedMaintenanceDate);

        String message;
        if (nextExpectedMaintenanceDate != null && nextExpectedMaintenanceDate.isBefore(LocalDate.now())) {
            message = "Maintenance overdue since " + nextExpectedMaintenanceDate;
        } else if (nextExpectedMaintenanceDate != null && nextExpectedMaintenanceDate.isEqual(LocalDate.now())) {
            message = "Maintenance due today!";
        } else {
            message = "Maintenance required";
        }
        dto.setAlertMessage(message);

        return dto;
    }

    @Transactional
    public void recordMaintenance(Long equipmentId) {
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", "id", equipmentId));
        equipment.setLastMaintenanceDate(LocalDate.now());
        if (equipment.getStatus() == Equipment.EquipmentStatus.UNDER_MAINTENANCE) {
            equipment.setStatus(Equipment.EquipmentStatus.AVAILABLE);
        }
        equipmentRepository.save(equipment);
    }

    /**
     * Sends a maintenance notification to the equipment's assigned manager.
     * This method is transactional and relies on the Equipment entity being managed.
     * @param equipment The equipment for which maintenance is due.
     */
    private void sendMaintenanceNotification(Equipment equipment) {
        User equipmentManager = equipment.getEquipmentManager();
        if (equipmentManager != null) {
            LocalDate nextExpectedMaintenanceDate = null;
            if (equipment.getLastMaintenanceDate() != null) {
                nextExpectedMaintenanceDate = equipment.getLastMaintenanceDate().plusDays(equipment.getMaintenanceIntervalDays());
            }

            String message;
            if (nextExpectedMaintenanceDate != null && nextExpectedMaintenanceDate.isBefore(LocalDate.now())) {
                message = String.format("📢 Equipment '%s' (S/N: %s) maintenance is overdue since %s. Please schedule immediately.",
                        equipment.getName(), equipment.getSerialNumber(), nextExpectedMaintenanceDate);
            } else if (nextExpectedMaintenanceDate != null && nextExpectedMaintenanceDate.isEqual(LocalDate.now())) {
                message = String.format("🛠️ Equipment '%s' (S/N: %s) maintenance is due today! Please take action.",
                        equipment.getName(), equipment.getSerialNumber());
            } else {
                message = String.format("⚠️ Equipment '%s' (S/N: %s) maintenance is required.",
                        equipment.getName(), equipment.getSerialNumber());
            }

            notificationService.createNotification(
                    equipmentManager.getId(),
                    message,
                    Notification.NotificationType.MAINTENANCE // Use the specific MAINTENANCE type
            );
        }
    }
}