package com.gym.crm.service.impl;

import com.gym.crm.dao.TraineeDAO;
import com.gym.crm.dao.TrainerDAO;
import com.gym.crm.dto.PasswordChangeRequest;
import com.gym.crm.dto.trainer.TrainerCreateRequest;
import com.gym.crm.dto.trainer.TrainerResponse;
import com.gym.crm.dto.trainer.TrainerUpdateRequest;
import com.gym.crm.exception.CoreServiceException;
import com.gym.crm.facade.GymTestObjects;
import com.gym.crm.mapper.TrainerMapper;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.TrainingType;
import com.gym.crm.model.User;
import com.gym.crm.util.UserCredentialsGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.gym.crm.facade.GymTestObjects.buildTrainerResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerServiceImplTest {
    private static final String TRAINER_FIRST_NAME = "Mike";
    private static final String TRAINER_LAST_NAME = "Johnson";
    private static final String TRAINER_USERNAME = "mike.johnson";
    private static final String PASSWORD = "password123";
    private static final String FITNESS_TYPE = "Fitness";
    private static final String YOGA_TYPE = "YOGA";
    private static final Long TRAINER_ID = 1L;
    private static final String GENERATED_PASSWORD = "generatedPassword";

    private final Trainer trainer = buildTrainer();

    @Mock
    private TrainerDAO trainerDAO;
    @Mock
    private TraineeDAO traineeDAO;
    @Mock
    private UserCredentialsGenerator userCredentialsGenerator;
    @Mock
    private TrainerMapper trainerMapper;
    @InjectMocks
    private TrainerServiceImpl service;

    @Test
    void create_ShouldCreateTrainerSuccessfully() {
        TrainerCreateRequest createRequest = GymTestObjects.buildTrainerCreateRequest();
        Trainer initialTrainer = buildTrainer();
        List<Trainer> existingTrainers = List.of(
                createTrainerWithUsername("existing.trainer1"),
                createTrainerWithUsername("existing.trainer2")
        );
        List<String> existingUsernames = List.of("existing.trainer1", "existing.trainer2");

        User userWithCredentials = initialTrainer.getUser().toBuilder()
                .username(TRAINER_USERNAME)
                .password(GENERATED_PASSWORD)
                .build();

        Trainer trainerWithCredentials = initialTrainer.toBuilder()
                .user(userWithCredentials)
                .build();

        TrainerResponse expected = buildTrainerResponse();

        when(trainerMapper.toEntity(createRequest)).thenReturn(initialTrainer);
        when(trainerDAO.findAll()).thenReturn(existingTrainers);
        when(userCredentialsGenerator.generateUsername(TRAINER_FIRST_NAME, TRAINER_LAST_NAME, existingUsernames))
                .thenReturn(TRAINER_USERNAME);
        when(userCredentialsGenerator.generatePassword()).thenReturn(GENERATED_PASSWORD);
        when(trainerDAO.create(any(Trainer.class))).thenReturn(trainerWithCredentials);
        when(trainerMapper.toResponse(trainerWithCredentials)).thenReturn(expected);

        TrainerResponse actual = service.create(createRequest);

        assertNotNull(actual);
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getUsername(), actual.getUsername());
        assertEquals(expected.getSpecialization(), actual.getSpecialization());

        verify(trainerMapper).toEntity(createRequest);
        verify(trainerDAO).findAll();
        verify(userCredentialsGenerator).generateUsername(TRAINER_FIRST_NAME, TRAINER_LAST_NAME, existingUsernames);
        verify(userCredentialsGenerator).generatePassword();
        verify(trainerDAO).create(any(Trainer.class));
        verify(trainerMapper).toResponse(trainerWithCredentials);
    }

    @Test
    void create_ShouldHandleEmptyExistingUsernames() {
        TrainerCreateRequest createRequest = GymTestObjects.buildTrainerCreateRequest();
        List<Trainer> existingTrainers = List.of();
        List<String> existingUsernames = List.of();
        TrainerResponse expectedResponse = buildTrainerResponse();

        when(trainerMapper.toEntity(createRequest)).thenReturn(trainer);
        when(trainerDAO.findAll()).thenReturn(existingTrainers);
        when(userCredentialsGenerator.generateUsername(TRAINER_FIRST_NAME, TRAINER_LAST_NAME, existingUsernames))
                .thenReturn(TRAINER_USERNAME);
        when(userCredentialsGenerator.generatePassword()).thenReturn(GENERATED_PASSWORD);
        when(trainerDAO.create(any(Trainer.class))).thenReturn(trainer);
        when(trainerMapper.toResponse(trainer)).thenReturn(expectedResponse);

        TrainerResponse actual = service.create(createRequest);

        assertNotNull(actual);
        verify(trainerDAO).findAll();
        verify(userCredentialsGenerator).generateUsername(TRAINER_FIRST_NAME, TRAINER_LAST_NAME, existingUsernames);
    }

    @Test
    void findById_ShouldReturnTrainerWhenExists() {
        TrainerResponse expected = buildTrainerResponse();

        when(trainerDAO.findById(TRAINER_ID)).thenReturn(Optional.of(trainer));
        when(trainerMapper.toResponse(trainer)).thenReturn(expected);

        Optional<TrainerResponse> actual = service.findById(TRAINER_ID);

        assertTrue(actual.isPresent());
        assertEquals(expected.getId(), actual.get().getId());
        assertEquals(expected.getUsername(), actual.get().getUsername());
        assertEquals(expected.getSpecialization(), actual.get().getSpecialization());

        verify(trainerDAO).findById(TRAINER_ID);
        verify(trainerMapper).toResponse(trainer);
    }

    @Test
    void findById_ShouldReturnEmptyWhenNotExists() {
        Long trainerId = 999L;

        when(trainerDAO.findById(trainerId)).thenReturn(Optional.empty());

        Optional<TrainerResponse> result = service.findById(trainerId);

        assertFalse(result.isPresent());

        verify(trainerDAO).findById(trainerId);
        verify(trainerMapper, never()).toResponse(any());
    }

    @Test
    void findByUsername_ShouldReturnTrainerWhenExists() {
        Trainer buildTrainer = buildTrainer();
        TrainerResponse expected = buildTrainerResponse();

        when(trainerDAO.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.of(buildTrainer));
        when(trainerMapper.toResponse(buildTrainer)).thenReturn(expected);

        Optional<TrainerResponse> actual = service.findByUsername(TRAINER_USERNAME);

        assertTrue(actual.isPresent());
        assertEquals(expected.getId(), actual.get().getId());
        assertEquals(expected.getUsername(), actual.get().getUsername());

        verify(trainerDAO).findByUsername(TRAINER_USERNAME);
        verify(trainerMapper).toResponse(buildTrainer);
    }

    @Test
    void findByUsername_ShouldReturnEmptyWhenNotExists() {
        when(trainerDAO.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.empty());

        Optional<TrainerResponse> actual = service.findByUsername(TRAINER_USERNAME);

        assertFalse(actual.isPresent());
        verify(trainerDAO).findByUsername(TRAINER_USERNAME);
        verify(trainerMapper, never()).toResponse(any());
    }

    @Test
    void update_ShouldUpdateTrainerSuccessfully() {
        TrainerUpdateRequest updateRequest = GymTestObjects.buildTrainerUpdateRequest();
        Trainer originalTrainer = buildTrainer();
        Trainer updatedTrainer = buildUpdatedTrainer();
        TrainerResponse expected = buildUpdatedResponse();

        when(trainerDAO.findById(updateRequest.getId())).thenReturn(Optional.of(originalTrainer));
        when(trainerDAO.update(any(Trainer.class))).thenReturn(updatedTrainer);
        when(trainerMapper.toResponse(updatedTrainer)).thenReturn(expected);

        TrainerResponse actual = service.update(updateRequest);

        assertNotNull(actual);
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getFirstName(), actual.getFirstName());
        assertEquals(expected.getLastName(), actual.getLastName());
        assertEquals(expected.isActive(), actual.isActive());
        assertEquals(expected.getSpecialization(), actual.getSpecialization());

        verify(trainerDAO).findById(updateRequest.getId());
        verify(trainerDAO).update(any(Trainer.class));
        verify(trainerMapper).toResponse(updatedTrainer);
    }

    @Test
    void update_ShouldThrowExceptionWhenTrainerNotFound() {
        TrainerUpdateRequest updateRequest = GymTestObjects.buildTrainerUpdateRequest();

        when(trainerDAO.findById(updateRequest.getId())).thenReturn(Optional.empty());

        CoreServiceException exception = assertThrows(CoreServiceException.class, () -> service.update(updateRequest));

        assertEquals("Trainer not found with id: " + updateRequest.getId(), exception.getMessage());

        verify(trainerDAO).findById(updateRequest.getId());
        verify(trainerDAO, never()).update(any());
        verify(trainerMapper, never()).toResponse(any());
    }

    @Test
    void changePassword_ShouldUpdatePasswordWhenOldPasswordMatches() {
        PasswordChangeRequest request = new PasswordChangeRequest();
        request.setUsername(TRAINER_USERNAME);
        request.setOldPassword(PASSWORD);
        request.setNewPassword("newSecurePassword");

        ArgumentCaptor<Trainer> captor = ArgumentCaptor.forClass(Trainer.class);
        when(trainerDAO.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.of(trainer));

        service.changePassword(request);

        verify(trainerDAO).update(captor.capture());

        Trainer updated = captor.getValue();
        assertEquals("newSecurePassword", updated.getUser().getPassword());
        verify(trainerDAO).findByUsername(TRAINER_USERNAME);
        verify(trainerDAO).update(any(Trainer.class));
    }

    @Test
    void toggleTrainerActivation_ShouldToggleFromActiveToInactive() {
        Trainer activeTrainer = buildTrainer();
        User inactiveUser = activeTrainer.getUser().toBuilder()
                .isActive(false)
                .build();
        Trainer inactiveTrainer = activeTrainer.toBuilder()
                .user(inactiveUser)
                .build();
        TrainerResponse expected = buildTrainerResponse();
        expected.setActive(false);

        ArgumentCaptor<Trainer> captor = ArgumentCaptor.forClass(Trainer.class);
        when(trainerDAO.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.of(activeTrainer));
        when(trainerDAO.update(any(Trainer.class))).thenReturn(inactiveTrainer);
        when(trainerMapper.toResponse(inactiveTrainer)).thenReturn(expected);

        TrainerResponse actual = service.toggleTrainerActivation(TRAINER_USERNAME);

        assertNotNull(actual);
        assertFalse(actual.isActive());

        verify(trainerDAO).findByUsername(TRAINER_USERNAME);
        verify(trainerDAO).update(any(Trainer.class));
        verify(trainerMapper).toResponse(inactiveTrainer);

        verify(trainerDAO).update(captor.capture());
        Trainer captured = captor.getValue();
        assertFalse(captured.getUser().getIsActive());
    }

    @Test
    void toggleTrainerActivation_ShouldToggleFromInactiveToActive() {
        User inactiveUser = buildTrainer().getUser().toBuilder()
                .isActive(false)
                .build();
        Trainer inactiveTrainer = buildTrainer().toBuilder()
                .user(inactiveUser)
                .build();

        User activeUser = inactiveUser.toBuilder()
                .isActive(true)
                .build();
        Trainer activeTrainer = inactiveTrainer.toBuilder()
                .user(activeUser)
                .build();

        TrainerResponse expected = buildTrainerResponse();
        expected.setActive(true);

        when(trainerDAO.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.of(inactiveTrainer));
        when(trainerDAO.update(any(Trainer.class))).thenReturn(activeTrainer);
        when(trainerMapper.toResponse(activeTrainer)).thenReturn(expected);

        TrainerResponse actual = service.toggleTrainerActivation(TRAINER_USERNAME);

        assertNotNull(actual);
        assertTrue(actual.isActive());

        verify(trainerDAO).findByUsername(TRAINER_USERNAME);
        verify(trainerDAO).update(any(Trainer.class));
        verify(trainerMapper).toResponse(activeTrainer);

        ArgumentCaptor<Trainer> captor = ArgumentCaptor.forClass(Trainer.class);
        verify(trainerDAO).update(captor.capture());
        Trainer captured = captor.getValue();
        assertTrue(captured.getUser().getIsActive());
    }

    @Test
    void findTrainersNotAssignedToTrainee_ShouldReturnListOfTrainers() {
        String traineeUsername = "trainee.user";
        Trainee trainee = buildTraineeWithUsername(traineeUsername);

        List<Trainer> unassignedTrainers = List.of(
                createTrainerWithUsername("trainer1"),
                createTrainerWithUsername("trainer2"),
                createTrainerWithUsername("trainer3")
        );

        List<TrainerResponse> expectedResponses = List.of(
                createTrainerResponse("trainer1", 1L),
                createTrainerResponse("trainer2", 2L),
                createTrainerResponse("trainer3", 3L)
        );

        when(traineeDAO.findByUsername(traineeUsername)).thenReturn(Optional.of(trainee));
        when(trainerDAO.findTrainersNotAssignedToTrainee(traineeUsername)).thenReturn(unassignedTrainers);
        when(trainerMapper.toResponse(unassignedTrainers.get(0))).thenReturn(expectedResponses.get(0));
        when(trainerMapper.toResponse(unassignedTrainers.get(1))).thenReturn(expectedResponses.get(1));
        when(trainerMapper.toResponse(unassignedTrainers.get(2))).thenReturn(expectedResponses.get(2));

        List<TrainerResponse> actual = service.findTrainersNotAssignedToTrainee(traineeUsername);

        assertNotNull(actual);
        assertEquals(3, actual.size());
        assertEquals("trainer1", actual.get(0).getUsername());
        assertEquals("trainer2", actual.get(1).getUsername());
        assertEquals("trainer3", actual.get(2).getUsername());

        verify(traineeDAO).findByUsername(traineeUsername);
        verify(trainerDAO).findTrainersNotAssignedToTrainee(traineeUsername);
        verify(trainerMapper).toResponse(unassignedTrainers.get(0));
        verify(trainerMapper).toResponse(unassignedTrainers.get(1));
        verify(trainerMapper).toResponse(unassignedTrainers.get(2));
    }

    private Trainer buildTrainer() {
        User user = User.builder()
                .id(999L)
                .firstName(TRAINER_FIRST_NAME)
                .lastName(TRAINER_LAST_NAME)
                .username(TRAINER_USERNAME)
                .password(PASSWORD)
                .isActive(true)
                .build();

        return Trainer.builder()
                .id(TRAINER_ID)
                .user(user)
                .specialization(TrainingType.builder().trainingTypeName(FITNESS_TYPE).build())
                .build();
    }

    private Trainer buildUpdatedTrainer() {
        User user = buildTrainer().getUser().toBuilder()
                .firstName("Michael")
                .lastName("Smith")
                .isActive(false)
                .build();

        return buildTrainer().toBuilder()
                .user(user)
                .specialization(TrainingType.builder().trainingTypeName(YOGA_TYPE).build())
                .build();
    }

    private TrainerResponse buildUpdatedResponse() {
        TrainerResponse response = new TrainerResponse();
        response.setId(TRAINER_ID);
        response.setFirstName("Michael");
        response.setLastName("Smith");
        response.setActive(false);
        response.setSpecialization(TrainingType.builder().trainingTypeName(YOGA_TYPE).build());

        return response;
    }

    private Trainer createTrainerWithUsername(String username) {
        User user = User.builder()
                .username(username)
                .build();

        return Trainer.builder()
                .user(user)
                .build();
    }

    private Trainee buildTraineeWithUsername(String username) {
        User user = User.builder()
                .username(username)
                .firstName("John")
                .lastName("Doe")
                .build();

        return Trainee.builder()
                .user(user)
                .build();
    }

    private TrainerResponse createTrainerResponse(String username, Long id) {
        TrainerResponse response = new TrainerResponse();
        response.setId(id);
        response.setUsername(username);
        response.setFirstName("Trainer");
        response.setLastName("Name");
        response.setActive(true);
        response.setSpecialization(TrainingType.builder().trainingTypeName(FITNESS_TYPE).build());
        return response;
    }
}
