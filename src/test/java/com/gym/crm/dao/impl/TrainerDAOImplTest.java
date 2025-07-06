package com.gym.crm.dao.impl;

import com.gym.crm.dao.hibernate.TransactionHandler;
import com.gym.crm.exception.DaoException;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.TrainingType;
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
class TrainerDAOImplTest {
    private static final Long TRAINER_ID = 1L;
    private static final String FIRST_NAME = "Jane";
    private static final String LAST_NAME = "Smith";
    private static final String USERNAME = "jane.smith";
    private static final String PASSWORD = "password456";
    private static final TrainingType DEFAULT_SPECIALIZATION = TrainingType.builder().trainingTypeName("Yoga").build();

    @Mock
    private SessionFactory sessionFactory;
    @Mock
    private Session session;
    @Mock
    private Transaction transaction;
    @Mock
    private Query<Trainer> query;
    @InjectMocks
    private TrainerDAOImpl dao;

    @Test
    void testCreate_ShouldCreateTrainer() {
        Trainer trainer = createTrainer();

        try (MockedStatic<TransactionHandler> mockedStatic = mockStatic(TransactionHandler.class)) {
            mockedStatic.when(() -> TransactionHandler.performReturningWithinSession(any(Function.class)))
                    .thenAnswer(invocation -> {
                        Function<Session, Trainer> function = invocation.getArgument(0);
                        return function.apply(session);
                    });

            Trainer result = dao.create(trainer);

            assertEquals(trainer, result);
            verify(session).persist(trainer);
        }
    }

    @Test
    void testCreate_ShouldCreateTrainerWithNullSpecialization() {
        Trainer trainer = createTrainer(null, false);

        try (MockedStatic<TransactionHandler> mockedStatic = mockStatic(TransactionHandler.class)) {
            mockedStatic.when(() -> TransactionHandler.performReturningWithinSession(any(Function.class)))
                    .thenAnswer(invocation -> {
                        Function<Session, Trainer> function = invocation.getArgument(0);
                        return function.apply(session);
                    });

            Trainer result = dao.create(trainer);

            assertEquals(trainer, result);
            verify(session).persist(trainer);
            assertNull(trainer.getSpecialization());
            assertFalse(trainer.getUser().getIsActive());
        }
    }

    @Test
    void testFindById_ShouldReturnTrainerWhenExists() {
        Trainer expected = createTrainerWithId(TRAINER_ID);

        try (MockedStatic<TransactionHandler> mockedStatic = mockStatic(TransactionHandler.class)) {
            mockedStatic.when(() -> TransactionHandler.performReturningWithinSession(any(Function.class)))
                    .thenAnswer(invocation -> {
                        Function<Session, Optional<Trainer>> function = invocation.getArgument(0);
                        when(session.find(Trainer.class, TRAINER_ID)).thenReturn(expected);
                        return function.apply(session);
                    });

            Optional<Trainer> actual = dao.findById(TRAINER_ID);

            assertTrue(actual.isPresent());
            assertEquals(expected, actual.get());
            verify(session).find(Trainer.class, TRAINER_ID);
        }
    }

    @Test
    void testFindById_ShouldReturnEmptyWhenNotExists() {
        Long id = 999L;

        try (MockedStatic<TransactionHandler> mockedStatic = mockStatic(TransactionHandler.class)) {
            mockedStatic.when(() -> TransactionHandler.performReturningWithinSession(any(Function.class)))
                    .thenAnswer(invocation -> {
                        Function<Session, Optional<Trainer>> function = invocation.getArgument(0);
                        when(session.find(Trainer.class, id)).thenReturn(null);
                        return function.apply(session);
                    });

            Optional<Trainer> actual = dao.findById(id);

            assertFalse(actual.isPresent());
            verify(session).find(Trainer.class, id);
        }
    }

