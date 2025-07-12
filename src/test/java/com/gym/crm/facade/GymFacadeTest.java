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
import com.gym.crm.model.TrainingType;
import com.gym.crm.service.AuthenticationService;
import com.gym.crm.service.TraineeService;
import com.gym.crm.service.TrainerService;
import com.gym.crm.service.TrainingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.gym.crm.facade.GymTestObjects.FIRST_NAME;
import static com.gym.crm.facade.GymTestObjects.FITNESS_TYPE;
import static com.gym.crm.facade.GymTestObjects.LAST_NAME;
import static com.gym.crm.facade.GymTestObjects.PASSWORD;
import static com.gym.crm.facade.GymTestObjects.TRAINEE_ID;
import static com.gym.crm.facade.GymTestObjects.TRAINER_FIRST_NAME;
import static com.gym.crm.facade.GymTestObjects.TRAINER_ID;
import static com.gym.crm.facade.GymTestObjects.TRAINER_LAST_NAME;
import static com.gym.crm.facade.GymTestObjects.TRAINER_USERNAME;
import static com.gym.crm.facade.GymTestObjects.TRAINING_DATE;
import static com.gym.crm.facade.GymTestObjects.TRAINING_DURATION;
import static com.gym.crm.facade.GymTestObjects.TRAINING_ID;
import static com.gym.crm.facade.GymTestObjects.TRAINING_NAME;
import static com.gym.crm.facade.GymTestObjects.USERNAME;
import static com.gym.crm.facade.GymTestObjects.YOGA_TYPE;
import static com.gym.crm.facade.GymTestObjects.buildPasswordChangeRequest;
import static com.gym.crm.facade.GymTestObjects.buildTraineeCreateRequest;
import static com.gym.crm.facade.GymTestObjects.buildTraineeResponse;
import static com.gym.crm.facade.GymTestObjects.buildTraineeUpdateRequest;
import static com.gym.crm.facade.GymTestObjects.buildTrainerCreateRequest;
import static com.gym.crm.facade.GymTestObjects.buildTrainerResponse;
import static com.gym.crm.facade.GymTestObjects.buildTrainerUpdateRequest;
import static com.gym.crm.facade.GymTestObjects.buildTrainingCreateRequest;
import static com.gym.crm.facade.GymTestObjects.buildTrainingResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GymFacadeTest {
    @Mock
    private TraineeService traineeService;
    @Mock
    private TrainerService trainerService;
    @Mock
    private TrainingService trainingService;
    @Mock
    private AuthenticationService authenticationService;
    @InjectMocks
    private GymFacade facade;

    @Test
    void createTrainee_ShouldCallServiceAndReturnResponse() {
        TraineeCreateRequest request = buildTraineeCreateRequest();
        TraineeResponse expectedResponse = buildTraineeResponse();

        when(traineeService.create(request)).thenReturn(expectedResponse);

        TraineeResponse actual = facade.createTrainee(request);

        assertNotNull(actual);
        assertEquals(TRAINEE_ID, actual.getId());
        assertEquals(FIRST_NAME, actual.getFirstName());
        assertEquals(LAST_NAME, actual.getLastName());
        assertEquals(USERNAME, actual.getUsername());
        verify(traineeService).create(request);
    }

    @Test
    void getTraineeByUsername_ShouldCallServiceAndReturnResponse() {
        TraineeResponse expectedResponse = buildTraineeResponse();

        when(traineeService.findByUsername(USERNAME)).thenReturn(Optional.of(expectedResponse));

        Optional<TraineeResponse> actual = facade.getTraineeByUsername(USERNAME, USERNAME, PASSWORD);

        assertTrue(actual.isPresent());
        assertEquals(TRAINEE_ID, actual.get().getId());
        assertEquals(USERNAME, actual.get().getUsername());
        verify(traineeService).findByUsername(USERNAME);
        verify(authenticationService).validateCredentials(USERNAME, PASSWORD);
    }

    @Test
    void getTraineeByUsername_ShouldReturnEmptyWhenNotFound() {
        when(traineeService.findByUsername(USERNAME)).thenReturn(Optional.empty());

        Optional<TraineeResponse> actual = facade.getTraineeByUsername(USERNAME, USERNAME, PASSWORD);

        assertFalse(actual.isPresent());
        verify(traineeService).findByUsername(USERNAME);
        verify(authenticationService).validateCredentials(USERNAME, PASSWORD);
    }

    @Test
    void updateTrainee_ShouldCallServiceAndReturnResponse() {
        TraineeUpdateRequest expected = buildTraineeUpdateRequest();
        TraineeResponse updatedResponse = buildUpdatedTraineeResponse();

        when(traineeService.update(expected)).thenReturn(updatedResponse);

        TraineeResponse actual = facade.updateTrainee(expected, USERNAME, PASSWORD);

        assertNotNull(actual);
        assertEquals(TRAINEE_ID, actual.getId());
        assertEquals(expected.getFirstName(), actual.getFirstName());
        assertEquals(expected.getLastName(), actual.getLastName());
        verify(traineeService).update(expected);
        verify(authenticationService).validateTraineeCredentials(USERNAME, PASSWORD);
    }

    @Test
    void deleteTrainee_ShouldCallService() {
        facade.deleteTrainee(USERNAME, USERNAME, PASSWORD);

        verify(authenticationService).validateTraineeCredentials(USERNAME, PASSWORD);
    }

    @Test
    void changeTraineePassword_ShouldDelegateToServiceAndReturnTrue() {
        PasswordChangeRequest request = buildPasswordChangeRequest();

        facade.changeTraineePassword(request);

        verify(traineeService).changePassword(request);
    }

    @Test
    void createTrainer_ShouldCallServiceAndReturnResponse() {
        TrainerCreateRequest request = buildTrainerCreateRequest();
        TrainerResponse expectedResponse = buildTrainerResponse();

        when(trainerService.create(request)).thenReturn(expectedResponse);

        TrainerResponse actual = facade.createTrainer(request);

        assertNotNull(actual);
        assertEquals(TRAINER_ID, actual.getId());
        assertEquals(TRAINER_FIRST_NAME, actual.getFirstName());
        assertEquals(TRAINER_LAST_NAME, actual.getLastName());
        assertEquals(TRAINER_USERNAME, actual.getUsername());
        assertEquals(FITNESS_TYPE, actual.getSpecialization().getTrainingTypeName());
        verify(trainerService).create(request);
    }

    @Test
    void getTrainerByUsername_ShouldCallServiceAndReturnResponse() {
        TrainerResponse expectedResponse = buildTrainerResponse();

        when(trainerService.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.of(expectedResponse));

        Optional<TrainerResponse> actual = facade.getTrainerByUsername(TRAINER_USERNAME, TRAINER_USERNAME, PASSWORD);

        assertTrue(actual.isPresent());
        assertEquals(TRAINER_ID, actual.get().getId());
        assertEquals(TRAINER_USERNAME, actual.get().getUsername());
        verify(trainerService).findByUsername(TRAINER_USERNAME);
        verify(authenticationService).validateCredentials(TRAINER_USERNAME, PASSWORD);
    }

    @Test
    void getTrainerByUsername_ShouldReturnEmptyWhenNotFound() {
        when(trainerService.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.empty());

        Optional<TrainerResponse> actual = facade.getTrainerByUsername(TRAINER_USERNAME, TRAINER_USERNAME, PASSWORD);

        assertFalse(actual.isPresent());
        verify(trainerService).findByUsername(TRAINER_USERNAME);
        verify(authenticationService).validateCredentials(TRAINER_USERNAME, PASSWORD);

    }

    @Test
    void updateTrainer_ShouldCallServiceAndReturnResponse() {
        TrainerUpdateRequest expected = buildTrainerUpdateRequest();
        TrainerResponse updatedResponse = buildUpdatedTrainerResponse();

        when(trainerService.update(expected)).thenReturn(updatedResponse);

        TrainerResponse actual = facade.updateTrainer(expected, PASSWORD);

        assertNotNull(actual);
        assertEquals(TRAINER_ID, actual.getId());
        assertEquals(expected.getFirstName(), actual.getFirstName());
        assertEquals(expected.getLastName(), actual.getLastName());
        assertEquals(YOGA_TYPE, actual.getSpecialization().getTrainingTypeName());
        verify(trainerService).update(expected);
        verify(authenticationService).validateTrainerCredentials(expected.getUsername(), PASSWORD);
    }

    @Test
    void changeTrainerPassword_ShouldDelegateToServiceAndReturnTrue() {
        PasswordChangeRequest request = buildPasswordChangeRequest();

        facade.changeTrainerPassword(request);

        verify(trainerService).changePassword(request);
    }

    @Test
    void createTraining_ShouldCallServiceAndReturnResponse() {
        TrainingCreateRequest request = buildTrainingCreateRequest();
        TrainingResponse expectedResponse = buildTrainingResponse();

        when(trainingService.create(request)).thenReturn(expectedResponse);

        TrainingResponse actual = facade.createTraining(request, USERNAME, PASSWORD);

        assertNotNull(actual);
        assertEquals(TRAINING_ID, actual.getId());
        assertEquals(USERNAME, actual.getTraineeUsername());
        assertEquals(TRAINER_USERNAME, actual.getTrainerUsername());
        assertEquals(TRAINING_NAME, actual.getTrainingName());
        assertEquals(TRAINING_DATE, actual.getTrainingDate());
        assertEquals(TRAINING_DURATION, actual.getTrainingDuration());
        verify(trainingService).create(request);
        verify(authenticationService).validateCredentials(USERNAME, PASSWORD);
    }

    @Test
    void getTrainingById_ShouldCallServiceAndReturnResponse() {
        TrainingResponse expectedResponse = buildTrainingResponse();

        when(trainingService.findById(TRAINING_ID)).thenReturn(Optional.of(expectedResponse));

        Optional<TrainingResponse> actual = facade.getTrainingById(TRAINING_ID, USERNAME, PASSWORD);

        assertTrue(actual.isPresent());
        assertEquals(TRAINING_ID, actual.get().getId());
        assertEquals(USERNAME, actual.get().getTraineeUsername());
        assertEquals(TRAINER_USERNAME, actual.get().getTrainerUsername());
        assertEquals(TRAINING_NAME, actual.get().getTrainingName());
        verify(trainingService).findById(TRAINING_ID);
        verify(authenticationService).validateCredentials(USERNAME, PASSWORD);
    }

    @Test
    void getTrainingById_ShouldReturnEmptyWhenNotFound() {
        Long nonExistentId = 999L;

        when(trainingService.findById(nonExistentId)).thenReturn(Optional.empty());

        Optional<TrainingResponse> actual = facade.getTrainingById(nonExistentId, USERNAME, PASSWORD);

        assertFalse(actual.isPresent());
        verify(trainingService).findById(nonExistentId);
    }

    @Test
    void constructor_ShouldInitializeServices() {
        TraineeService mockTraineeService = mock(TraineeService.class);
        TrainerService mockTrainerService = mock(TrainerService.class);
        TrainingService mockTrainingService = mock(TrainingService.class);
        AuthenticationService mockAuthenticationService = mock(AuthenticationService.class);

        when(mockTraineeService.findByUsername(USERNAME)).thenReturn(Optional.of(buildTraineeResponse()));

        GymFacade gymFacade = new GymFacade(mockTraineeService, mockTrainerService, mockTrainingService, mockAuthenticationService);

        assertNotNull(gymFacade);

        Optional<TraineeResponse> actual = gymFacade.getTraineeByUsername(USERNAME, USERNAME, PASSWORD);

        assertTrue(actual.isPresent());
        verify(mockAuthenticationService).validateCredentials(USERNAME, PASSWORD);
    }

    private TraineeResponse buildUpdatedTraineeResponse() {
        TraineeResponse response = new TraineeResponse();
        response.setId(TRAINEE_ID);
        response.setFirstName("Jane");
        response.setLastName("Smith");

        return response;
    }

    private TrainerResponse buildUpdatedTrainerResponse() {
        TrainerResponse response = new TrainerResponse();
        response.setId(TRAINER_ID);
        response.setFirstName("Michael");
        response.setLastName("Smith");
        response.setSpecialization(TrainingType.builder().trainingTypeName(YOGA_TYPE).build());

        return response;
    }
}