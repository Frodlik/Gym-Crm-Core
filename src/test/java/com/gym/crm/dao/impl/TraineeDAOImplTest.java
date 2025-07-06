package com.gym.crm.dao.impl;

import com.gym.crm.dao.hibernate.TransactionHandler;
import com.gym.crm.exception.DaoException;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraineeDAOImplTest {
    private static final Long TRAINEE_ID = 1L;
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String USERNAME = "john.doe";
    private static final String PASSWORD = "password123";
    private static final LocalDate DATE_OF_BIRTH = LocalDate.of(1990, 1, 1);
    private static final String ADDRESS = "123 Main St";

    @Mock
    private SessionFactory sessionFactory;
    @Mock
    private Session session;
    @Mock
    private Transaction transaction;
    @Mock
    private Query<Trainee> query;
    @InjectMocks
    private TraineeDAOImpl dao;

    @Test
    void testCreate_ShouldCreateTrainee() {
        Trainee trainee = createTraineeWithoutId(FIRST_NAME, LAST_NAME, USERNAME,
                DATE_OF_BIRTH, ADDRESS, true);

        try (MockedStatic<TransactionHandler> mockedStatic = mockStatic(TransactionHandler.class)) {
            mockedStatic.when(() -> TransactionHandler.performReturningWithinSession(any(Function.class)))
                    .thenAnswer(invocation -> {
                        Function<Session, Trainee> function = invocation.getArgument(0);
                        return function.apply(session);
                    });

            Trainee result = dao.create(trainee);

            assertEquals(trainee, result);
            verify(session).persist(trainee);
        }
    }

    @Test
    void testCreate_ShouldCreateTraineeWithNullAddress() {
        Trainee trainee = createTraineeWithoutId(FIRST_NAME, LAST_NAME, USERNAME,
                DATE_OF_BIRTH, null, false);

        try (MockedStatic<TransactionHandler> mockedStatic = mockStatic(TransactionHandler.class)) {
            mockedStatic.when(() -> TransactionHandler.performReturningWithinSession(any(Function.class)))
                    .thenAnswer(invocation -> {
                        Function<Session, Trainee> function = invocation.getArgument(0);
                        return function.apply(session);
                    });

            Trainee result = dao.create(trainee);

            assertEquals(trainee, result);
            verify(session).persist(trainee);
            assertNull(trainee.getAddress());
            assertFalse(trainee.getUser().getIsActive());
        }
    }

    @Test
    void testFindById_ShouldReturnTraineeWhenExists() {
        Trainee expected = createSampleTrainee(TRAINEE_ID);

        try (MockedStatic<TransactionHandler> mockedStatic = mockStatic(TransactionHandler.class)) {
            mockedStatic.when(() -> TransactionHandler.performReturningWithinSession(any(Function.class)))
                    .thenAnswer(invocation -> {
                        Function<Session, Optional<Trainee>> function = invocation.getArgument(0);
                        when(session.find(Trainee.class, TRAINEE_ID)).thenReturn(expected);
                        return function.apply(session);
                    });

            Optional<Trainee> actual = dao.findById(TRAINEE_ID);

            assertTrue(actual.isPresent());
            assertEquals(expected, actual.get());
            verify(session).find(Trainee.class, TRAINEE_ID);
        }
    }

    @Test
    void testFindById_ShouldReturnEmptyWhenNotExists() {
        Long id = 999L;

        try (MockedStatic<TransactionHandler> mockedStatic = mockStatic(TransactionHandler.class)) {
            mockedStatic.when(() -> TransactionHandler.performReturningWithinSession(any(Function.class)))
                    .thenAnswer(invocation -> {
                        Function<Session, Optional<Trainee>> function = invocation.getArgument(0);
                        when(session.find(Trainee.class, id)).thenReturn(null);
                        return function.apply(session);
                    });

            Optional<Trainee> actual = dao.findById(id);

            assertFalse(actual.isPresent());
            verify(session).find(Trainee.class, id);
        }
    }

    @Test
    void testFindAll_ShouldReturnAllTrainees() {
        Trainee trainee1 = createSampleTrainee(1L);
        Trainee trainee2 = createSampleTrainee(2L);
        List<Trainee> expectedList = Arrays.asList(trainee1, trainee2);

        try (MockedStatic<TransactionHandler> mockedStatic = mockStatic(TransactionHandler.class)) {
            mockedStatic.when(() -> TransactionHandler.performReturningWithinSession(any(Function.class)))
                    .thenAnswer(invocation -> {
                        Function<Session, List<Trainee>> function = invocation.getArgument(0);
                        when(session.createQuery("FROM Trainee", Trainee.class)).thenReturn(query);
                        when(query.getResultList()).thenReturn(expectedList);
                        return function.apply(session);
                    });

            List<Trainee> actual = dao.findAll();

            assertEquals(2, actual.size());
            assertTrue(actual.contains(trainee1));
            assertTrue(actual.contains(trainee2));
            verify(session).createQuery("FROM Trainee", Trainee.class);
            verify(query).getResultList();
        }
    }

    @Test
    void testFindAll_ShouldReturnEmptyListWhenNoTrainees() {
        try (MockedStatic<TransactionHandler> mockedStatic = mockStatic(TransactionHandler.class)) {
            mockedStatic.when(() -> TransactionHandler.performReturningWithinSession(any(Function.class)))
                    .thenAnswer(invocation -> {
                        Function<Session, List<Trainee>> function = invocation.getArgument(0);
                        when(session.createQuery("FROM Trainee", Trainee.class)).thenReturn(query);
                        when(query.getResultList()).thenReturn(Collections.emptyList());
                        return function.apply(session);
                    });

            List<Trainee> actual = dao.findAll();

            assertTrue(actual.isEmpty());
            verify(session).createQuery("FROM Trainee", Trainee.class);
            verify(query).getResultList();
        }
    }

    @Test
    void testUpdate_ShouldUpdateExistingTrainee() {
        Trainee existingTrainee = createSampleTrainee(TRAINEE_ID);
        User updatedUser = existingTrainee.getUser().toBuilder()
                .firstName("John Updated")
                .isActive(false)
                .build();
        Trainee updatedTrainee = existingTrainee.toBuilder()
                .user(updatedUser)
                .address("456 Oak Ave")
                .build();

        try (MockedStatic<TransactionHandler> mockedStatic = mockStatic(TransactionHandler.class)) {
            mockedStatic.when(() -> TransactionHandler.performReturningWithinSession(any(Function.class)))
                    .thenAnswer(invocation -> {
                        Function<Session, Trainee> function = invocation.getArgument(0);
                        when(session.find(Trainee.class, TRAINEE_ID)).thenReturn(existingTrainee);
                        when(session.merge(updatedTrainee)).thenReturn(updatedTrainee);
                        return function.apply(session);
                    });

            Trainee actual = dao.update(updatedTrainee);

            assertEquals(updatedTrainee, actual);
            verify(session).find(Trainee.class, TRAINEE_ID);
            verify(session).merge(updatedTrainee);
        }
    }

    @Test
    void testUpdate_ShouldThrowExceptionWhenTraineeNotExists() {
        Trainee trainee = createSampleTrainee(999L);

        try (MockedStatic<TransactionHandler> mockedStatic = mockStatic(TransactionHandler.class)) {
            mockedStatic.when(() -> TransactionHandler.performReturningWithinSession(any(Function.class)))
                    .thenAnswer(invocation -> {
                        Function<Session, Trainee> function = invocation.getArgument(0);
                        when(session.find(Trainee.class, 999L)).thenReturn(null);
                        return function.apply(session);
                    });

            DaoException exception = assertThrows(DaoException.class, () -> dao.update(trainee));

            assertEquals("Trainee not found with ID: 999", exception.getMessage());
            verify(session).find(Trainee.class, 999L);
            verify(session, never()).merge(any());
        }
    }

    @Test
    void testDelete_ShouldReturnTrueWhenTraineeExists() {
        Trainee trainee = createSampleTrainee(TRAINEE_ID);

        try (MockedStatic<TransactionHandler> mockedStatic = mockStatic(TransactionHandler.class)) {
            mockedStatic.when(() -> TransactionHandler.performReturningWithinSession(any(Function.class)))
                    .thenAnswer(invocation -> {
                        Function<Session, Boolean> function = invocation.getArgument(0);
                        when(session.find(Trainee.class, TRAINEE_ID)).thenReturn(trainee);
                        return function.apply(session);
                    });

            boolean result = dao.delete(TRAINEE_ID);

            assertTrue(result);
            verify(session).find(Trainee.class, TRAINEE_ID);
            verify(session).remove(trainee);
        }
    }

    @Test
    void testDelete_ShouldReturnFalseWhenTraineeNotExists() {
        Long id = 999L;

        try (MockedStatic<TransactionHandler> mockedStatic = mockStatic(TransactionHandler.class)) {
            mockedStatic.when(() -> TransactionHandler.performReturningWithinSession(any(Function.class)))
                    .thenAnswer(invocation -> {
                        Function<Session, Boolean> function = invocation.getArgument(0);
                        when(session.find(Trainee.class, id)).thenReturn(null);
                        return function.apply(session);
                    });

            boolean result = dao.delete(id);

            assertFalse(result);
            verify(session).find(Trainee.class, id);
            verify(session, never()).remove(any());
        }
    }

    private Trainee createSampleTrainee(Long id) {
        User user = User.builder()
                .id(1000L)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .username(USERNAME)
                .password(PASSWORD)
                .isActive(true)
                .build();

        return Trainee.builder()
                .id(id)
                .user(user)
                .dateOfBirth(DATE_OF_BIRTH)
                .address(ADDRESS)
                .build();
    }

    private Trainee createTraineeWithoutId(String firstName, String lastName, String username,
                                           LocalDate dateOfBirth, String address, Boolean isActive) {
        User user = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .username(username)
                .password(PASSWORD)
                .isActive(isActive)
                .build();

        return Trainee.builder()
                .user(user)
                .dateOfBirth(dateOfBirth)
                .address(address)
                .build();
    }
}
