package com.gym.crm.service.impl;

import com.gym.crm.dao.TraineeDAO;
import com.gym.crm.dao.TrainerDAO;
import com.gym.crm.dao.TrainingDAO;
import com.gym.crm.dto.training.TrainingCreateRequest;
import com.gym.crm.dto.training.TrainingResponse;
import com.gym.crm.exception.CoreServiceException;
import com.gym.crm.facade.GymTestObjects;
import com.gym.crm.mapper.TrainingMapper;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.Training;
import com.gym.crm.model.TrainingType;
import com.gym.crm.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingServiceImplTest {
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String USERNAME = "john.doe";
    private static final String TRAINER_FIRST_NAME = "Mike";
    private static final String TRAINER_LAST_NAME = "Johnson";
    private static final String TRAINER_USERNAME = "mike.johnson";
    private static final String TRAINING_NAME = "Morning Workout";
    private static final String FITNESS_TYPE = "Fitness";
    private static final LocalDate TRAINING_DATE = LocalDate.of(2024, 1, 15);
    private static final int TRAINING_DURATION = 60;
    private static final Long TRAINEE_ID = 1L;
    private static final Long TRAINER_ID = 2L;
    private static final Long TRAINING_ID = 1L;

    private final Training training = buildTraining();
    private final Trainee trainee = buildTrainee();
    private final Trainer trainer = buildTrainer();

    @Mock
    private TrainingDAO trainingDAO;
    @Mock
    private TraineeDAO traineeDAO;
    @Mock
    private TrainerDAO trainerDAO;
    @Mock
    private TrainingMapper trainingMapper;
    @InjectMocks
    private TrainingServiceImpl service;

    @Test
    void create_ShouldCreateTrainingSuccessfully() {
        TrainingCreateRequest createRequest = GymTestObjects.buildTrainingCreateRequest();
        TrainingResponse expected = GymTestObjects.buildTrainingResponse();

        when(traineeDAO.findById(createRequest.getTraineeId())).thenReturn(Optional.of(trainee));
        when(trainerDAO.findById(createRequest.getTrainerId())).thenReturn(Optional.of(trainer));
        when(trainingMapper.toEntity(createRequest)).thenReturn(training);
        when(trainingDAO.create(any(Training.class))).thenReturn(training);
        when(trainingMapper.toResponse(training)).thenReturn(expected);

        TrainingResponse actual = service.create(createRequest);

        assertNotNull(actual);
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getTraineeUsername(), actual.getTraineeUsername());
        assertEquals(expected.getTrainerUsername(), actual.getTrainerUsername());
        assertEquals(expected.getTrainingName(), actual.getTrainingName());
        assertEquals(expected.getTrainingDate(), actual.getTrainingDate());
        assertEquals(expected.getTrainingDuration(), actual.getTrainingDuration());

        verify(traineeDAO).findById(createRequest.getTraineeId());
        verify(trainerDAO).findById(createRequest.getTrainerId());
        verify(trainingMapper).toEntity(createRequest);
        verify(trainingMapper).toResponse(training);
    }

    @Test
    void create_ShouldThrowExceptionWhenTraineeNotFound() {
        TrainingCreateRequest actual = GymTestObjects.buildTrainingCreateRequest();

        when(traineeDAO.findById(actual.getTraineeId())).thenReturn(Optional.empty());
        when(trainerDAO.findById(actual.getTrainerId())).thenReturn(Optional.of(trainer));

        CoreServiceException exception = assertThrows(CoreServiceException.class, () -> service.create(actual));

        assertEquals("Trainee or/and Trainer was not found", exception.getMessage());

        verify(traineeDAO).findById(actual.getTraineeId());
        verify(trainerDAO).findById(actual.getTrainerId());
        verify(trainingMapper, never()).toEntity(any());
        verify(trainingDAO, never()).create(any());
        verify(trainingMapper, never()).toResponse(any());
    }

    @Test
    void create_ShouldThrowExceptionWhenTrainerNotFound() {
        TrainingCreateRequest actual = GymTestObjects.buildTrainingCreateRequest();

        when(traineeDAO.findById(actual.getTraineeId())).thenReturn(Optional.of(trainee));
        when(trainerDAO.findById(actual.getTrainerId())).thenReturn(Optional.empty());

        CoreServiceException exception = assertThrows(CoreServiceException.class, () -> service.create(actual));

        assertEquals("Trainee or/and Trainer was not found", exception.getMessage());

        verify(traineeDAO).findById(actual.getTraineeId());
        verify(trainerDAO).findById(actual.getTrainerId());
        verify(trainingMapper, never()).toEntity(any());
        verify(trainingDAO, never()).create(any());
        verify(trainingMapper, never()).toResponse(any());
    }

    @Test
    void create_ShouldThrowExceptionWhenBothTraineeAndTrainerNotFound() {
        TrainingCreateRequest createRequest = GymTestObjects.buildTrainingCreateRequest();

        when(traineeDAO.findById(createRequest.getTraineeId())).thenReturn(Optional.empty());
        when(trainerDAO.findById(createRequest.getTrainerId())).thenReturn(Optional.empty());

        CoreServiceException exception = assertThrows(CoreServiceException.class,
                () -> service.create(createRequest));

        assertEquals("Trainee or/and Trainer was not found", exception.getMessage());

        verify(traineeDAO).findById(createRequest.getTraineeId());
        verify(trainerDAO).findById(createRequest.getTrainerId());
        verify(trainingMapper, never()).toEntity(any());
        verify(trainingDAO, never()).create(any());
        verify(trainingMapper, never()).toResponse(any());
    }

    @Test
    void findById_ShouldReturnTrainingWhenExists() {
        TrainingResponse expected = GymTestObjects.buildTrainingResponse();

        when(trainingDAO.findById(TRAINING_ID)).thenReturn(Optional.of(training));
        when(trainingMapper.toResponse(training)).thenReturn(expected);

        Optional<TrainingResponse> actual = service.findById(TRAINING_ID);

        assertTrue(actual.isPresent());
        assertEquals(expected.getId(), actual.get().getId());
        assertEquals(expected.getTraineeUsername(), actual.get().getTraineeUsername());
        assertEquals(expected.getTrainerUsername(), actual.get().getTrainerUsername());
        assertEquals(expected.getTrainingName(), actual.get().getTrainingName());

        verify(trainingDAO).findById(TRAINING_ID);
        verify(trainingMapper).toResponse(training);
    }

    @Test
    void findById_ShouldReturnEmptyWhenNotExists() {
        Long trainingId = 999L;

        when(trainingDAO.findById(trainingId)).thenReturn(Optional.empty());

        Optional<TrainingResponse> result = service.findById(trainingId);

        assertFalse(result.isPresent());

        verify(trainingDAO).findById(trainingId);
        verify(trainingMapper, never()).toResponse(any());
    }

    @Test
    void create_ShouldSetCorrectUserIds() {
        Long traineeUserId = 100L;
        Long trainerUserId = 200L;
        TrainingCreateRequest createRequest = GymTestObjects.buildTrainingCreateRequest();
        TrainingResponse expectedResponse = GymTestObjects.buildTrainingResponse();

        Trainee updatedTrainee = trainee.toBuilder()
                .id(traineeUserId)
                .build();

        Trainer updatedTrainer = trainer.toBuilder()
                .id(trainerUserId)
                .build();

        when(traineeDAO.findById(createRequest.getTraineeId())).thenReturn(Optional.of(updatedTrainee));
        when(trainerDAO.findById(createRequest.getTrainerId())).thenReturn(Optional.of(updatedTrainer));
        when(trainingMapper.toEntity(createRequest)).thenReturn(training);
        when(trainingDAO.create(any(Training.class))).thenReturn(training);
        when(trainingMapper.toResponse(training)).thenReturn(expectedResponse);

        ArgumentCaptor<Training> captor = ArgumentCaptor.forClass(Training.class);

        service.create(createRequest);

        verify(trainingDAO).create(captor.capture());
    }

    @Test
    void getTraineeTrainingsByCriteria_ShouldReturnMappedResponses() {
        String traineeUsername = USERNAME;
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);
        String trainerName = "Mike";
        String trainingType = "Fitness";

        TrainingResponse expected = GymTestObjects.buildTrainingResponse();

        when(traineeDAO.findByUsername(traineeUsername)).thenReturn(Optional.of(trainee));
        when(trainingDAO.findTraineeTrainingsByCriteria(traineeUsername, from, to, trainerName, trainingType))
                .thenReturn(List.of(training));
        when(trainingMapper.toResponse(training)).thenReturn(expected);

        List<TrainingResponse> actual = service.getTraineeTrainingsByCriteria(
                traineeUsername, from, to, trainerName, trainingType
        );

        assertEquals(1, actual.size());
        assertEquals(expected, actual.get(0));

        verify(traineeDAO).findByUsername(traineeUsername);
        verify(trainingDAO).findTraineeTrainingsByCriteria(traineeUsername, from, to, trainerName, trainingType);
        verify(trainingMapper).toResponse(training);
    }

    @Test
    void getTraineeTrainingsByCriteria_ShouldThrow_WhenTraineeNotFound() {
        String traineeUsername = "nonexistent";

        when(traineeDAO.findByUsername(traineeUsername)).thenReturn(Optional.empty());

        CoreServiceException exception = assertThrows(CoreServiceException.class, () ->
                service.getTraineeTrainingsByCriteria(traineeUsername, null, null, null, null));

        assertEquals("Trainee not found with username: nonexistent", exception.getMessage());
        verify(traineeDAO).findByUsername(traineeUsername);
        verifyNoInteractions(trainingDAO);
    }

    @Test
    void getTraineeTrainingsByCriteria_ShouldThrow_WhenUsernameIsBlank() {
        CoreServiceException exception = assertThrows(CoreServiceException.class, () ->
                service.getTraineeTrainingsByCriteria("  ", null, null, null, null));

        assertEquals("Trainee username is required", exception.getMessage());
        verifyNoInteractions(traineeDAO);
    }

    @Test
    void getTrainerTrainingsByCriteria_ShouldReturnMappedResponses() {
        String trainerUsername = TRAINER_USERNAME;
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);
        String traineeName = "John";

        TrainingResponse expected = GymTestObjects.buildTrainingResponse();

        when(trainerDAO.findByUsername(trainerUsername)).thenReturn(Optional.of(trainer));
        when(trainingDAO.findTrainerTrainingsByCriteria(trainerUsername, from, to, traineeName))
                .thenReturn(List.of(training));
        when(trainingMapper.toResponse(training)).thenReturn(expected);

        List<TrainingResponse> actual = service.getTrainerTrainingsByCriteria(
                trainerUsername, from, to, traineeName
        );

        assertEquals(1, actual.size());
        assertEquals(expected, actual.get(0));

        verify(trainerDAO).findByUsername(trainerUsername);
        verify(trainingDAO).findTrainerTrainingsByCriteria(trainerUsername, from, to, traineeName);
        verify(trainingMapper).toResponse(training);
    }

    @Test
    void getTrainerTrainingsByCriteria_ShouldThrow_WhenTrainerNotFound() {
        String trainerUsername = "not.found";

        when(trainerDAO.findByUsername(trainerUsername)).thenReturn(Optional.empty());

        CoreServiceException exception = assertThrows(CoreServiceException.class, () ->
                service.getTrainerTrainingsByCriteria(trainerUsername, null, null, null));

        assertEquals("Trainer not found with username: not.found", exception.getMessage());
        verify(trainerDAO).findByUsername(trainerUsername);
        verifyNoInteractions(trainingDAO);
    }

    @Test
    void getTrainerTrainingsByCriteria_ShouldThrow_WhenUsernameIsBlank() {
        CoreServiceException exception = assertThrows(CoreServiceException.class, () ->
                service.getTrainerTrainingsByCriteria("  ", null, null, null));

        assertEquals("Trainer username is required", exception.getMessage());
        verifyNoInteractions(trainerDAO);
    }

    private Training buildTraining() {
        return Training.builder()
                .id(TRAINEE_ID)
                .id(TRAINER_ID)
                .trainingName(TRAINING_NAME)
                .trainingDate(TRAINING_DATE)
                .trainingDuration(TRAINING_DURATION)
                .trainingType(buildFitnessTrainingType())
                .build();
    }

    private Trainee buildTrainee() {
        User user = User.builder()
                .id(999L)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .username(USERNAME)
                .password("password123")
                .isActive(true)
                .build();

        return Trainee.builder()
                .id(TRAINEE_ID)
                .user(user)
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .address("123 Main St")
                .build();
    }

    private Trainer buildTrainer() {
        User user = User.builder()
                .id(999L)
                .firstName(TRAINER_FIRST_NAME)
                .lastName(TRAINER_LAST_NAME)
                .username(TRAINER_USERNAME)
                .password("password123")
                .isActive(true)
                .build();

        return Trainer.builder()
                .id(TRAINER_ID)
                .user(user)
                .specialization(buildFitnessTrainingType())
                .build();
    }

    private TrainingType buildFitnessTrainingType() {
        return TrainingType.builder()
                .trainingTypeName(FITNESS_TYPE)
                .build();
    }
}