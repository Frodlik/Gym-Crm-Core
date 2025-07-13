package com.gym.crm.dao.impl;

import com.github.database.rider.core.api.dataset.DataSet;
import com.gym.crm.exception.TransactionHandlerException;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TraineeDAOImplTest extends BaseIntegrationTest<TraineeDAOImpl> {
    @Test
    @DataSet(value = "dataset/trainee-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testCreate_ShouldPersistTraineeSuccessfully() {
        Trainee trainee = createSampleTrainee();

        Trainee actual = dao.create(trainee);

        assertNotNull(actual.getId());
        assertEquals("Alex", actual.getUser().getFirstName());
    }

    @Test
    @DataSet(value = "dataset/trainee-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testCreate_ShouldPersistTraineeWithNullAddress() {
        User user = User.builder()
                .firstName("John")
                .lastName("Doe")
                .username("john.doe")
                .password("pass")
                .isActive(false)
                .build();

        Trainee trainee = Trainee.builder()
                .user(user)
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .address(null)
                .build();

        Trainee actual = dao.create(trainee);

        assertNotNull(actual.getId());
        assertNull(actual.getAddress());
        assertFalse(actual.getUser().getIsActive());
    }

    @Test
    @DataSet(value = "dataset/trainee-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testFindById_ShouldReturnTraineeWhenExists() {
        Long existingId = 1L;

        Trainee actual = dao.findById(existingId).orElseThrow();

        assertEquals(LocalDate.of(1990, 1, 1), actual.getDateOfBirth());
        assertEquals("123 Main St, New York, NY 10001", actual.getAddress());
        assertEquals("Emma", actual.getUser().getFirstName());
        assertEquals("Miller", actual.getUser().getLastName());
        assertEquals("emma.miller", actual.getUser().getUsername());
    }

    @Test
    @DataSet(value = "dataset/trainee-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testFindById_ShouldReturnEmptyWhenNotExists() {
        Long nonExistentId = 999L;

        Optional<Trainee> actual = dao.findById(nonExistentId);

        assertFalse(actual.isPresent());
    }

    @Test
    @DataSet(value = "dataset/trainee-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void findByUsername_ShouldReturnTraineeWhenExists() {
        String existingUsername = "emma.miller";

        Optional<Trainee> actual = dao.findByUsername(existingUsername);

        assertTrue(actual.isPresent());
        assertEquals("Emma", actual.get().getUser().getFirstName());
        assertEquals("Miller", actual.get().getUser().getLastName());
        assertEquals(existingUsername, actual.get().getUser().getUsername());
        assertEquals("123 Main St, New York, NY 10001", actual.get().getAddress());
        assertEquals(LocalDate.of(1990, 1, 1), actual.get().getDateOfBirth());
    }

    @Test
    @DataSet(value = "dataset/trainee-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void findByUsername_ShouldReturnEmptyWhenNotExists() {
        String nonExistentUsername = "non.existent";

        Optional<Trainee> actual = dao.findByUsername(nonExistentUsername);

        assertFalse(actual.isPresent());
    }

    @Test
    @DataSet(value = "dataset/trainee-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testFindAll_ShouldReturnInitialTraineesFromDataset() {
        List<Trainee> actualTrainees = dao.findAll();

        Trainee trainee = actualTrainees.getFirst();

        assertEquals(1, actualTrainees.size());
        assertEquals("Emma", trainee.getUser().getFirstName());
        assertEquals("Miller", trainee.getUser().getLastName());
        assertEquals("emma.miller", trainee.getUser().getUsername());
        assertEquals("password123", trainee.getUser().getPassword());
        assertEquals("123 Main St, New York, NY 10001", trainee.getAddress());
        assertEquals(LocalDate.of(1990, 1, 1), trainee.getDateOfBirth());
    }

    @Test
    @DataSet(value = "dataset/trainee-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testUpdate_ShouldUpdateExistingTrainee() {
        Long idToUpdate = 1L;
        Trainee trainee = dao.findById(idToUpdate).orElseThrow();

        User user = trainee.getUser().toBuilder()
                .firstName("UpdatedFirst")
                .lastName("UpdatedLast")
                .isActive(false)
                .build();

        Trainee updated = trainee.toBuilder()
                .user(user)
                .address("Updated Address 456")
                .dateOfBirth(LocalDate.of(1980, 12, 31))
                .build();

        Trainee actual = dao.update(updated);

        assertEquals(idToUpdate, actual.getId());
        assertEquals("UpdatedFirst", actual.getUser().getFirstName());
        assertEquals("UpdatedLast", actual.getUser().getLastName());
        assertFalse(actual.getUser().getIsActive());
        assertEquals("Updated Address 456", actual.getAddress());
        assertEquals(LocalDate.of(1980, 12, 31), actual.getDateOfBirth());
    }

    @Test
    @DataSet(value = "dataset/trainee-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testUpdate_ShouldThrowExceptionWhenTraineeNotExists() {
        User nonExistentUser = User.builder()
                .firstName("Non")
                .lastName("Existent")
                .username("non.existent")
                .password("password123")
                .isActive(true)
                .build();

        Trainee nonExistentTrainee = Trainee.builder()
                .id(999L)
                .user(nonExistentUser)
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .address("123 Main St")
                .build();

        assertThrows(RuntimeException.class, () -> dao.update(nonExistentTrainee));
    }

    @DataSet(value = "dataset/trainer-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testUpdateTraineeTrainersList_ShouldUpdateTrainersListSuccessfully() {
        String traineeUsername = "tom.brown";
        List<String> trainerUsernames = List.of("sarah.johnson", "mike.wilson");
        List<Trainer> expected = buildExpectedTrainers();

        Trainee actual = dao.updateTraineeTrainersList(traineeUsername, trainerUsernames);

        assertNotNull(actual);
        assertEquals(traineeUsername, actual.getUser().getUsername());
        assertEquals(2, actual.getTrainers().size());
        assertTrue(actual.getTrainers().containsAll(expected));
    }

    @Test
    @DataSet(value = "dataset/trainer-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testUpdateTraineeTrainersList_ShouldClearPreviousTrainers() {
        String traineeUsername = "tom.brown";
        List<String> firstTrainersList = List.of("sarah.johnson");
        List<String> secondTrainersList = List.of("mike.wilson");

        dao.updateTraineeTrainersList(traineeUsername, firstTrainersList);

        Trainee actual = dao.updateTraineeTrainersList(traineeUsername, secondTrainersList);

        assertNotNull(actual);
        assertEquals(1, actual.getTrainers().size());
    }

    @Test
    @DataSet(value = "dataset/trainer-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testUpdateTraineeTrainersList_ShouldThrowExceptionWhenTraineeNotExists() {
        String nonExistentTraineeUsername = "non.existent";
        List<String> trainerUsernames = List.of("sarah.johnson");

        TransactionHandlerException actualException = assertThrows(TransactionHandlerException.class,
                () -> dao.updateTraineeTrainersList(nonExistentTraineeUsername, trainerUsernames)
        );

        assertEquals("Error performing Hibernate operation. Transaction is rolled back", actualException.getMessage());
    }

    @Test
    @DataSet(value = "dataset/trainee-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testDeleteByUsername_ShouldReturnTrueWhenTraineeExists() {
        String existingUsername = "emma.miller";

        dao.deleteByUsername(existingUsername);

        Optional<Trainee> trainee = dao.findByUsername(existingUsername);

        assertFalse(trainee.isPresent());
    }

    @Test
    @DataSet(value = "dataset/trainee-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testDeleteByUsername_ShouldReturnFalseWhenTraineeNotExists() {
        String nonExistentUsername = "non.existent";

        dao.deleteByUsername(nonExistentUsername);

        List<Trainee> trainees = dao.findAll();
        assertEquals(1, trainees.size());
    }

    @Test
    @DataSet(value = "dataset/trainee-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testConcurrentOperations_ShouldHandleMultipleTrainees() {
        Trainee trainee1 = createSampleTraineeWithUsername("user1");
        Trainee trainee2 = createSampleTraineeWithDetails(LocalDate.of(1990, 1, 1));

        Trainee saved1 = dao.create(trainee1);
        Trainee saved2 = dao.create(trainee2);

        User updatedUser = saved1.getUser().toBuilder()
                .firstName("John Updated")
                .build();

        dao.update(saved1.toBuilder().user(updatedUser).build());
        dao.deleteByUsername(saved2.getUser().getUsername());

        List<Trainee> allTrainees = dao.findAll();

        Optional<Trainee> updated = allTrainees.stream()
                .filter(t -> t.getUser().getUsername().equals("user1"))
                .findFirst();

        Optional<Trainee> deletedCheck = allTrainees.stream()
                .filter(t -> t.getUser().getUsername().equals("user2"))
                .findFirst();

        assertTrue(updated.isPresent());
        assertEquals("John Updated", updated.get().getUser().getFirstName());
        assertFalse(deletedCheck.isPresent());
    }

    @Test
    @DataSet(value = "dataset/trainee-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testDatabaseConstraints_ShouldEnforceUniqueUsername() {
        Trainee trainee1 = createSampleTraineeWithUsername("duplicate.user");
        Trainee trainee2 = createSampleTraineeWithUsername("duplicate.user");

        dao.create(trainee1);

        assertThrows(RuntimeException.class, () -> dao.create(trainee2));
    }

    @Test
    @DataSet(value = "dataset/trainee-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testFindAll_ShouldReturnEmptyListWhenNoTrainees() {
        dao.deleteByUsername("emma.miller");

        List<Trainee> allTrainees = dao.findAll();

        assertTrue(allTrainees.isEmpty());
    }

    private Trainee createSampleTrainee() {
        User user = User.builder()
                .firstName("Alex")
                .lastName("Turner")
                .username("alex.turner")
                .password("password123")
                .isActive(true)
                .build();

        return Trainee.builder()
                .user(user)
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .address("123 Main St")
                .build();
    }

    private Trainee createSampleTraineeWithUsername(String username) {
        User user = User.builder()
                .firstName("John")
                .lastName("Doe")
                .username(username)
                .password("password123")
                .isActive(true)
                .build();

        return Trainee.builder()
                .user(user)
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .address("123 Main St")
                .build();
    }

    private Trainee createSampleTraineeWithDetails(LocalDate dateOfBirth) {
        User user = User.builder()
                .firstName("Jane")
                .lastName("Doe")
                .username("user2")
                .password("password123")
                .isActive(true)
                .build();

        return Trainee.builder()
                .user(user)
                .dateOfBirth(dateOfBirth)
                .address("123 Main St")
                .build();
    }

    private List<Trainer> buildExpectedTrainers() {
        TrainingType strength = TrainingType.builder()
                .id(1L)
                .trainingTypeName("Strength")
                .build();
        TrainingType yoga = TrainingType.builder()
                .id(2L)
                .trainingTypeName("Yoga")
                .build();

        User sarah = User.builder()
                .id(1L)
                .firstName("Sarah")
                .lastName("Johnson")
                .username("sarah.johnson")
                .password("password456")
                .isActive(true)
                .build();
        User mike = User.builder()
                .id(2L)
                .firstName("Mike")
                .lastName("Wilson")
                .username("mike.wilson")
                .password("password789")
                .isActive(false)
                .build();

        Trainer trainerSarah = Trainer.builder()
                .id(1L)
                .user(sarah)
                .specialization(strength)
                .build();
        Trainer trainerMike = Trainer.builder()
                .id(2L)
                .user(mike)
                .specialization(yoga)
                .build();

        return List.of(trainerMike, trainerSarah);
    }
}
