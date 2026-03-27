package com.example.task_service.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String name;
    String description;

    @Enumerated(EnumType.STRING)
    TaskStatus status = TaskStatus.NEW;

    @ManyToOne
    @JoinColumn(name = "executor_id")
    User executor;
}
