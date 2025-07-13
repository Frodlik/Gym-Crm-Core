package com.gym.crm.dao;

import com.gym.crm.model.Trainer;

import java.util.List;
import java.util.Optional;

public interface TrainerDAO {
    Trainer create(Trainer trainer);

    Optional<Trainer> findById(Long id);

    Optional<Trainer> findByUsername(String username);

    List<Trainer> findAll();

    List<Trainer> findTrainersNotAssignedToTrainee(String traineeUsername);

    Trainer update(Trainer trainer);
}
