package com.gym.crm.dao.impl;

import com.github.database.rider.core.api.dataset.DataSet;
import com.gym.crm.exception.TransactionHandlerException;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.TrainingType;
import com.gym.crm.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TrainerDAOImplTest extends BaseIntegrationTest<TrainerDAOImpl> {
    @Test
    @DataSet(value = "dataset/trainer-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testCreate_ShouldPersistTrainerWithYogaSpecialization() {
        Trainer trainerToCreate = createSampleTrainerWithSpecialization("Yoga", true);

        Trainer actual = dao.create(trainerToCreate);

        String actualSpecializationName = doInSession(session -> {
            Trainer persistedTrainer = session.get(Trainer.class, actual.getId());

            return persistedTrainer.getSpecialization().getTrainingTypeName();
        });

        assertNotNull(actual);
        assertNotNull(actual.getId());
        assertNotNull(actual.getUser().getId());
        assertEquals("Jane", actual.getUser().getFirstName());
        assertEquals("Smith", actual.getUser().getLastName());
        assertEquals("jane.smith", actual.getUser().getUsername());
        assertTrue(actual.getUser().getIsActive());
        assertEquals("Yoga", actualSpecializationName);
    }

    @Test
    @DataSet(value = "dataset/trainer-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testCreate_ShouldPersistTrainerWithCardioSpecializationAndInactiveStatus() {
        Trainer trainerToCreate = createSampleTrainerWithSpecialization("Cardio", false);

        Trainer actual = dao.create(trainerToCreate);

        String actualSpecializationName = doInSession(session -> {
            Trainer persistedTrainer = session.get(Trainer.class, actual.getId());

            return persistedTrainer.getSpecialization().getTrainingTypeName();
        });

        assertNotNull(actual);
        assertNotNull(actual.getId());
        assertNotNull(actual.getSpecialization());
        assertFalse(actual.getUser().getIsActive());
        assertEquals("Cardio", actualSpecializationName);
    }

    @Test
    @DataSet(value = "dataset/trainer-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testCreate_ShouldPersistTrainerWithHIITSpecializationAndInactiveStatus() {
        Trainer trainerToCreate = createSampleTrainerWithSpecialization("HIIT", false);

        Trainer actualTrainer = dao.create(trainerToCreate);

        assertNotNull(actualTrainer);
        assertNotNull(actualTrainer.getId());
        assertEquals("HIIT", actualTrainer.getSpecialization().getTrainingTypeName());
        assertFalse(actualTrainer.getUser().getIsActive());
    }

    @Test
    @DataSet(value = "dataset/trainer-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testFindById_ShouldReturnTrainerWhenExists() {
        Long existingTrainerId = 1L;

        Trainer actual = dao.findById(existingTrainerId).orElseThrow();

        String actualSpecializationName = doInSession(session -> {
            Trainer persistedTrainer = session.get(Trainer.class, existingTrainerId);

            return persistedTrainer.getSpecialization().getTrainingTypeName();
        });

        assertEquals(existingTrainerId, actual.getId());
        assertEquals("Sarah", actual.getUser().getFirstName());
        assertEquals("Johnson", actual.getUser().getLastName());
        assertEquals("sarah.johnson", actual.getUser().getUsername());
        assertTrue(actual.getUser().getIsActive());
        assertEquals("Strength", actualSpecializationName);
    }

    @Test
    @DataSet(value = "dataset/trainer-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testFindById_ShouldReturnEmptyWhenTrainerNotExists() {
        Long nonExistentTrainerId = 999L;

        Optional<Trainer> actual = dao.findById(nonExistentTrainerId);

        assertFalse(actual.isPresent());
    }

    @Test
    @DataSet(value = "dataset/trainer-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void findByUsername_ShouldReturnTrainerWhenExists() {
        String existingUsername = "anna.davis";

        Optional<Trainer> actual = dao.findByUsername(existingUsername);

        assertTrue(actual.isPresent());

        Trainer trainer = actual.get();
        assertEquals("Anna", trainer.getUser().getFirstName());
        assertEquals("Davis", trainer.getUser().getLastName());
        assertEquals(existingUsername, trainer.getUser().getUsername());
    }

    @Test
    @DataSet(value = "dataset/trainer-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void findByUsername_ShouldReturnEmptyWhenNotExists() {
        String nonExistentUsername = "non.existent";

        Optional<Trainer> actual = dao.findByUsername(nonExistentUsername);

        assertFalse(actual.isPresent());
    }

    @Test
    @DataSet(value = "dataset/trainer-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testFindAll_ShouldReturnAllExistingTrainers() {
        int expectedTrainersCount = 3;

        List<Trainer> actualTrainersList = dao.findAll();

        Trainer firstTrainer = actualTrainersList.stream()
                .filter(t -> t.getUser().getUsername().equals("sarah.johnson"))
                .findFirst()
                .orElseThrow();

        Trainer secondTrainer = actualTrainersList.stream()
                .filter(t -> t.getUser().getUsername().equals("mike.wilson"))
                .findFirst()
                .orElseThrow();

        assertNotNull(actualTrainersList);
        assertEquals(expectedTrainersCount, actualTrainersList.size());
        assertEquals("Sarah", firstTrainer.getUser().getFirstName());
        assertEquals("Johnson", firstTrainer.getUser().getLastName());
        assertTrue(firstTrainer.getUser().getIsActive());
        assertEquals("Mike", secondTrainer.getUser().getFirstName());
        assertEquals("Wilson", secondTrainer.getUser().getLastName());
        assertFalse(secondTrainer.getUser().getIsActive());
    }

    @Test
    @DataSet(value = "dataset/trainer-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testFindTrainersNotAssignedToTrainee_ShouldReturnUnassignedTrainers() {
        String traineeUsername = "tom.brown";

        List<Trainer> actual = dao.findTrainersNotAssignedToTrainee(traineeUsername);

        assertNotNull(actual);
        assertEquals(1, actual.size());
        assertEquals("mike.wilson", actual.get(0).getUser().getUsername());
        assertEquals("Mike", actual.get(0).getUser().getFirstName());
        assertNotNull(actual.get(0).getSpecialization());
    }

    @Test
    @DataSet(value = "dataset/trainer-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testFindTrainersNotAssignedToTrainee_ShouldReturnAllTrainersWhenTraineeHasNoTrainings() {
        String nonExistentTraineeUsername = "non.existent";

        List<Trainer> actual = dao.findTrainersNotAssignedToTrainee(nonExistentTraineeUsername);

        assertNotNull(actual);
        assertEquals(3, actual.size());
    }

    @Test
    @DataSet(value = "dataset/trainer-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testUpdate_ShouldUpdateExistingTrainerWithNewSpecializationAndUserData() {
        Long trainerIdToUpdate = 2L;
        Trainer existingTrainer = dao.findById(trainerIdToUpdate).orElseThrow();

        TrainingType pilatesSpecialization = getExistingTrainingType("Pilates");

        User updatedUser = existingTrainer.getUser().toBuilder()
                .firstName("UpdatedMike")
                .isActive(true)
                .build();

        Trainer trainerToUpdate = existingTrainer.toBuilder()
                .user(updatedUser)
                .specialization(pilatesSpecialization)
                .build();

        Trainer actual = dao.update(trainerToUpdate);

        String actualSpecializationName = doInSession(session -> {
            Trainer persistedTrainer = session.get(Trainer.class, actual.getId());

            return persistedTrainer.getSpecialization().getTrainingTypeName();
        });

        assertNotNull(actual);
        assertEquals("UpdatedMike", actual.getUser().getFirstName());
        assertEquals("Wilson", actual.getUser().getLastName());
        assertTrue(actual.getUser().getIsActive());
        assertEquals("Pilates", actualSpecializationName);
    }

    @Test
    @DataSet(value = "dataset/trainer-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testUpdate_ShouldThrowExceptionWhenTrainerNotExists() {
        User nonExistentUser = User.builder()
                .firstName("Ghost")
                .lastName("User")
                .username("ghost.user")
                .password("pwd")
                .isActive(true)
                .build();

        Trainer nonExistentTrainer = Trainer.builder()
                .id(999L)
                .user(nonExistentUser)
                .specialization(null)
                .build();

        TransactionHandlerException actualException = assertThrows(
                TransactionHandlerException.class,
                () -> dao.update(nonExistentTrainer)
        );
        assertEquals("Error performing Hibernate operation. Transaction is rolled back", actualException.getMessage());
    }

    private Trainer createSampleTrainerWithSpecialization(String specializationName, boolean isActive) {
        String actualSpecializationName = specializationName != null ? specializationName : "Cardio";
        TrainingType specialization = getExistingTrainingType(actualSpecializationName);

        User user = User.builder()
                .firstName("Jane")
                .lastName("Smith")
                .username("jane.smith")
                .password("password456")
                .isActive(isActive)
                .build();

        return Trainer.builder()
                .user(user)
                .specialization(specialization)
                .build();
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
