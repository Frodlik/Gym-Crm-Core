package com.gym.crm.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Entity
@Table(name = "trainers")
@Getter
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class Trainer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialization", referencedColumnName = "id")
    private TrainingType specialization;

    @ManyToMany(mappedBy = "trainers", fetch = FetchType.LAZY)
    private Set<Trainee> trainees;

    @OneToMany(mappedBy = "trainer", cascade = CascadeType.ALL)
    private Set<Training> trainings;
}
