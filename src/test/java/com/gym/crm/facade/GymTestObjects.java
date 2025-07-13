package com.gym.crm.facade;

import com.gym.crm.dto.PasswordChangeRequest;
import com.gym.crm.dto.trainee.TraineeCreateRequest;
import com.gym.crm.dto.trainee.TraineeResponse;
import com.gym.crm.dto.trainee.TraineeTrainersUpdateRequest;
import com.gym.crm.dto.trainee.TraineeUpdateRequest;
import com.gym.crm.dto.trainer.TrainerCreateRequest;
import com.gym.crm.dto.trainer.TrainerResponse;
import com.gym.crm.dto.trainer.TrainerUpdateRequest;
import com.gym.crm.dto.training.TrainingCreateRequest;
import com.gym.crm.dto.training.TrainingResponse;
import com.gym.crm.model.TrainingType;

import java.time.LocalDate;
import java.util.List;

public class GymTestObjects {
    public static final String FIRST_NAME = "John";
    public static final String LAST_NAME = "Doe";
    public static final String USERNAME = "john.doe";
    public static final String PASSWORD = "LuNdYs6oTA";
    public static final String TRAINER_FIRST_NAME = "Mike";
    public static final String TRAINER_LAST_NAME = "Johnson";
    public static final String TRAINER_USERNAME = "mike.johnson";
    public static final String TRAINING_NAME = "Morning Workout";
    public static final LocalDate BIRTH_DATE = LocalDate.of(1990, 1, 1);
    public static final LocalDate TRAINING_DATE = LocalDate.of(2024, 1, 15);
    public static final String ADDRESS = "123 Main St";
    public static final String FITNESS_TYPE = "FITNESS";
    public static final String YOGA_TYPE = "YOGA";
    public static final int TRAINING_DURATION = 60;
    public static final Long TRAINEE_ID = 1L;
    public static final Long TRAINER_ID = 2L;
    public static final Long TRAINING_ID = 3L;

    public static TraineeCreateRequest buildTraineeCreateRequest() {
        TraineeCreateRequest request = new TraineeCreateRequest();
        request.setFirstName(FIRST_NAME);
        request.setLastName(LAST_NAME);
        request.setDateOfBirth(BIRTH_DATE);
        request.setAddress(ADDRESS);

        return request;
    }

    public static TraineeUpdateRequest buildTraineeUpdateRequest() {
        TraineeUpdateRequest request = new TraineeUpdateRequest();
        request.setId(TRAINEE_ID);
        request.setFirstName("Jane");
        request.setLastName("Smith");
        request.setIsActive(false);
        request.setDateOfBirth(LocalDate.of(1985, 5, 15));
        request.setAddress("456 Oak Ave");

        return request;
    }

    public static TraineeResponse buildTraineeResponse() {
        TraineeResponse response = new TraineeResponse();
        response.setId(TRAINEE_ID);
        response.setFirstName(FIRST_NAME);
        response.setLastName(LAST_NAME);
        response.setUsername(USERNAME);
        response.setActive(true);
        response.setDateOfBirth(BIRTH_DATE);
        response.setAddress(ADDRESS);

        return response;
    }

    public static TrainerCreateRequest buildTrainerCreateRequest() {
        TrainerCreateRequest request = new TrainerCreateRequest();
        request.setFirstName(TRAINER_FIRST_NAME);
        request.setLastName(TRAINER_LAST_NAME);
        request.setSpecialization(TrainingType.builder().trainingTypeName(FITNESS_TYPE).build());

        return request;
    }

    public static TrainerUpdateRequest buildTrainerUpdateRequest() {
        TrainerUpdateRequest request = new TrainerUpdateRequest();
        request.setId(TRAINER_ID);
        request.setFirstName("Michael");
        request.setLastName("Smith");
        request.setIsActive(false);
        request.setSpecialization(TrainingType.builder().trainingTypeName(YOGA_TYPE).build());

        return request;
    }

    public static TrainerResponse buildTrainerResponse() {
        TrainerResponse response = new TrainerResponse();
        response.setId(TRAINER_ID);
        response.setFirstName(TRAINER_FIRST_NAME);
        response.setLastName(TRAINER_LAST_NAME);
        response.setUsername(TRAINER_USERNAME);
        response.setActive(true);
        response.setSpecialization(TrainingType.builder().trainingTypeName(FITNESS_TYPE).build());

        return response;
    }

    public static TrainingCreateRequest buildTrainingCreateRequest() {
        TrainingCreateRequest request = new TrainingCreateRequest();
        request.setTraineeId(TRAINEE_ID);
        request.setTrainerId(TRAINER_ID);
        request.setTrainingName(TRAINING_NAME);
        request.setTrainingDate(TRAINING_DATE);
        request.setTrainingDuration(TRAINING_DURATION);

        return request;
    }

    public static TrainingResponse buildTrainingResponse() {
        TrainingResponse response = new TrainingResponse();
        response.setId(TRAINING_ID);
        response.setTraineeUsername(USERNAME);
        response.setTrainerUsername(TRAINER_USERNAME);
        response.setTrainingName(TRAINING_NAME);
        response.setTrainingTypeName(FITNESS_TYPE);
        response.setTrainingDate(TRAINING_DATE);
        response.setTrainingDuration(TRAINING_DURATION);

        return response;
    }

    public static PasswordChangeRequest buildPasswordChangeRequest() {
        PasswordChangeRequest request = new PasswordChangeRequest();
        request.setUsername(USERNAME);
        request.setOldPassword("oldPassword");
        request.setNewPassword("newPassword");

        return request;
    }

    public static TraineeTrainersUpdateRequest buildTraineeTrainersUpdateRequest() {
        TraineeTrainersUpdateRequest request = new TraineeTrainersUpdateRequest();
        request.setTraineeUsername(USERNAME);
        request.setTrainerUsernames(List.of(TRAINER_USERNAME, "trainer2.username"));

        return request;
    }
}