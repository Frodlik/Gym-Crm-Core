package com.gym.crm.dao;

import com.gym.crm.model.Trainee;

import java.util.List;
import java.util.Optional;

public interface TraineeDAO {
    Trainee create(Trainee trainee);

    Optional<Trainee> findById(Long id);

    Optional<Trainee> findByUsername(String username);

    List<Trainee> findAll();

    Trainee update(Trainee trainee);

    boolean delete(Long id);
}
