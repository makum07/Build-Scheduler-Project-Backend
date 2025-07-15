package com.buildscheduler.buildscheduler.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;

@Entity
@Table(name = "skills")
@Getter @Setter @NoArgsConstructor
public class Skill extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String name;
}