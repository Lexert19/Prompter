package com.example.promptengineering.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "migration_flag")
public class MigrationFlag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    private boolean executed;

    private LocalDateTime executedAt;

    public MigrationFlag() {}

    public MigrationFlag(String name) {
        this.name = name;
        this.executed = false;
    }

}