    @Test
    void testFindAll_ShouldReturnAllTrainers() {
        Trainer trainer1 = createTrainerWithId(1L);
        Trainer trainer2 = createTrainerWithId(2L);
        List<Trainer> expectedList = Arrays.asList(trainer1, trainer2);

        try (MockedStatic<TransactionHandler> mockedStatic = mockStatic(TransactionHandler.class)) {
            mockedStatic.when(() -> TransactionHandler.performReturningWithinSession(any(Function.class)))
                    .thenAnswer(invocation -> {
                        Function<Session, List<Trainer>> function = invocation.getArgument(0);
                        when(session.createQuery("FROM Trainer", Trainer.class)).thenReturn(query);
                        when(query.getResultList()).thenReturn(expectedList);
                        return function.apply(session);
                    });

            List<Trainer> actual = dao.findAll();

            assertEquals(2, actual.size());
            assertTrue(actual.contains(trainer1));
            assertTrue(actual.contains(trainer2));
            verify(session).createQuery("FROM Trainer", Trainer.class);
            verify(query).getResultList();
        }
    }

    @Test
    void testFindAll_ShouldReturnEmptyListWhenNoTrainers() {
        try (MockedStatic<TransactionHandler> mockedStatic = mockStatic(TransactionHandler.class)) {
            mockedStatic.when(() -> TransactionHandler.performReturningWithinSession(any(Function.class)))
                    .thenAnswer(invocation -> {
                        Function<Session, List<Trainer>> function = invocation.getArgument(0);
                        when(session.createQuery("FROM Trainer", Trainer.class)).thenReturn(query);
                        when(query.getResultList()).thenReturn(Collections.emptyList());
                        return function.apply(session);
                    });

            List<Trainer> actual = dao.findAll();

            assertTrue(actual.isEmpty());
            verify(session).createQuery("FROM Trainer", Trainer.class);
            verify(query).getResultList();
        }
    }

    @Test
    void testUpdate_ShouldUpdateExistingTrainer() {
        Trainer existingTrainer = createTrainerWithId(TRAINER_ID);
        User updatedUser = existingTrainer.getUser().toBuilder()
                .firstName("John Updated")
                .isActive(false)
                .build();
        Trainer updatedTrainer = existingTrainer.toBuilder()
                .user(updatedUser)
                .specialization(TrainingType.builder().trainingTypeName("Pilates").build())
                .build();

        try (MockedStatic<TransactionHandler> mockedStatic = mockStatic(TransactionHandler.class)) {
            mockedStatic.when(() -> TransactionHandler.performReturningWithinSession(any(Function.class)))
                    .thenAnswer(invocation -> {
                        Function<Session, Trainer> function = invocation.getArgument(0);
                        when(session.find(Trainer.class, TRAINER_ID)).thenReturn(existingTrainer);
                        when(session.merge(updatedTrainer)).thenReturn(updatedTrainer);
                        return function.apply(session);
                    });

            Trainer actual = dao.update(updatedTrainer);

            assertEquals(updatedTrainer, actual);
            verify(session).find(Trainer.class, TRAINER_ID);
            verify(session).merge(updatedTrainer);
        }
    }

    @Test
    void testUpdate_ShouldThrowExceptionWhenTrainerNotExists() {
        Trainer trainer = createTrainerWithId(999L);

        try (MockedStatic<TransactionHandler> mockedStatic = mockStatic(TransactionHandler.class)) {
            mockedStatic.when(() -> TransactionHandler.performReturningWithinSession(any(Function.class)))
                    .thenAnswer(invocation -> {
                        Function<Session, Trainer> function = invocation.getArgument(0);
                        when(session.find(Trainer.class, 999L)).thenReturn(null);
                        return function.apply(session);
                    });

            DaoException exception = assertThrows(DaoException.class, () -> dao.update(trainer));

            assertEquals("Trainer not found with ID: 999", exception.getMessage());
            verify(session).find(Trainer.class, 999L);
            verify(session, never()).merge(any());
        }
    }

    private Trainer createTrainer() {
        return createTrainer(DEFAULT_SPECIALIZATION, true);
    }

    private Trainer createTrainer(TrainingType specialization, boolean isActive) {
        User user = User.builder()
                .id(1000L)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .username(USERNAME)
                .password(PASSWORD)
                .isActive(isActive)
                .build();

        return Trainer.builder()
                .user(user)
                .specialization(specialization)
                .build();
    }

    private Trainer createTrainerWithId(Long id) {
        Trainer trainer = createTrainer();

        return trainer.toBuilder()
                .id(id)
                .build();
    }

}
