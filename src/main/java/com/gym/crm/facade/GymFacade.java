package com.gym.crm.facade;

import com.gym.crm.dto.PasswordChangeRequest;
import com.gym.crm.dto.trainee.TraineeCreateRequest;
import com.gym.crm.dto.trainee.TraineeResponse;
import com.gym.crm.dto.trainee.TraineeUpdateRequest;
import com.gym.crm.dto.trainer.TrainerCreateRequest;
import com.gym.crm.dto.trainer.TrainerResponse;
import com.gym.crm.dto.trainer.TrainerUpdateRequest;
import com.gym.crm.dto.training.TrainingCreateRequest;
import com.gym.crm.dto.training.TrainingResponse;
import com.gym.crm.service.TraineeService;
import com.gym.crm.service.TrainerService;
import com.gym.crm.service.TrainingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class GymFacade {
    private static final Logger logger = LoggerFactory.getLogger(GymFacade.class);

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;

    public GymFacade(TraineeService traineeService, TrainerService trainerService, TrainingService trainingService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
    }

    public TraineeResponse createTrainee(TraineeCreateRequest request) {
        logger.info("Facade: Creating trainee");
        return traineeService.create(request);
    }

    public Optional<TraineeResponse> getTraineeById(Long id) {
        logger.debug("Facade: Getting trainee by ID: {}", id);
        return traineeService.findById(id);
    }

    public TraineeResponse updateTrainee(TraineeUpdateRequest request) {
        logger.info("Facade: Updating trainee with ID: {}", request.getId());
        return traineeService.update(request);
    }

    public void deleteTrainee(Long id) {
        logger.info("Facade: Deleting trainee with ID: {}", id);
        traineeService.delete(id);
    }

    public boolean changeTraineePassword(PasswordChangeRequest request) {
        logger.info("Facade: Changing password for trainee with username: {}", request.getUsername());
        return traineeService.changePassword(request);
    }

    public TrainerResponse createTrainer(TrainerCreateRequest request) {
        logger.info("Facade: Creating trainer");
        return trainerService.create(request);
    }

    public Optional<TrainerResponse> getTrainerById(Long id) {
        logger.debug("Facade: Getting trainer by ID: {}", id);
        return trainerService.findById(id);
    }

    public TrainerResponse updateTrainer(TrainerUpdateRequest request) {
        logger.info("Facade: Updating trainer with ID: {}", request.getId());
        return trainerService.update(request);
    }

    public boolean changeTrainerPassword(PasswordChangeRequest request) {
        logger.info("Facade: Changing password for trainer with username: {}", request.getUsername());
        return trainerService.changePassword(request);
    }

    public TrainingResponse createTraining(TrainingCreateRequest training) {
        logger.info("Facade: Creating training");
        return trainingService.create(training);
    }

    public Optional<TrainingResponse> getTrainingById(Long id) {
        logger.debug("Facade: Getting training by ID: {}", id);
        return trainingService.findById(id);
    }
}
