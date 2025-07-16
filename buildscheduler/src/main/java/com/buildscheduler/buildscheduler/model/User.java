package com.buildscheduler.buildscheduler.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User extends BaseEntity implements UserDetails {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();



    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "user_skills",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id"))
    private Set<Skill> skills = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "user_certifications", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "certification")
    private Set<String> certifications = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AvailabilitySlot> availabilitySlots = new HashSet<>();

    @Column(nullable = false, columnDefinition = "varchar(20) default 'INCOMPLETE'")
    private String profileStatus = "INCOMPLETE";

    public boolean isAvailable(LocalDateTime start, LocalDateTime end) {
        return availabilitySlots.stream().anyMatch(slot -> slot.covers(start, end));
    }

    // Assignments and relationships
    @OneToMany(mappedBy = "worker", cascade = CascadeType.ALL)
    private Set<Assignment> assignments = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_supervisor_id")
    private User siteSupervisor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_manager_id")
    private User projectManager;

    // Inverse relationships for hierarchy
    @OneToMany(mappedBy = "siteSupervisor", fetch = FetchType.LAZY)
    private Set<User> supervisedWorkers = new HashSet<>();

    @OneToMany(mappedBy = "projectManager", fetch = FetchType.LAZY)
    private Set<User> managedTeam = new HashSet<>();

    // Projects relationships
    @OneToMany(mappedBy = "projectManager", fetch = FetchType.LAZY)
    private Set<Project> managedProjects = new HashSet<>();

    @OneToMany(mappedBy = "siteSupervisor", fetch = FetchType.LAZY)
    private Set<MainTask> supervisedTasks = new HashSet<>();

    // Equipment management
    @OneToMany(mappedBy = "equipmentManager", fetch = FetchType.LAZY)
    private Set<Equipment> managedEquipment = new HashSet<>();




    // Required by UserDetails interface
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