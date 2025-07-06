package com.gym.crm.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Table(name = "trainings")
@Getter
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class Training {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "training_name", nullable = false, length = 100)
    private String trainingName;

    @Column(name = "training_date", nullable = false)
    private LocalDate trainingDate;

    @Column(name = "training_duration", nullable = false)
    private Integer trainingDuration;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainee_id", referencedColumnName = "id")
    private Trainee trainee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id", referencedColumnName = "id")
    private Trainer trainer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_type_id", referencedColumnName = "id")
    private TrainingType trainingType;
}
