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
import java.util.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
// Corrected Lombok annotations:
@EqualsAndHashCode(callSuper = true, of = "id") // <-- callSuper = true and of = "id"
@ToString(exclude = {
        "skills",
        "certifications",
        "workerAvailabilitySlots", // Corrected field name
        "workerAssignments",       // Corrected field name
        "supervisedWorkers",
        "managedTeam",
        "managedProjects",
        "supervisedTasks",
        "managedEquipment",
        "notifications",
        // Exclude self-referencing ManyToOne relationships if they can cause recursion in toString()
        // It's better to explicitly list them if they are part of the graph being fetched.
        "siteSupervisor",
        "projectManager",
        "roles" // Exclude EAGER collections from toString() to be safe and clean
})
public class User extends BaseEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Keep this ID here, it's specific to the User entity

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

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @BatchSize(size = 25)
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


    // In User.java
    public boolean isAvailable(LocalDateTime start, LocalDateTime end) {
        // ... (your existing availability logic)
        return true; // Simplified for example, keep your original logic
    }

    // Required by Spring Security
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> (GrantedAuthority) role::getName)
                .toList();
    }

    // REMOVE all manual overrides for equals(), hashCode(), and toString()
    // Lombok will generate them correctly now.
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