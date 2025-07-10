package com.gym.crm.service.impl;

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

    @Override
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
    public boolean changePassword(PasswordChangeRequest request) {
        logger.debug("Changing password for trainer: {}", request.getUsername());

        Trainer trainer = trainerDAO.findByUsername(request.getUsername())
                .orElseThrow(() -> new CoreServiceException("User not found with username: " + request.getUsername()));

        if (!trainer.getUser().getPassword().equals(request.getOldPassword())) {
            logger.warn("Invalid old password for trainer: {}", request.getUsername());

            return false;
        }

        User updatedUser = trainer.getUser().toBuilder()
                .password(request.getNewPassword())
                .build();

        Trainer updatedTrainer = trainer.toBuilder()
                .user(updatedUser)
                .build();

        trainerDAO.update(updatedTrainer);

        logger.info("Password changed successfully for trainer: {}", request.getUsername());

        return true;
    }
}
