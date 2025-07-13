package com.gym.crm.service;

import com.gym.crm.dto.PasswordChangeRequest;
import com.gym.crm.dto.trainer.TrainerCreateRequest;
import com.gym.crm.dto.trainer.TrainerResponse;
import com.gym.crm.dto.trainer.TrainerUpdateRequest;

import java.util.List;
import java.util.Optional;

public interface TrainerService {
    TrainerResponse create(TrainerCreateRequest request);

    Optional<TrainerResponse> findById(Long id);

    Optional<TrainerResponse> findByUsername(String username);

    List<TrainerResponse> findTrainersNotAssignedToTrainee(String traineeUsername);

    TrainerResponse update(TrainerUpdateRequest request);

    void changePassword(PasswordChangeRequest request);

    TrainerResponse toggleTrainerActivation(String username);
}
