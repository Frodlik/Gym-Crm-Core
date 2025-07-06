package com.gym.crm.dao.impl;

import com.gym.crm.dao.hibernate.TransactionHandler;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.Training;
import com.gym.crm.model.TrainingType;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingDAOImplTest {
    private static final Long TRAINING_ID = 1L;
    private static final Long TRAINEE_ID = 1L;
    private static final Long TRAINER_ID = 2L;
    private static final String TRAINING_NAME = "Morning Yoga Session";
    private static final TrainingType TRAINING_TYPE = TrainingType.builder().trainingTypeName("Yoga").build();
    private static final LocalDate TRAINING_DATE = LocalDate.of(2024, 1, 15);
    private static final int DURATION = 60;

    @Mock
    private SessionFactory sessionFactory;
    @Mock
    private Session session;
    @Mock
    private Transaction transaction;
    @Mock
    private Query<Training> query;
    @InjectMocks
    private TrainingDAOImpl dao;

    @Test
    void testCreate_ShouldCreateTraining() {
        Training training = createTraining(TRAINEE_ID, TRAINER_ID, TRAINING_NAME, TRAINING_TYPE, TRAINING_DATE, DURATION);

        try (MockedStatic<TransactionHandler> mockedStatic = mockStatic(TransactionHandler.class)) {
            mockedStatic.when(() -> TransactionHandler.performReturningWithinSession(any(Function.class)))
                    .thenAnswer(invocation -> {
                        Function<Session, Training> function = invocation.getArgument(0);
                        return function.apply(session);
                    });

            Training result = dao.create(training);

            assertEquals(training, result);
            verify(session).persist(training);
        }
    }

    @Test
    void testCreate_ShouldCreateTrainingWithNullTrainingType() {
        Training training = createTraining(3L, 4L, "General Training", null,
                LocalDate.of(2024, 2, 20), 90);

        try (MockedStatic<TransactionHandler> mockedStatic = mockStatic(TransactionHandler.class)) {
            mockedStatic.when(() -> TransactionHandler.performReturningWithinSession(any(Function.class)))
                    .thenAnswer(invocation -> {
                        Function<Session, Training> function = invocation.getArgument(0);
                        return function.apply(session);
                    });

            Training result = dao.create(training);

            assertEquals(training, result);
            verify(session).persist(training);
            assertNull(training.getTrainingType());
        }
    }

    @Test
    void testFindById_ShouldReturnTrainingWhenExists() {
        Training expected = createSampleTraining();

        try (MockedStatic<TransactionHandler> mockedStatic = mockStatic(TransactionHandler.class)) {
            mockedStatic.when(() -> TransactionHandler.performReturningWithinSession(any(Function.class)))
                    .thenAnswer(invocation -> {
                        Function<Session, Optional<Training>> function = invocation.getArgument(0);
                        when(session.find(Training.class, TRAINING_ID)).thenReturn(expected);
                        return function.apply(session);
                    });

            Optional<Training> actual = dao.findById(TRAINING_ID);

            assertTrue(actual.isPresent());
            assertEquals(expected, actual.get());
            verify(session).find(Training.class, TRAINING_ID);
        }
    }

    @Test
    void testFindById_ShouldReturnEmptyWhenNotExists() {
        Long id = 999L;

        try (MockedStatic<TransactionHandler> mockedStatic = mockStatic(TransactionHandler.class)) {
            mockedStatic.when(() -> TransactionHandler.performReturningWithinSession(any(Function.class)))
                    .thenAnswer(invocation -> {
                        Function<Session, Optional<Training>> function = invocation.getArgument(0);
                        when(session.find(Training.class, id)).thenReturn(null);
                        return function.apply(session);
                    });

            Optional<Training> actual = dao.findById(id);

            assertFalse(actual.isPresent());
            verify(session).find(Training.class, id);
        }
    }

    @Test
    void testFindAll_ShouldReturnAllTrainings() {
        Training training1 = createSampleTraining();
        Training training2 = createTraining(3L, 4L, "Evening Pilates",
                TrainingType.builder().trainingTypeName("Pilates").build(), TRAINING_DATE, 75);
        List<Training> expectedList = Arrays.asList(training1, training2);

        try (MockedStatic<TransactionHandler> mockedStatic = mockStatic(TransactionHandler.class)) {
            mockedStatic.when(() -> TransactionHandler.performReturningWithinSession(any(Function.class)))
                    .thenAnswer(invocation -> {
                        Function<Session, List<Training>> function = invocation.getArgument(0);
                        when(session.createQuery("FROM Training", Training.class)).thenReturn(query);
                        when(query.getResultList()).thenReturn(expectedList);
                        return function.apply(session);
                    });

            List<Training> actual = dao.findAll();

            assertEquals(2, actual.size());
            assertTrue(actual.contains(training1));
            assertTrue(actual.contains(training2));
            verify(session).createQuery("FROM Training", Training.class);
            verify(query).getResultList();
        }
    }

    @Test
    void testFindAll_ShouldReturnEmptyListWhenNoTrainings() {
        try (MockedStatic<TransactionHandler> mockedStatic = mockStatic(TransactionHandler.class)) {
            mockedStatic.when(() -> TransactionHandler.performReturningWithinSession(any(Function.class)))
                    .thenAnswer(invocation -> {
                        Function<Session, List<Training>> function = invocation.getArgument(0);
                        when(session.createQuery("FROM Training", Training.class)).thenReturn(query);
                        when(query.getResultList()).thenReturn(Collections.emptyList());
                        return function.apply(session);
                    });

            List<Training> actual = dao.findAll();

            assertTrue(actual.isEmpty());
            verify(session).createQuery("FROM Training", Training.class);
            verify(query).getResultList();
        }
    }

    @Test
    void testCreate_ShouldHandleTrainingWithMinimalData() {
        LocalDate today = LocalDate.now();
        Training training = createTraining(5L, 6L, "Quick Session", null, today, 30);

        try (MockedStatic<TransactionHandler> mockedStatic = mockStatic(TransactionHandler.class)) {
            mockedStatic.when(() -> TransactionHandler.performReturningWithinSession(any(Function.class)))
                    .thenAnswer(invocation -> {
                        Function<Session, Training> function = invocation.getArgument(0);
                        return function.apply(session);
                    });

            Training result = dao.create(training);

            assertEquals(training, result);
            verify(session).persist(training);
            assertEquals("Quick Session", training.getTrainingName());
            assertEquals(today, training.getTrainingDate());
            assertEquals(30, training.getTrainingDuration());
            assertNull(training.getTrainingType());
        }
    }

    private Training createSampleTraining() {
        return createTraining(TRAINEE_ID, TRAINER_ID, TRAINING_NAME, TRAINING_TYPE, TRAINING_DATE, DURATION);
    }

    private Training createTraining(Long traineeId, Long trainerId, String name, TrainingType type, LocalDate date, int duration) {
        Trainee trainee = Trainee.builder()
                .id(traineeId)
                .build();

        Trainer trainer = Trainer.builder()
                .id(trainerId)
                .build();

        return Training.builder()
                .trainee(trainee)
                .trainer(trainer)
                .trainingName(name)
                .trainingType(type)
                .trainingDate(date)
                .trainingDuration(duration)
                .build();
    }
}
