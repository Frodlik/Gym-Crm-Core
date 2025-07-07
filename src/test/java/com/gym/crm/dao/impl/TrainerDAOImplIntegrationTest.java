package com.gym.crm.dao.impl;

import com.gym.crm.dao.TrainerDAO;
import com.gym.crm.dao.hibernate.TransactionHandler;
import com.gym.crm.exception.TransactionHandlerException;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.TrainingType;
import com.gym.crm.model.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TrainerDAOImplIntegrationTest extends BaseIntegrationTest {
    private TrainerDAO trainerDAO;

    @BeforeAll
    void initDAO() {
        trainerDAO = new TrainerDAOImpl();
    }

    @BeforeEach
    void cleanDatabase() {
        TransactionHandler.performReturningWithinSession(session -> {
            session.createQuery("DELETE FROM Trainer").executeUpdate();
            session.createQuery("DELETE FROM User").executeUpdate();

            return null;
        });
    }

    @Test
    void testCreate_ShouldCreateTrainer() {
        Trainer trainer = createTrainer("Yoga", true);

        Trainer result = trainerDAO.create(trainer);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertNotNull(result.getUser().getId());
        assertEquals("Jane", result.getUser().getFirstName());
        assertEquals("Smith", result.getUser().getLastName());
        assertEquals("jane.smith", result.getUser().getUsername());
        assertTrue(result.getUser().getIsActive());

        String specializationName = TransactionHandler.performReturningWithinSession(session -> {
            Trainer createdTrainer = session.get(Trainer.class, result.getId());

            return createdTrainer.getSpecialization().getTrainingTypeName();
        });

        assertEquals("Yoga", specializationName);
    }

    @Test
    void testCreate_ShouldCreateTrainerWithDefaultSpecialization() {
        Trainer trainer = createTrainer("Cardio", false);

        Trainer result = trainerDAO.create(trainer);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertNotNull(result.getSpecialization());
        assertFalse(result.getUser().getIsActive());

        String specializationName = TransactionHandler.performReturningWithinSession(session -> {
            Trainer createdTrainer = session.get(Trainer.class, result.getId());

            return createdTrainer.getSpecialization().getTrainingTypeName();
        });

        assertEquals("Cardio", specializationName);
    }

    @Test
    void testCreate_ShouldCreateTrainerWithNullSpecialization() {
        Trainer trainer = createTrainerWithNullSpecialization();

        Trainer result = trainerDAO.create(trainer);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertNull(result.getSpecialization());
        assertFalse(result.getUser().getIsActive());
    }

    @Test
    void testFindById_ShouldReturnTrainerWhenExists() {
        Trainer trainer = createTrainer("Cardio", true);
        Trainer saved = trainerDAO.create(trainer);

        Optional<Trainer> found = trainerDAO.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());

        String specializationName = TransactionHandler.performReturningWithinSession(session -> {
            Trainer foundTrainer = session.get(Trainer.class, found.get().getId());

            return foundTrainer.getSpecialization().getTrainingTypeName();
        });

        assertEquals("Cardio", specializationName);
    }

    @Test
    void testFindById_ShouldReturnEmptyWhenNotExists() {
        Optional<Trainer> found = trainerDAO.findById(999L);
        assertFalse(found.isPresent());
    }

    @Test
    void testFindAll_ShouldReturnAllTrainers() {
        User user = User.builder()
                .firstName("Jane")
                .lastName("Smith")
                .username("jane.smith1")
                .password("password456")
                .isActive(true)
                .build();

        Trainer t1 = Trainer.builder()
                .user(user)
                .specialization(null)
                .build();

        Trainer t2 = createTrainer("Strength", true);
        trainerDAO.create(t1);
        trainerDAO.create(t2);

        List<Trainer> list = trainerDAO.findAll();

        assertNotNull(list);
        assertEquals(2, list.size());
    }

    @Test
    void testFindAll_ShouldReturnEmptyListWhenNoTrainers() {
        List<Trainer> list = trainerDAO.findAll();

        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    @Test
    void testUpdate_ShouldUpdateExistingTrainer() {
        Trainer trainer = createTrainer("Strength", true);
        Trainer saved = trainerDAO.create(trainer);

        TrainingType hiitType = getExistingTrainingType("HIIT");

        User updatedUser = saved.getUser().toBuilder()
                .firstName("Jane Updated")
                .isActive(false)
                .build();

        Trainer updatedTrainer = saved.toBuilder()
                .user(updatedUser)
                .specialization(hiitType)
                .build();

        Trainer result = trainerDAO.update(updatedTrainer);

        assertNotNull(result);
        assertEquals("Jane Updated", result.getUser().getFirstName());

        String specializationName = TransactionHandler.performReturningWithinSession(session -> {
            Trainer updatedTrainerFromDb = session.get(Trainer.class, result.getId());

            return updatedTrainerFromDb.getSpecialization().getTrainingTypeName();
        });

        assertEquals("HIIT", specializationName);
    }

    @Test
    void testUpdate_ShouldThrowExceptionWhenTrainerNotExists() {
        User user = User.builder()
                .firstName("Ghost")
                .lastName("User")
                .username("ghost.user")
                .password("pwd")
                .isActive(true)
                .build();

        Trainer ghost = Trainer.builder()
                .id(999L)
                .user(user)
                .specialization(null)
                .build();

        TransactionHandlerException exception = assertThrows(TransactionHandlerException.class, () -> trainerDAO.update(ghost));
        assertEquals("Error performing Hibernate operation. Transaction is rolled back", exception.getMessage());
    }

    private Trainer createTrainer(String specializationName, boolean isActive) {
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

    private Trainer createTrainerWithNullSpecialization() {
        User user = User.builder()
                .firstName("Jane")
                .lastName("Smith")
                .username("jane.smith")
                .password("password456")
                .isActive(false)
                .build();

        return Trainer.builder()
                .user(user)
                .specialization(null)
                .build();
    }

    private TrainingType getExistingTrainingType(String trainingTypeName) {
        return TransactionHandler.performReturningWithinSession(session -> {
            TrainingType trainingType = session.createQuery(
                            "SELECT tt FROM TrainingType tt WHERE tt.trainingTypeName = :name",
                            TrainingType.class)
                    .setParameter("name", trainingTypeName)
                    .uniqueResult();

            if (trainingType != null) {
                session.evict(trainingType);
            }

            return trainingType;
        });
    }
}
