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
import com.gym.crm.service.AuthenticationService;
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
    private final AuthenticationService authenticationService;

    public GymFacade(TraineeService traineeService, TrainerService trainerService,
                     TrainingService trainingService, AuthenticationService authenticationService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
        this.authenticationService = authenticationService;
    }

    public TraineeResponse createTrainee(TraineeCreateRequest request) {
        logger.info("Facade: Creating trainee");
        return traineeService.create(request);
    }

    public Optional<TraineeResponse> getTraineeByUsername(String targetUsername, String username, String password) {
        logger.debug("Facade: Getting trainee by username: {}", targetUsername);
        authenticationService.validateCredentials(username, password);
        return traineeService.findByUsername(targetUsername);
    }

    public TraineeResponse updateTrainee(TraineeUpdateRequest request, String username, String password) {
        logger.info("Facade: Updating trainee with ID: {}", request.getId());
        authenticationService.validateTraineeCredentials(username, password);
        return traineeService.update(request);
    }

    public void deleteTrainee(String targetUsername, String username, String password) {
        logger.info("Facade: Deleting trainee with username: {}", targetUsername);
        authenticationService.validateTraineeCredentials(username, password);
        traineeService.deleteByUsername(targetUsername);
    }

    public void changeTraineePassword(PasswordChangeRequest request) {
        logger.info("Facade: Changing password for trainee with username: {}", request.getUsername());
        authenticationService.validateTraineeCredentials(request.getUsername(), request.getOldPassword());
        traineeService.changePassword(request);
    }

    public TrainerResponse createTrainer(TrainerCreateRequest request) {
        logger.info("Facade: Creating trainer");
        return trainerService.create(request);
    }

    public Optional<TrainerResponse> getTrainerByUsername(String targetUsername, String username, String password) {
        logger.debug("Facade: Getting trainer by username: {}", targetUsername);
        authenticationService.validateCredentials(username, password);
        return trainerService.findByUsername(targetUsername);
    }

    public TrainerResponse updateTrainer(TrainerUpdateRequest request, String password) {
        logger.info("Facade: Updating trainer with ID: {}", request.getId());
        authenticationService.validateTrainerCredentials(request.getUsername(), password);
        return trainerService.update(request);
    }

    public void changeTrainerPassword(PasswordChangeRequest request) {
        logger.info("Facade: Changing password for trainer with username: {}", request.getUsername());
        authenticationService.validateTrainerCredentials(request.getUsername(), request.getOldPassword());
        trainerService.changePassword(request);
    }

    public TrainingResponse createTraining(TrainingCreateRequest training, String username, String password) {
        logger.info("Facade: Creating training");
        authenticationService.validateCredentials(username, password);
        return trainingService.create(training);
    }

    public Optional<TrainingResponse> getTrainingById(Long id, String username, String password) {
        logger.debug("Facade: Getting training by ID: {}", id);
        authenticationService.validateCredentials(username, password);
        return trainingService.findById(id);
    }
}
