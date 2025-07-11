package com.gym.crm.service;

import com.gym.crm.dto.PasswordChangeRequest;
import com.gym.crm.dto.trainee.TraineeCreateRequest;
import com.gym.crm.dto.trainee.TraineeResponse;
import com.gym.crm.dto.trainee.TraineeUpdateRequest;

import java.util.Optional;

public interface TraineeService {
    TraineeResponse create(TraineeCreateRequest request);

    Optional<TraineeResponse> findById(Long id);

    Optional<TraineeResponse> findByUsername(String username);

    TraineeResponse update(TraineeUpdateRequest request);

    void deleteByUsername(String username);

    void changePassword(PasswordChangeRequest request);
}
