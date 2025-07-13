package com.gym.crm.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Entity
@Table(name = "training_types")
@Getter
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class TrainingType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "training_type_name", nullable = false, unique = true, length = 100)
    private String trainingTypeName;

    @JsonIgnore
    @OneToMany(mappedBy = "trainingType", fetch = FetchType.LAZY)
    private Set<Training> trainings;

    @JsonIgnore
    @OneToMany(mappedBy = "specialization", fetch = FetchType.LAZY)
    private Set<Trainer> trainers;
}
