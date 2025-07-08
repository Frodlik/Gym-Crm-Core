package com.gym.crm.dao.impl;

import com.gym.crm.dao.TrainingDAO;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.Training;
import com.gym.crm.model.TrainingType;
import com.gym.crm.model.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TrainingDAOImplTest extends BaseIntegrationTest {
    private TrainingDAO trainingDAO;

    @BeforeAll
    void initDAO() {
        trainingDAO = new TrainingDAOImpl();
    }

    @Test
    void testCreate_ShouldCreateTraining() {
        Training training = createTraining("Morning Yoga Session", "Yoga", 60);

        Training result = trainingDAO.create(training);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Morning Yoga Session", result.getTrainingName());
        assertEquals(60, result.getTrainingDuration());
        assertNotNull(result.getTrainee());
        assertNotNull(result.getTrainer());
        assertNotNull(result.getTrainingType());

        String trainingTypeName = doInSession(session -> {
            Training createdTraining = session.get(Training.class, result.getId());

            return createdTraining.getTrainingType().getTrainingTypeName();
        });

        assertEquals("Yoga", trainingTypeName);
    }

    @Test
    void testCreate_ShouldCreateTrainingWithNotNullTrainingType() {
        Training training = createTraining("General Training", "Flexibility", 90);

        Training result = trainingDAO.create(training);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("General Training", result.getTrainingName());
        assertEquals(90, result.getTrainingDuration());
        assertEquals("Flexibility", result.getTrainingType().getTrainingTypeName());
    }

    @Test
    void testFindById_ShouldReturnTrainingWhenExists() {
        Training training = createTraining("Morning Cardio", "Cardio", 45);
        Training saved = trainingDAO.create(training);

        Optional<Training> found = trainingDAO.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
        assertEquals("Morning Cardio", found.get().getTrainingName());
        assertEquals(45, found.get().getTrainingDuration());

        String trainingTypeName = doInSession(session -> {
            Training foundTraining = session.get(Training.class, found.get().getId());

            return foundTraining.getTrainingType().getTrainingTypeName();
        });

        assertEquals("Cardio", trainingTypeName);
    }

    @Test
    void testFindById_ShouldReturnEmptyWhenNotExists() {
        Optional<Training> found = trainingDAO.findById(999L);
        assertFalse(found.isPresent());
    }

    @Test
    void testFindAll_ShouldReturnAllTrainings() {
        Training training1 = createTraining("Morning Yoga", "Yoga", 60);
        Training training2 = createTraining("Evening Pilates", "Pilates", 75);

        trainingDAO.create(training1);
        trainingDAO.create(training2);

        List<Training> list = trainingDAO.findAll();

        assertNotNull(list);
        assertEquals(2, list.size());
    }

    @Test
    void testFindAll_ShouldReturnEmptyListWhenNoTrainings() {
        List<Training> list = trainingDAO.findAll();

        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    @Test
    void testCreate_ShouldHandleTrainingWithMinimalData() {
        LocalDate today = LocalDate.now();
        Training training = createTrainingWithMinimalData(today);

        Training result = trainingDAO.create(training);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Quick Session", result.getTrainingName());
        assertEquals(today, result.getTrainingDate());
        assertEquals(30, result.getTrainingDuration());
        assertNotNull(result.getTrainingType());
    }

    private Training createTraining(String trainingName, String trainingTypeName, int duration) {
        Trainee trainee = saveTrainee();
        Trainer trainer = saveTrainer();
        TrainingType trainingType = trainingTypeName != null ? getExistingTrainingType(trainingTypeName) : null;

        return Training.builder()
                .trainee(trainee)
                .trainer(trainer)
                .trainingName(trainingName)
                .trainingType(trainingType)
                .trainingDate(LocalDate.of(2024, 1, 15))
                .trainingDuration(duration)
                .build();
    }

    private Training createTrainingWithMinimalData(LocalDate date) {
        Trainee trainee = saveTrainee();
        Trainer trainer = saveTrainer();
        TrainingType defaultType = getExistingTrainingType("Cardio");

        return Training.builder()
                .trainee(trainee)
                .trainer(trainer)
                .trainingName("Quick Session")
                .trainingType(defaultType)
                .trainingDate(date)
                .trainingDuration(30)
                .build();
    }

    private Trainee saveTrainee() {
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

    private Trainer saveTrainer() {
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
