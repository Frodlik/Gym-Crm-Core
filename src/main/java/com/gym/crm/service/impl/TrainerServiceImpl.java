package com.gym.crm.service.impl;

import com.gym.crm.dao.TraineeDAO;
import com.gym.crm.dao.TrainerDAO;
import com.gym.crm.dto.PasswordChangeRequest;
import com.gym.crm.dto.trainer.TrainerCreateRequest;
import com.gym.crm.dto.trainer.TrainerResponse;
import com.gym.crm.dto.trainer.TrainerUpdateRequest;
import com.gym.crm.exception.CoreServiceException;
import com.gym.crm.mapper.TrainerMapper;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.User;
import com.gym.crm.service.TrainerService;
import com.gym.crm.service.transaction.PersistenceTx;
import com.gym.crm.util.UserCredentialsGenerator;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TrainerServiceImpl implements TrainerService {
    private static final Logger logger = LoggerFactory.getLogger(TrainerServiceImpl.class);

    private TrainerDAO trainerDAO;
    private TraineeDAO traineeDAO;
    private UserCredentialsGenerator userCredentialsGenerator;
    private TrainerMapper trainerMapper;

    @Autowired
    public void setTrainerDAO(TrainerDAO trainerDAO) {
        this.trainerDAO = trainerDAO;
    }

    @Autowired
    public void setUserCredentialsGenerator(UserCredentialsGenerator userCredentialsGenerator) {
        this.userCredentialsGenerator = userCredentialsGenerator;
    }

    @Autowired
    public void setTrainerMapper(TrainerMapper trainerMapper) {
        this.trainerMapper = trainerMapper;
    }

    @Autowired
    public void setTraineeDAO(TraineeDAO traineeDAO) {
        this.traineeDAO = traineeDAO;
    }

    @Override
    @PersistenceTx
    public TrainerResponse create(@Valid TrainerCreateRequest request) {
        logger.debug("Creating trainer: {} {}", request.getFirstName(), request.getLastName());

        Trainer trainer = trainerMapper.toEntity(request);

        List<String> existingUsernames = trainerDAO.findAll().stream()
                .map(t -> t.getUser().getUsername())
                .toList();

        String username = userCredentialsGenerator.generateUsername(
                trainer.getUser().getFirstName(), trainer.getUser().getLastName(), existingUsernames);
        String password = userCredentialsGenerator.generatePassword();

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .username(username)
                .password(password)
                .isActive(true)
                .build();
        trainer = Trainer.builder()
                .user(user)
                .specialization(request.getSpecialization())
                .build();

        Trainer saved = trainerDAO.create(trainer);

        logger.info("Successfully created trainer with ID: {} and username: {}", saved.getId(), saved.getUser().getUsername());

        return trainerMapper.toResponse(saved);
    }

    @Override
    public Optional<TrainerResponse> findById(Long id) {
        logger.debug("Finding trainer by ID: {}", id);

        return trainerDAO.findById(id)
                .map(trainerMapper::toResponse);
    }

    @Override
    public Optional<TrainerResponse> findByUsername(String username) {
        logger.debug("Finding trainer by username: {}", username);

        return trainerDAO.findByUsername(username)
                .map(trainerMapper::toResponse);
    }

    @Override
    public List<TrainerResponse> findTrainersNotAssignedToTrainee(String traineeUsername) {
        logger.debug("Finding trainers not assigned to trainee with username: {}", traineeUsername);

        traineeDAO.findByUsername(traineeUsername)
                .orElseThrow(() -> new CoreServiceException("Trainee not found with username: " + traineeUsername));

        List<Trainer> trainers = trainerDAO.findTrainersNotAssignedToTrainee(traineeUsername);

        logger.info("Found {} trainers not assigned to trainee: {}", trainers.size(), traineeUsername);

        return trainers.stream()
                .map(trainerMapper::toResponse)
                .toList();
    }

    @Override
    @PersistenceTx
    public TrainerResponse update(@Valid TrainerUpdateRequest request) {
        logger.debug("Updating trainer with ID: {}", request.getId());

        Optional<Trainer> existingTrainer = trainerDAO.findById(request.getId());
        if (existingTrainer.isEmpty()) {
            throw new CoreServiceException("Trainer not found with id: " + request.getId());
        }

        Trainer trainer = existingTrainer.get();

        User updatedUser = trainer.getUser().toBuilder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .username(request.getUsername())
                .isActive(request.getIsActive())
                .build();
        trainer = trainer.toBuilder()
                .user(updatedUser)
                .specialization(request.getSpecialization())
                .build();

        Trainer updatedTrainer = trainerDAO.update(trainer);

        logger.info("Successfully updated trainer with ID: {}", request.getId());

        return trainerMapper.toResponse(updatedTrainer);
    }

    @Override
    public void changePassword(PasswordChangeRequest request) {
        logger.debug("Changing password for trainer: {}", request.getUsername());

        Trainer trainer = trainerDAO.findByUsername(request.getUsername())
                .orElseThrow(() -> new CoreServiceException("User not found with username: " + request.getUsername()));

        if (!trainer.getUser().getPassword().equals(request.getOldPassword())) {
            throw new CoreServiceException("Invalid old password");
        }

        User updatedUser = trainer.getUser().toBuilder()
                .password(request.getNewPassword())
                .build();
        Trainer updatedTrainer = trainer.toBuilder()
                .user(updatedUser)
                .build();

        trainerDAO.update(updatedTrainer);

        logger.info("Password changed successfully for trainer: {}", request.getUsername());
    }

    @Override
    @PersistenceTx
    public TrainerResponse toggleTrainerActivation(String username) {
        logger.debug("Toggling activation for trainer with username: {}", username);

        Trainer trainer = trainerDAO.findByUsername(username)
                .orElseThrow(() -> new CoreServiceException("Trainer not found with username: " + username));

        boolean isActive = !trainer.getUser().getIsActive();

        User updatedUser = trainer.getUser().toBuilder()
                .isActive(isActive)
                .build();
        Trainer updatedTrainer = trainer.toBuilder()
                .user(updatedUser)
                .build();

        Trainer savedTrainer = trainerDAO.update(updatedTrainer);

        logger.info("Successfully toggled activation for trainer with username: {} to {}",
                username, isActive ? "active" : "inactive");

        return trainerMapper.toResponse(savedTrainer);
    }
}
