package com.gym.crm.service;

import com.gym.crm.dto.training.TrainingCreateRequest;
import com.gym.crm.dto.training.TrainingResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TrainingService {
    TrainingResponse create(TrainingCreateRequest training);

    Optional<TrainingResponse> findById(Long id);

    List<TrainingResponse> getTraineeTrainingsByCriteria(String traineeUsername, LocalDate fromDate, LocalDate toDate,
                                                         String trainerName, String trainingType);

    List<TrainingResponse> getTrainerTrainingsByCriteria(String trainerUsername, LocalDate fromDate, LocalDate toDate,
                                                         String traineeName);
}
