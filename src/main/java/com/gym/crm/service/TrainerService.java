package com.gym.crm.service;

import com.gym.crm.dto.PasswordChangeRequest;
import com.gym.crm.dto.trainer.TrainerCreateRequest;
import com.gym.crm.dto.trainer.TrainerResponse;
import com.gym.crm.dto.trainer.TrainerUpdateRequest;

import java.util.Optional;

public interface TrainerService {
    TrainerResponse create(TrainerCreateRequest request);

    Optional<TrainerResponse> findById(Long id);

    Optional<TrainerResponse> findByUsername(String username);

    TrainerResponse update(TrainerUpdateRequest request);

    boolean changePassword(PasswordChangeRequest request);
}
