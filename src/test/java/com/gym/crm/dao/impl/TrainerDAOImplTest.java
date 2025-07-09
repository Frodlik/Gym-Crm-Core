package com.gym.crm.dao.impl;

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
    void testCreate_ShouldCreateTrainer() {
        Trainer trainer = createTrainer("Yoga", true);

        Trainer result = dao.create(trainer);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertNotNull(result.getUser().getId());
        assertEquals("Jane", result.getUser().getFirstName());
        assertEquals("Smith", result.getUser().getLastName());
        assertEquals("jane.smith", result.getUser().getUsername());
        assertTrue(result.getUser().getIsActive());

        String specializationName = doInSession(session -> {
            Trainer createdTrainer = session.get(Trainer.class, result.getId());

            return createdTrainer.getSpecialization().getTrainingTypeName();
        });

        assertEquals("Yoga", specializationName);
    }

    @Test
    void testCreate_ShouldCreateTrainerWithDefaultSpecialization() {
        Trainer trainer = createTrainer("Cardio", false);

        Trainer result = dao.create(trainer);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertNotNull(result.getSpecialization());
        assertFalse(result.getUser().getIsActive());

        String specializationName = doInSession(session -> {
            Trainer createdTrainer = session.get(Trainer.class, result.getId());

            return createdTrainer.getSpecialization().getTrainingTypeName();
        });

        assertEquals("Cardio", specializationName);
    }

    @Test
    void testCreate_ShouldCreateTrainerWithNotNullSpecialization() {
        Trainer trainer = createTrainer("HIIT", false);

        Trainer result = dao.create(trainer);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("HIIT", result.getSpecialization().getTrainingTypeName());
        assertFalse(result.getUser().getIsActive());
    }

    @Test
    void testFindById_ShouldReturnTrainerWhenExists() {
        Long existingId = 1L;

        Optional<Trainer> found = dao.findById(existingId);

        assertTrue(found.isPresent());

        Trainer t = found.get();
        assertEquals(existingId, t.getId());

        String specName = doInSession(session -> {
            Trainer persisted = session.get(Trainer.class, existingId);
            return persisted.getSpecialization().getTrainingTypeName();
        });
        assertEquals("Strength", specName);
    }

    @Test
    void testFindById_ShouldReturnEmptyWhenNotExists() {
        Optional<Trainer> found = dao.findById(999L);
        assertFalse(found.isPresent());
    }

    @Test
    void testFindAll_ShouldReturnAllTrainers() {
        List<Trainer> list = dao.findAll();

        assertNotNull(list);
        assertEquals(5, list.size());
    }

    @Test
    void testUpdate_ShouldUpdateExistingTrainer() {
        Long idToUpdate = 2L;

        Trainer trainer = dao.findById(idToUpdate).get();

        TrainingType hiitType = getExistingTrainingType("HIIT");

        User updatedUser = trainer.getUser().toBuilder()
                .firstName("UpdatedJane")
                .isActive(false)
                .build();

        Trainer updatedTrainer = trainer.toBuilder()
                .user(updatedUser)
                .specialization(hiitType)
                .build();

        Trainer result = dao.update(updatedTrainer);

        assertNotNull(result);
        assertEquals("UpdatedJane", result.getUser().getFirstName());

        String specializationName = doInSession(session -> {
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

        TransactionHandlerException exception = assertThrows(TransactionHandlerException.class, () -> dao.update(ghost));
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
