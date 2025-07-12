package com.gym.crm.dao;

import com.gym.crm.model.Training;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TrainingDAO {
    Training create(Training training);

    Optional<Training> findById(Long id);

    List<Training> findAll();

    List<Training> findTraineeTrainingsByCriteria(String traineeUsername, LocalDate fromDate, LocalDate toDate,
                                                  String trainerName, String trainingType);

    List<Training> findTrainerTrainingsByCriteria(String trainerUsername, LocalDate fromDate, LocalDate toDate,
                                                  String traineeName);
}
