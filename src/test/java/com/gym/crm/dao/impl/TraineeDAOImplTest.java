package com.gym.crm.dao.impl;

import com.github.database.rider.core.api.dataset.DataSet;
import com.gym.crm.model.Trainee;
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

    @Test
    @DataSet(value = "dataset/trainee-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testDelete_ShouldReturnTrueWhenTraineeExists() {
        boolean isDeleted = dao.delete(1L);

        Optional<Trainee> trainee = dao.findById(1L);

        assertTrue(isDeleted);
        assertFalse(trainee.isPresent());
    }

    @Test
    @DataSet(value = "dataset/trainee-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testDelete_ShouldReturnFalseWhenTraineeNotExists() {
        Long nonExistentId = 999L;

        boolean isDeleted = dao.delete(nonExistentId);

        assertFalse(isDeleted);
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
        boolean isDeleted = dao.delete(saved2.getId());

        List<Trainee> allTrainees = dao.findAll();

        Optional<Trainee> updated = allTrainees.stream()
                .filter(t -> t.getUser().getUsername().equals("user1"))
                .findFirst();

        Optional<Trainee> deletedCheck = allTrainees.stream()
                .filter(t -> t.getUser().getUsername().equals("user2"))
                .findFirst();

        assertTrue(updated.isPresent());
        assertEquals("John Updated", updated.get().getUser().getFirstName());
        assertTrue(isDeleted);
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
        dao.delete(1L);

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
}
