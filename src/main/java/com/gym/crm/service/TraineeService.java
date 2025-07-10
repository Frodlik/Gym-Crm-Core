package com.gym.crm.service;

import com.gym.crm.dto.PasswordChangeRequest;
import com.gym.crm.dto.trainee.TraineeCreateRequest;
import com.gym.crm.dto.trainee.TraineeResponse;
import com.gym.crm.dto.trainee.TraineeUpdateRequest;

import java.util.Optional;

public interface TraineeService {
    TraineeResponse create(TraineeCreateRequest request);

    Optional<TraineeResponse> findById(Long id);

    TraineeResponse update(TraineeUpdateRequest request);

    void delete(Long id);

    boolean changePassword(PasswordChangeRequest request);
}
