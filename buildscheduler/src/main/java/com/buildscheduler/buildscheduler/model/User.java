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
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
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
    private Set<Role> roles;


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
    private Set<AvailabilitySlot> availabilitySlots = new HashSet<>();

    @Column(nullable = false, columnDefinition = "varchar(20) default 'INCOMPLETE'")
    private String profileStatus = "INCOMPLETE";

    public boolean isAvailable(LocalDateTime start, LocalDateTime end) {
        return availabilitySlots.stream().anyMatch(slot -> slot.covers(start, end));
    }

    // Assignments and team hierarchy
    @OneToMany(mappedBy = "worker", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @BatchSize(size = 25)
    private Set<Assignment> assignments = new HashSet<>();

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


    // Required by Spring Security
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> (GrantedAuthority) role::getName)
                .toList();
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
