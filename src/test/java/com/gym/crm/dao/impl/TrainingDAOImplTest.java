package com.gym.crm.dao.impl;

import com.github.database.rider.core.api.dataset.DataSet;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.Training;
import com.gym.crm.model.TrainingType;
import com.gym.crm.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TrainingDAOImplTest extends BaseIntegrationTest<TrainingDAOImpl> {
    @Test
    @DataSet(value = "dataset/training-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testCreate_ShouldPersistTrainingWithYogaSpecializationAndCorrectDuration() {
        Training trainingToCreate = createSampleTrainingWithSpecialization("Morning Yoga Session", "Yoga", 60);

        Training actual = dao.create(trainingToCreate);

        String actualTrainingTypeName = doInSession(session -> {
            Training persistedTraining = session.get(Training.class, actual.getId());

            return persistedTraining.getTrainingType().getTrainingTypeName();
        });

        assertNotNull(actual);
        assertNotNull(actual.getId());
        assertEquals("Morning Yoga Session", actual.getTrainingName());
        assertEquals(60, actual.getTrainingDuration());
        assertEquals(LocalDate.of(2024, 8, 15), actual.getTrainingDate());
        assertNotNull(actual.getTrainee());
        assertNotNull(actual.getTrainer());
        assertNotNull(actual.getTrainingType());
        assertEquals("Yoga", actualTrainingTypeName);
    }

    @Test
    @DataSet(value = "dataset/training-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testCreate_ShouldPersistTrainingWithFlexibilitySpecializationAndExtendedDuration() {
        Training trainingToCreate = createSampleTrainingWithSpecialization("General Flexibility Training", "Flexibility", 90);

        Training actual = dao.create(trainingToCreate);

        String actualTrainingTypeName = doInSession(session -> {
            Training persistedTraining = session.get(Training.class, actual.getId());

            return persistedTraining.getTrainingType().getTrainingTypeName();
        });

        assertNotNull(actual);
        assertNotNull(actual.getId());
        assertEquals("General Flexibility Training", actual.getTrainingName());
        assertEquals(90, actual.getTrainingDuration());
        assertEquals("Flexibility", actualTrainingTypeName);
    }

    @Test
    @DataSet(value = "dataset/training-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testCreate_ShouldPersistTrainingWithMinimalDataAndDefaultCardioType() {
        LocalDate todayDate = LocalDate.now();
        Training trainingToCreate = createSampleTrainingWithMinimalData(todayDate);

        Training actual = dao.create(trainingToCreate);

        String actualTrainingTypeName = doInSession(session -> {
            Training persistedTraining = session.get(Training.class, actual.getId());

            return persistedTraining.getTrainingType().getTrainingTypeName();
        });

        assertNotNull(actual);
        assertNotNull(actual.getId());
        assertEquals("Quick Session", actual.getTrainingName());
        assertEquals(todayDate, actual.getTrainingDate());
        assertEquals(30, actual.getTrainingDuration());
        assertNotNull(actual.getTrainingType());
        assertEquals("Cardio", actualTrainingTypeName);
    }

    @Test
    @DataSet(value = "dataset/training-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testFindById_ShouldReturnTrainingWhenExists() {
        Long existingTrainingId = 1L;

        Training actual = dao.findById(existingTrainingId).orElseThrow();

        doInSession(session -> {
            Training persistedTraining = session.get(Training.class, existingTrainingId);

            assertEquals(existingTrainingId, actual.getId());
            assertEquals("Power Strength Training", actual.getTrainingName());
            assertEquals(LocalDate.of(2024, 8, 10), actual.getTrainingDate());
            assertEquals(75, actual.getTrainingDuration());
            assertEquals("Strength", persistedTraining.getTrainingType().getTrainingTypeName());
            assertEquals("alex.trainer", persistedTraining.getTrainer().getUser().getUsername());
            assertEquals("john.trainee", persistedTraining.getTrainee().getUser().getUsername());
        });
    }

    @Test
    @DataSet(value = "dataset/training-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testFindById_ShouldReturnEmptyWhenTrainingNotExists() {
        Long nonExistentTrainingId = 999L;

        Optional<Training> actual = dao.findById(nonExistentTrainingId);

        assertFalse(actual.isPresent());
    }

    @Test
    @DataSet(value = "dataset/training-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testFindAll_ShouldReturnAllExistingTrainings() {
        int expectedTrainingsCount = 4;

        List<Training> actualTrainingsList = dao.findAll();

        doInSession(session -> {
            Training firstTraining = actualTrainingsList.get(0);
            Training secondTraining = actualTrainingsList.get(1);

            Training freshFirstTraining = session.get(Training.class, firstTraining.getId());
            Training freshSecondTraining = session.get(Training.class, secondTraining.getId());

            assertNotNull(actualTrainingsList);
            assertEquals(expectedTrainingsCount, actualTrainingsList.size());
            assertEquals("Power Strength Training", firstTraining.getTrainingName());
            assertEquals(75, firstTraining.getTrainingDuration());
            assertEquals("Relaxing Yoga Session", secondTraining.getTrainingName());
            assertEquals(90, secondTraining.getTrainingDuration());
            assertEquals("alex.trainer", freshFirstTraining.getTrainer().getUser().getUsername());
            assertEquals("maria.trainer", freshSecondTraining.getTrainer().getUser().getUsername());
        });
    }

    private Training createSampleTrainingWithSpecialization(String trainingName, String trainingTypeName, int duration) {
        Trainee trainee = createSampleTrainee();
        Trainer trainer = createSampleTrainer();
        TrainingType trainingType = trainingTypeName != null ? getExistingTrainingType(trainingTypeName) : null;

        return Training.builder()
                .trainee(trainee)
                .trainer(trainer)
                .trainingName(trainingName)
                .trainingType(trainingType)
                .trainingDate(LocalDate.of(2024, 8, 15))
                .trainingDuration(duration)
                .build();
    }

    private Training createSampleTrainingWithMinimalData(LocalDate trainingDate) {
        Trainee trainee = createSampleTrainee();
        Trainer trainer = createSampleTrainer();
        TrainingType defaultTrainingType = getExistingTrainingType("Cardio");

        return Training.builder()
                .trainee(trainee)
                .trainer(trainer)
                .trainingName("Quick Session")
                .trainingType(defaultTrainingType)
                .trainingDate(trainingDate)
                .trainingDuration(30)
                .build();
    }

    private Trainee createSampleTrainee() {
        return doInSession(session -> {
            User user = User.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .username("john.doe" + System.currentTimeMillis())
                    .password("password123")
                    .isActive(true)
                    .build();

            Trainee trainee = Trainee.builder()
                    .user(user)
                    .dateOfBirth(LocalDate.of(1990, 1, 1))
                    .address("123 Main St")
                    .build();

            session.persist(trainee);
            session.flush();

            return trainee;
        });
    }

    private Trainer createSampleTrainer() {
        return doInSession(session -> {
            User user = User.builder()
                    .firstName("Jane")
                    .lastName("Smith")
                    .username("jane.smith" + System.currentTimeMillis())
                    .password("password456")
                    .isActive(true)
                    .build();

            TrainingType specialization = getExistingTrainingType("Yoga");

            Trainer trainer = Trainer.builder()
                    .user(user)
                    .specialization(specialization)
                    .build();

            session.persist(trainer);
            session.flush();

            return trainer;
        });
    }

    private TrainingType getExistingTrainingType(String trainingTypeName) {
        return doInSession(session -> {
            TrainingType trainingType = session.createQuery(
                            "SELECT tt FROM TrainingType tt WHERE tt.trainingTypeName = :name", TrainingType.class)
                    .setParameter("name", trainingTypeName)
                    .uniqueResult();

            if (trainingType != null) {
                session.evict(trainingType);
            }

            return trainingType;
        });
    }
}
