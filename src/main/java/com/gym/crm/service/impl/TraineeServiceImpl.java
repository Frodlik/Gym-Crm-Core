package com.gym.crm.service.impl;

import com.gym.crm.dao.TraineeDAO;
import com.gym.crm.dto.PasswordChangeRequest;
import com.gym.crm.dto.trainee.TraineeCreateRequest;
import com.gym.crm.dto.trainee.TraineeResponse;
import com.gym.crm.dto.trainee.TraineeUpdateRequest;
import com.gym.crm.exception.CoreServiceException;
import com.gym.crm.mapper.TraineeMapper;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.User;
import com.gym.crm.service.TraineeService;
import com.gym.crm.util.UserCredentialsGenerator;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TraineeServiceImpl implements TraineeService {
    private static final Logger logger = LoggerFactory.getLogger(TraineeServiceImpl.class);

    private TraineeDAO traineeDAO;
    private UserCredentialsGenerator userCredentialsGenerator;
    private TraineeMapper traineeMapper;

    @Autowired
    public void setTraineeDAO(TraineeDAO traineeDAO) {
        this.traineeDAO = traineeDAO;
    }

    @Autowired
    public void setUserCredentialsGenerator(UserCredentialsGenerator userCredentialsGenerator) {
        this.userCredentialsGenerator = userCredentialsGenerator;
    }

    @Autowired
    public void setTraineeMapper(TraineeMapper traineeMapper) {
        this.traineeMapper = traineeMapper;
    }

    @Override
    public TraineeResponse create(@Valid TraineeCreateRequest request) {
        logger.debug("Creating trainee: {} {}", request.getFirstName(), request.getLastName());

        Trainee trainee = traineeMapper.toEntity(request);

        List<String> existingUsernames = traineeDAO.findAll().stream()
                .map(t -> t.getUser().getUsername())
                .toList();

        String username = userCredentialsGenerator.generateUsername(
                request.getFirstName(), request.getLastName(), existingUsernames);
        String password = userCredentialsGenerator.generatePassword();

        User updatedUser = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .username(username)
                .password(password)
                .isActive(true)
                .build();
        trainee = trainee.toBuilder()
                .user(updatedUser)
                .build();

        Trainee saved = traineeDAO.create(trainee);

        logger.info("Successfully created trainee with ID: {} and username: {}", saved.getId(), saved.getUser().getUsername());

        return traineeMapper.toResponse(saved);
    }

    @Override
    public Optional<TraineeResponse> findById(Long id) {
        logger.debug("Finding trainee by ID: {}", id);

        return traineeDAO.findById(id)
                .map(traineeMapper::toResponse);
    }

    @Override
    public Optional<TraineeResponse> findByUsername(String username) {
        logger.debug("Finding trainee by username: {}", username);

        return traineeDAO.findByUsername(username)
                .map(traineeMapper::toResponse);
    }

    @Override
    public TraineeResponse update(@Valid TraineeUpdateRequest request) {
        logger.debug("Updating trainee with ID: {}", request.getId());

        Optional<Trainee> existingTrainee = traineeDAO.findById(request.getId());
        if (existingTrainee.isEmpty()) {
            throw new CoreServiceException("Trainee not found with id: " + request.getId());
        }

        Trainee trainee = existingTrainee.get();

        User updatedUser = trainee.getUser().toBuilder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .username(request.getUsername())
                .isActive(request.getIsActive())
                .build();
        trainee = trainee.toBuilder()
                .user(updatedUser)
                .dateOfBirth(request.getDateOfBirth())
                .address(request.getAddress())
                .build();

        Trainee updatedTrainee = traineeDAO.update(trainee);

        logger.info("Successfully updated trainee with ID: {}", request.getId());

        return traineeMapper.toResponse(updatedTrainee);
    }

    @Override
    public void deleteByUsername(String username) {
        logger.debug("Deleting trainee by username: {}", username);

        traineeDAO.findByUsername(username)
                .orElseThrow(() -> new CoreServiceException("Trainee not found with username: " + username));

        traineeDAO.deleteByUsername(username);

        logger.info("Trainee deleted with username: {}", username);
    }

    @Override
    public void changePassword(PasswordChangeRequest request) {
        logger.debug("Changing password for trainee: {}", request.getUsername());

        Trainee trainee = traineeDAO.findByUsername(request.getUsername())
                .orElseThrow(() -> new CoreServiceException("User not found with username: " + request.getUsername()));

        if (!trainee.getUser().getPassword().equals(request.getOldPassword())) {
            throw new CoreServiceException("Invalid old password");
        }

        User updatedUser = trainee.getUser().toBuilder()
                .password(request.getNewPassword())
                .build();
        Trainee updatedTrainee = trainee.toBuilder()
                .user(updatedUser)
                .build();

        traineeDAO.update(updatedTrainee);

        logger.info("Password changed successfully for trainee: {}", request.getUsername());
    }
}
