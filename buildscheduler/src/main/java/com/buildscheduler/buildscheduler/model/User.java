package com.buildscheduler.buildscheduler.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter; // Added for debugging clarity
import java.util.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
@EqualsAndHashCode(callSuper = true, of = "id")
@ToString(exclude = {
        "skills",
        "certifications",
        "workerAvailabilitySlots",
        "workerAssignments",
        "supervisedWorkers",
        "managedTeam",
        "managedProjects",
        "supervisedTasks",
        "managedEquipment",
        "notifications",
        "siteSupervisor",
        "projectManager",
        "roles"
})
public class User extends BaseEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username is required")
    @Column(unique = true)
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Column(unique = true)
    private String email;

    @Pattern(regexp = "^\\+?[0-9\\s]{10,15}$", message = "Invalid phone number")
    private String phone;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_skills",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    @BatchSize(size = 25)
    private Set<Skill> skills = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_certifications", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "certification")
    @BatchSize(size = 25)
    private Set<String> certifications = new HashSet<>();

//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
//    @BatchSize(size = 25)
//    private Set<WorkerAvailabilitySlot> workerAvailabilitySlots = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<WorkerAvailabilitySlot> workerAvailabilitySlots = new HashSet<>();

    @Column(nullable = false, columnDefinition = "varchar(20) default 'INCOMPLETE'")
    private String profileStatus = "INCOMPLETE";

    // Assignments and team hierarchy
    @OneToMany(mappedBy = "worker", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @BatchSize(size = 25)
    private Set<WorkerAssignment> workerAssignments = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_supervisor_id")
    private User siteSupervisor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_manager_id")
    private User projectManager;

    @JsonIgnore
    @OneToMany(mappedBy = "siteSupervisor", fetch = FetchType.LAZY)
    @BatchSize(size = 25)
    private Set<User> supervisedWorkers = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "projectManager", fetch = FetchType.LAZY)
    @BatchSize(size = 25)
    private Set<User> managedTeam = new HashSet<>();

    // Projects & Equipment
    @JsonIgnore
    @OneToMany(mappedBy = "projectManager", fetch = FetchType.LAZY)
    @BatchSize(size = 25)
    private Set<Project> managedProjects = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "siteSupervisor", fetch = FetchType.LAZY)
    @BatchSize(size = 25)
    private Set<MainTask> supervisedTasks = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "equipmentManager", fetch = FetchType.LAZY)
    @BatchSize(size = 25)
    private Set<Equipment> managedEquipment = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Notification> notifications = new HashSet<>();


    // **CORRECTED isAvailable METHOD**
    public boolean isAvailable(LocalDateTime desiredStart, LocalDateTime desiredEnd) {
        if (desiredStart == null || desiredEnd == null || desiredStart.isAfter(desiredEnd)) {
            System.out.println("DEBUG: Invalid desired time range.");
            return false; // Invalid time range
        }

        // --- Step 1: Check against availability slots ---
        // A worker must have at least one availability slot that fully contains the desired period.
        boolean hasAvailabilityInSlots = false;
        if (workerAvailabilitySlots != null && !workerAvailabilitySlots.isEmpty()) {
            for (WorkerAvailabilitySlot slot : workerAvailabilitySlots) {
                // Ensure the slot date matches the desired assignment date
                // Or, if slots are recurring, adjust logic accordingly.
                // For simplicity, we assume availability slots are defined for specific days.
                if (!slot.getDate().isEqual(desiredStart.toLocalDate())) {
                    continue; // This slot is for a different day
                }

                LocalTime slotStartTime = slot.getStartTime();
                LocalTime slotEndTime = slot.getEndTime();

                // Construct full LocalDateTime for the slot on the desired date
                LocalDateTime slotStartDateTime = slot.getDate().atTime(slotStartTime);
                LocalDateTime slotEndDateTime = slot.getDate().atTime(slotEndTime);

                // Handle overnight slots (e.g., 22:00 - 06:00 crosses midnight)
                if (slotEndTime.isBefore(slotStartTime)) {
                    slotEndDateTime = slotEndDateTime.plusDays(1);
                }

                // Check if the desired period is entirely within this availability slot
                // (start >= slotStart && end <= slotEnd)
                if (!desiredStart.isBefore(slotStartDateTime) && !desiredEnd.isAfter(slotEndDateTime)) {
                    hasAvailabilityInSlots = true;
                    System.out.println(String.format("DEBUG: Worker %s (ID: %d) has availability slot %s - %s that covers desired %s - %s.",
                            this.username, this.id,
                            slotStartDateTime.format(DateTimeFormatter.ofPattern("MMM dd HH:mm")),
                            slotEndDateTime.format(DateTimeFormatter.ofPattern("MMM dd HH:mm")),
                            desiredStart.format(DateTimeFormatter.ofPattern("MMM dd HH:mm")),
                            desiredEnd.format(DateTimeFormatter.ofPattern("MMM dd HH:mm"))
                    ));
                    break; // Found a suitable availability slot
                }
            }
        }

        if (!hasAvailabilityInSlots) {
            System.out.println(String.format("DEBUG: Worker %s (ID: %d) does NOT have an availability slot for %s - %s.",
                    this.username, this.id,
                    desiredStart.format(DateTimeFormatter.ofPattern("MMM dd HH:mm")),
                    desiredEnd.format(DateTimeFormatter.ofPattern("MMM dd HH:mm"))
            ));
            return false; // No availability slot found for the desired period
        }

        // --- Step 2: Check against existing worker assignments ---
        // A worker is NOT available if the desired period overlaps with any existing assignment.
        boolean overlapsExistingAssignment = false;
        if (workerAssignments != null && !workerAssignments.isEmpty()) {
            for (WorkerAssignment assignment : workerAssignments) {
                LocalDateTime assignmentStart = assignment.getAssignmentStart();
                LocalDateTime assignmentEnd = assignment.getAssignmentEnd();

                // Overlap condition: (StartA < EndB) && (EndA > StartB)
                // This means the two time ranges intersect.
                if (desiredStart.isBefore(assignmentEnd) && desiredEnd.isAfter(assignmentStart)) {
                    overlapsExistingAssignment = true;
                    System.out.println(String.format("DEBUG: Worker %s (ID: %d) has existing assignment %s - %s that OVERLAPS with desired %s - %s.",
                            this.username, this.id,
                            assignmentStart.format(DateTimeFormatter.ofPattern("MMM dd HH:mm")),
                            assignmentEnd.format(DateTimeFormatter.ofPattern("MMM dd HH:mm")),
                            desiredStart.format(DateTimeFormatter.ofPattern("MMM dd HH:mm")),
                            desiredEnd.format(DateTimeFormatter.ofPattern("MMM dd HH:mm"))
                    ));
                    break; // Found an overlapping assignment
                }
            }
        }

        if (overlapsExistingAssignment) {
            System.out.println(String.format("DEBUG: Worker %s (ID: %d) is NOT available due to existing assignment overlap for %s - %s.",
                    this.username, this.id,
                    desiredStart.format(DateTimeFormatter.ofPattern("MMM dd HH:mm")),
                    desiredEnd.format(DateTimeFormatter.ofPattern("MMM dd HH:mm"))
            ));
            return false;
        }

        // If we reached here, it means:
        // 1. There is an availability slot that fully contains the desired period.
        // 2. The desired period does not overlap with any existing assignment.
        System.out.println(String.format("DEBUG: Worker %s (ID: %d) IS available for %s - %s.",
                this.username, this.id,
                desiredStart.format(DateTimeFormatter.ofPattern("MMM dd HH:mm")),
                desiredEnd.format(DateTimeFormatter.ofPattern("MMM dd HH:mm"))
        ));
        return true;
    }


    // Required by Spring Security
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> (GrantedAuthority) role::getName)
                .toList();
    }

    // Helper method to check roles
    public boolean hasRole(String roleName) {
        return this.getRoles() != null && this.getRoles().stream()
                .anyMatch(role -> role.getName().equals(roleName));
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}