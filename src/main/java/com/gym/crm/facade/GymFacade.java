package com.gym.crm.facade;

import com.gym.crm.dto.PasswordChangeRequest;
import com.gym.crm.dto.trainee.TraineeCreateRequest;
import com.gym.crm.dto.trainee.TraineeResponse;
import com.gym.crm.dto.trainee.TraineeTrainersUpdateRequest;
import com.gym.crm.dto.trainee.TraineeTrainingCriteriaRequest;
import com.gym.crm.dto.trainee.TraineeUpdateRequest;
import com.gym.crm.dto.trainer.TrainerCreateRequest;
import com.gym.crm.dto.trainer.TrainerResponse;
import com.gym.crm.dto.trainer.TrainerTrainingCriteriaRequest;
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

import java.util.List;
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
        authenticationService.validateCredentials(username, password);
        logger.debug("Facade: Getting trainee by username: {}", targetUsername);

        return traineeService.findByUsername(targetUsername);
    }

    public TraineeResponse updateTrainee(TraineeUpdateRequest request, String username, String password) {
        authenticationService.validateTraineeCredentials(username, password);
        logger.info("Facade: Updating trainee with ID: {}", request.getId());

        return traineeService.update(request);
    }

    public TraineeResponse updateTraineeTrainersList(TraineeTrainersUpdateRequest request, String username, String password) {
        authenticationService.validateTraineeCredentials(username, password);
        logger.info("Facade: Updating trainers list for trainee with username: {}", request.getTraineeUsername());

        return traineeService.updateTraineeTrainersList(request);
    }

    public void deleteTrainee(String targetUsername, String username, String password) {
        authenticationService.validateTraineeCredentials(username, password);
        logger.info("Facade: Deleting trainee with username: {}", targetUsername);
        traineeService.deleteByUsername(targetUsername);
    }

    public void changeTraineePassword(PasswordChangeRequest request) {
        authenticationService.validateTraineeCredentials(request.getUsername(), request.getOldPassword());
        logger.info("Facade: Changing password for trainee with username: {}", request.getUsername());
        traineeService.changePassword(request);
    }

    public TraineeResponse toggleTraineeActivation(String targetUsername, String username, String password) {
        authenticationService.validateCredentials(username, password);
        logger.info("Facade: Toggling activation for trainee with username: {}", targetUsername);

        return traineeService.toggleTraineeActivation(targetUsername);
    }

    public TrainerResponse createTrainer(TrainerCreateRequest request) {
        logger.info("Facade: Creating trainer");

        return trainerService.create(request);
    }

    public Optional<TrainerResponse> getTrainerByUsername(String targetUsername, String username, String password) {
        authenticationService.validateCredentials(username, password);
        logger.debug("Facade: Getting trainer by username: {}", targetUsername);

        return trainerService.findByUsername(targetUsername);
    }

    public List<TrainerResponse> getTrainersNotAssignedToTrainee(String traineeUsername, String username, String password) {
        authenticationService.validateCredentials(username, password);
        logger.debug("Facade: Getting trainers not assigned to trainee with username: {}", traineeUsername);

        return trainerService.findTrainersNotAssignedToTrainee(traineeUsername);
    }

    public TrainerResponse updateTrainer(TrainerUpdateRequest request, String password) {
        authenticationService.validateTrainerCredentials(request.getUsername(), password);
        logger.info("Facade: Updating trainer with ID: {}", request.getId());

        return trainerService.update(request);
    }

    public void changeTrainerPassword(PasswordChangeRequest request) {
        authenticationService.validateTrainerCredentials(request.getUsername(), request.getOldPassword());
        logger.info("Facade: Changing password for trainer with username: {}", request.getUsername());
        trainerService.changePassword(request);
    }

    public TrainerResponse toggleTrainerActivation(String targetUsername, String username, String password) {
        authenticationService.validateCredentials(username, password);
        logger.info("Facade: Toggling activation for trainer with username: {}", targetUsername);

        return trainerService.toggleTrainerActivation(targetUsername);
    }

    public TrainingResponse createTraining(TrainingCreateRequest training, String username, String password) {
        authenticationService.validateCredentials(username, password);
        logger.info("Facade: Creating training");

        return trainingService.create(training);
    }

    public Optional<TrainingResponse> getTrainingById(Long id, String username, String password) {
        authenticationService.validateCredentials(username, password);
        logger.debug("Facade: Getting training by ID: {}", id);

        return trainingService.findById(id);
    }

    public List<TrainingResponse> getTraineeTrainingsByCriteria(TraineeTrainingCriteriaRequest request,
                                                                String username, String password) {
        authenticationService.validateCredentials(username, password);
        logger.debug("Facade: Getting trainee trainings by criteria for username: {}", request.getTraineeUsername());

        return trainingService.getTraineeTrainingsByCriteria(
                request.getTraineeUsername(),
                request.getFromDate(),
                request.getToDate(),
                request.getTrainerName(),
                request.getTrainingType()
        );
    }

    public List<TrainingResponse> getTrainerTrainingsByCriteria(TrainerTrainingCriteriaRequest request,
                                                                String username, String password) {
        authenticationService.validateCredentials(username, password);
        logger.debug("Facade: Getting trainer trainings by criteria for username: {}", request.getTrainerUsername());

        return trainingService.getTrainerTrainingsByCriteria(
                request.getTrainerUsername(),
                request.getFromDate(),
                request.getToDate(),
                request.getTraineeName()
        );
    }
}
