package com.gym.crm.service.impl;

import com.gym.crm.dao.TraineeDAO;
import com.gym.crm.dto.trainee.TraineeCreateRequest;
import com.gym.crm.dto.trainee.TraineeResponse;
import com.gym.crm.dto.trainee.TraineeUpdateRequest;
import com.gym.crm.exception.CoreServiceException;
import com.gym.crm.facade.GymTestObjects;
import com.gym.crm.mapper.TraineeMapper;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.User;
import com.gym.crm.util.UserCredentialsGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.gym.crm.facade.GymTestObjects.buildTraineeResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraineeServiceImplTest {
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String USERNAME = "john.doe";
    private static final String PASSWORD = "password123";
    private static final LocalDate BIRTH_DATE = LocalDate.of(1990, 1, 1);
    private static final String ADDRESS = "123 Main St";
    private static final Long TRAINEE_ID = 1L;
    private static final String GENERATED_PASSWORD = "generatedPassword";

    private final Trainee trainee = buildTrainee();

    @Mock
    private TraineeDAO traineeDAO;
    @Mock
    private UserCredentialsGenerator userCredentialsGenerator;
    @Mock
    private TraineeMapper traineeMapper;
    @InjectMocks
    private TraineeServiceImpl service;

    @Test
    void create_ShouldCreateTraineeSuccessfully() {
        TraineeCreateRequest createRequest = GymTestObjects.buildTraineeCreateRequest();
        List<Trainee> existingTrainees = List.of(
                createTraineeWithUsername("existing.user1"),
                createTraineeWithUsername("existing.user2")
        );
        List<String> existingUsernames = List.of("existing.user1", "existing.user2");
        TraineeResponse expected = buildTraineeResponse();

        when(traineeMapper.toEntity(createRequest)).thenReturn(trainee);
        when(traineeDAO.findAll()).thenReturn(existingTrainees);
        when(userCredentialsGenerator.generateUsername(FIRST_NAME, LAST_NAME, existingUsernames))
                .thenReturn(USERNAME);
        when(userCredentialsGenerator.generatePassword()).thenReturn(GENERATED_PASSWORD);
        when(traineeDAO.create(any(Trainee.class))).thenReturn(trainee);
        when(traineeMapper.toResponse(trainee)).thenReturn(expected);

        TraineeResponse actual = service.create(createRequest);

        assertNotNull(actual);
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getUsername(), actual.getUsername());

        verify(traineeMapper).toEntity(createRequest);
        verify(traineeDAO).findAll();
        verify(userCredentialsGenerator).generateUsername(FIRST_NAME, LAST_NAME, existingUsernames);
        verify(userCredentialsGenerator).generatePassword();
        verify(traineeDAO).create(any(Trainee.class));
        verify(traineeMapper).toResponse(any(Trainee.class));

        ArgumentCaptor<Trainee> captor = ArgumentCaptor.forClass(Trainee.class);
        verify(traineeDAO).create(captor.capture());

        Trainee captured = captor.getValue();
        assertEquals(GENERATED_PASSWORD, captured.getUser().getPassword());
        assertEquals(USERNAME, captured.getUser().getUsername());
    }

    @Test
    void findById_ShouldReturnTraineeWhenExists() {
        TraineeResponse expected = GymTestObjects.buildTraineeResponse();

        when(traineeDAO.findById(TRAINEE_ID)).thenReturn(Optional.of(trainee));
        when(traineeMapper.toResponse(trainee)).thenReturn(expected);

        Optional<TraineeResponse> actual = service.findById(TRAINEE_ID);

        assertTrue(actual.isPresent());
        assertEquals(expected.getId(), actual.get().getId());
        assertEquals(expected.getUsername(), actual.get().getUsername());

        verify(traineeDAO).findById(TRAINEE_ID);
        verify(traineeMapper).toResponse(trainee);
    }

    @Test
    void findById_ShouldReturnEmptyWhenNotExists() {
        Long traineeId = 999L;
        when(traineeDAO.findById(traineeId)).thenReturn(Optional.empty());

        Optional<TraineeResponse> actual = service.findById(traineeId);

        assertFalse(actual.isPresent());

        verify(traineeDAO).findById(traineeId);
        verify(traineeMapper, never()).toResponse(any());
    }

    @Test
    void update_ShouldUpdateTraineeSuccessfully() {
        TraineeUpdateRequest updateRequest = GymTestObjects.buildTraineeUpdateRequest();
        Trainee updatedTrainee = buildUpdatedTrainee();
        TraineeResponse expected = buildUpdatedResponse();

        when(traineeDAO.findById(updateRequest.getId())).thenReturn(Optional.of(trainee));
        when(traineeDAO.update(any(Trainee.class))).thenReturn(updatedTrainee);
        when(traineeMapper.toResponse(any(Trainee.class))).thenReturn(expected);

        TraineeResponse actual = service.update(updateRequest);

        assertNotNull(actual);
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getFirstName(), actual.getFirstName());
        assertEquals(expected.getLastName(), actual.getLastName());
        assertEquals(expected.isActive(), actual.isActive());

        verify(traineeDAO).findById(updateRequest.getId());
        verify(traineeDAO).update(any(Trainee.class));
        verify(traineeMapper).toResponse(updatedTrainee);

        ArgumentCaptor<Trainee> captor = ArgumentCaptor.forClass(Trainee.class);
        verify(traineeDAO).update(captor.capture());

        Trainee captured = captor.getValue();
        assertEquals("Jane", captured.getUser().getFirstName());
        assertEquals("Smith", captured.getUser().getLastName());
        assertFalse(captured.getUser().getIsActive());
        assertEquals(LocalDate.of(1985, 5, 15), captured.getDateOfBirth());
        assertEquals("456 Oak Ave", captured.getAddress());
    }

    @Test
    void update_ShouldThrowExceptionWhenTraineeNotFound() {
        TraineeUpdateRequest updateRequest = GymTestObjects.buildTraineeUpdateRequest();

        when(traineeDAO.findById(updateRequest.getId())).thenReturn(Optional.empty());

        CoreServiceException exception = assertThrows(CoreServiceException.class, () -> service.update(updateRequest));

        assertEquals("Trainee not found with id: " + updateRequest.getId(), exception.getMessage());

        verify(traineeDAO).findById(updateRequest.getId());
        verify(traineeDAO, never()).update(any());
        verify(traineeMapper, never()).toResponse(any());
    }

    @Test
    void delete_ShouldCallDAODelete() {
        service.delete(TRAINEE_ID);

        verify(traineeDAO).delete(TRAINEE_ID);
    }

    private Trainee buildTrainee() {
        User user = User.builder()
                .id(999L)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .username(USERNAME)
                .password(PASSWORD)
                .isActive(true)
                .build();

        return Trainee.builder()
                .id(TRAINEE_ID)
                .user(user)
                .dateOfBirth(BIRTH_DATE)
                .address(ADDRESS)
                .build();
    }

    private Trainee buildUpdatedTrainee() {
        User user = buildTrainee().getUser().toBuilder()
                .firstName("Jane")
                .lastName("Smith")
                .isActive(false)
                .build();

        return buildTrainee().toBuilder()
                .user(user)
                .dateOfBirth(LocalDate.of(1985, 5, 15))
                .address("456 Oak Ave")
                .build();
    }

    private TraineeResponse buildUpdatedResponse() {
        TraineeResponse response = new TraineeResponse();
        response.setId(TRAINEE_ID);
        response.setFirstName("Jane");
        response.setLastName("Smith");
        response.setActive(false);

        return response;
    }

    private Trainee createTraineeWithUsername(String username) {
        User user = User.builder()
                .username(username)
                .build();

        return Trainee.builder()
                .user(user)
                .build();
    }
}
