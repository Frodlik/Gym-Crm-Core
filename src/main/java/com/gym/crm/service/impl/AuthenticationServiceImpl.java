package com.gym.crm.service.impl;

import com.gym.crm.dao.TraineeDAO;
import com.gym.crm.dao.TrainerDAO;
import com.gym.crm.exception.CoreServiceException;
import com.gym.crm.exception.NotAuthenticatedException;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.service.AuthenticationService;
import com.gym.crm.util.UserCredentialsGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationServiceImpl.class);

    private TraineeDAO traineeDAO;
    private TrainerDAO trainerDAO;
    private UserCredentialsGenerator userCredentialsGenerator;

    @Autowired
    public void setTraineeDAO(TraineeDAO traineeDAO) {
        this.traineeDAO = traineeDAO;
    }

    @Autowired
    public void setTrainerDAO(TrainerDAO trainerDAO) {
        this.trainerDAO = trainerDAO;
    }

    @Autowired
    public void setUserCredentialsGenerator(UserCredentialsGenerator userCredentialsGenerator) {
        this.userCredentialsGenerator = userCredentialsGenerator;
    }

    public String validateCredentials(String username, String password) {
        logger.debug("Validating credentials for user: {}", username);

        if (username == null || password == null) {
            throw new NotAuthenticatedException("Username and password are required");
        }

        Optional<Trainee> trainee = traineeDAO.findByUsername(username);
        if (trainee.isPresent()) {
            if (userCredentialsGenerator.matches(password, trainee.get().getUser().getPassword())) {
                logger.info("Trainee authenticated successfully: {}", username);

                return "TRAINEE";
            } else {
                throw new NotAuthenticatedException("Invalid password for trainee: " + username);
            }
        }

        Optional<Trainer> trainer = trainerDAO.findByUsername(username);
        if (trainer.isPresent()) {
            if (userCredentialsGenerator.matches(password, trainer.get().getUser().getPassword())) {
                logger.info("Trainer authenticated successfully: {}", username);

                return "TRAINER";
            } else {
                throw new NotAuthenticatedException("Invalid password for trainer: " + username);
            }
        }

        throw new CoreServiceException("User not found: " + username);
    }

    public void validateTraineeCredentials(String username, String password) {
        String userType = validateCredentials(username, password);
        if (!"TRAINEE".equals(userType)) {
            throw new NotAuthenticatedException("Access denied. Only trainees can perform this operation.");
        }
    }

    public void validateTrainerCredentials(String username, String password) {
        String userType = validateCredentials(username, password);
        if (!"TRAINER".equals(userType)) {
            throw new NotAuthenticatedException("Access denied. Only trainers can perform this operation.");
        }
    }
}
