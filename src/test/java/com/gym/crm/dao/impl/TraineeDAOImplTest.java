package com.gym.crm.dao.impl;

import com.gym.crm.exception.TransactionHandlerException;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.test.context.ContextConfiguration;

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
@ContextConfiguration(classes = {TraineeDAOImpl.class})
class TraineeDAOImplTest extends BaseIntegrationTest<TraineeDAOImpl> {
    @Test
    void testCreate_ShouldPersistTraineeSuccessfully() {
        Trainee trainee = createSampleTrainee();

        Trainee savedTrainee = dao.create(trainee);

        assertNotNull(savedTrainee);
        assertNotNull(savedTrainee.getId());
        assertNotNull(savedTrainee.getUser().getId());
        assertEquals("John", savedTrainee.getUser().getFirstName());
        assertEquals("Doe", savedTrainee.getUser().getLastName());
        assertEquals("john.doe", savedTrainee.getUser().getUsername());
        assertEquals(LocalDate.of(1990, 1, 1), savedTrainee.getDateOfBirth());
        assertEquals("123 Main St", savedTrainee.getAddress());
        assertTrue(savedTrainee.getUser().getIsActive());
    }

    @Test
    void testCreate_ShouldPersistTraineeWithNullAddress() {
        User user = User.builder()
                .firstName("John")
                .lastName("Doe")
                .username("john.doe")
                .password("password123")
                .isActive(false)
                .build();

        Trainee trainee = Trainee.builder()
                .user(user)
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .address(null)
                .build();

        Trainee savedTrainee = dao.create(trainee);

        assertNotNull(savedTrainee);
        assertNotNull(savedTrainee.getId());
        assertNull(savedTrainee.getAddress());
        assertFalse(savedTrainee.getUser().getIsActive());
    }

    @Test
    void testFindById_ShouldReturnTraineeWhenExists() {
        Trainee trainee = createSampleTrainee();
        Trainee savedTrainee = dao.create(trainee);

        Optional<Trainee> foundTrainee = dao.findById(savedTrainee.getId());

        assertTrue(foundTrainee.isPresent());
        assertEquals(savedTrainee.getId(), foundTrainee.get().getId());
        assertEquals(savedTrainee.getUser().getFirstName(), foundTrainee.get().getUser().getFirstName());
        assertEquals(savedTrainee.getUser().getLastName(), foundTrainee.get().getUser().getLastName());
        assertEquals(savedTrainee.getDateOfBirth(), foundTrainee.get().getDateOfBirth());
        assertEquals(savedTrainee.getAddress(), foundTrainee.get().getAddress());
    }

    @Test
    void testFindById_ShouldReturnEmptyWhenNotExists() {
        Long nonExistentId = 999L;

        Optional<Trainee> foundTrainee = dao.findById(nonExistentId);

        assertFalse(foundTrainee.isPresent());
    }

    @Test
    void testFindAll_ShouldReturnAllTrainees() {
        Trainee trainee1 = createSampleTraineeWithUsername("john.doe1");
        Trainee trainee2 = createSampleTraineeWithDetails("Smith", "jane.doe",
                LocalDate.of(1985, 5, 15), "456 Oak Ave");

        dao.create(trainee1);
        dao.create(trainee2);

        List<Trainee> allTrainees = dao.findAll();

        assertNotNull(allTrainees);
        assertEquals(2, allTrainees.size());

        assertTrue(allTrainees.stream()
                .anyMatch(t -> "john.doe1".equals(t.getUser().getUsername())));
        assertTrue(allTrainees.stream()
                .anyMatch(t -> "jane.doe".equals(t.getUser().getUsername())));
    }

    @Test
    void testFindAll_ShouldReturnEmptyListWhenNoTrainees() {
        List<Trainee> allTrainees = dao.findAll();

        assertNotNull(allTrainees);
        assertTrue(allTrainees.isEmpty());
    }

    @Test
    void testUpdate_ShouldUpdateExistingTrainee() {
        Trainee trainee = createSampleTrainee();
        Trainee savedTrainee = dao.create(trainee);

        User updatedUser = savedTrainee.getUser().toBuilder()
                .firstName("John Updated")
                .isActive(false)
                .build();

        Trainee updatedTrainee = savedTrainee.toBuilder()
                .user(updatedUser)
                .address("456 Oak Ave")
                .dateOfBirth(LocalDate.of(1985, 12, 25))
                .build();

        Trainee result = dao.update(updatedTrainee);

        assertNotNull(result);
        assertEquals(savedTrainee.getId(), result.getId());
        assertEquals("John Updated", result.getUser().getFirstName());
        assertFalse(result.getUser().getIsActive());
        assertEquals("456 Oak Ave", result.getAddress());
        assertEquals(LocalDate.of(1985, 12, 25), result.getDateOfBirth());

        Optional<Trainee> persistedTrainee = dao.findById(savedTrainee.getId());
        assertTrue(persistedTrainee.isPresent());
        assertEquals("John Updated", persistedTrainee.get().getUser().getFirstName());
        assertFalse(persistedTrainee.get().getUser().getIsActive());
        assertEquals("456 Oak Ave", persistedTrainee.get().getAddress());
    }

    @Test
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

        TransactionHandlerException exception = assertThrows(TransactionHandlerException.class,
                () -> dao.update(nonExistentTrainee));

        assertEquals("Error performing Hibernate operation. Transaction is rolled back", exception.getMessage());
    }

    @Test
    void testDelete_ShouldReturnTrueWhenTraineeExists() {
        Trainee trainee = createSampleTrainee();
        Trainee savedTrainee = dao.create(trainee);

        boolean result = dao.delete(savedTrainee.getId());

        assertTrue(result);

        Optional<Trainee> deletedTrainee = dao.findById(savedTrainee.getId());
        assertFalse(deletedTrainee.isPresent());
    }

    @Test
    void testDelete_ShouldReturnFalseWhenTraineeNotExists() {
        Long nonExistentId = 999L;

        boolean result = dao.delete(nonExistentId);

        assertFalse(result);
    }

    @Test
    void testConcurrentOperations_ShouldHandleMultipleTrainees() {
        Trainee trainee1 = createSampleTraineeWithUsername("user1");
        Trainee trainee2 = createSampleTraineeWithDetails("Doe", "user2",
                LocalDate.of(1990, 1, 1), "123 Main St");

        Trainee saved1 = dao.create(trainee1);
        Trainee saved2 = dao.create(trainee2);

        User updatedUser = saved1.getUser().toBuilder()
                .firstName("John Updated")
                .build();

        dao.update(saved1.toBuilder()
                .user(updatedUser)
                .build());

        boolean deleted = dao.delete(saved2.getId());

        List<Trainee> allTrainees = dao.findAll();
        assertEquals(1, allTrainees.size());
        assertEquals("John Updated", allTrainees.getFirst().getUser().getFirstName());
        assertEquals("user1", allTrainees.getFirst().getUser().getUsername());
        assertTrue(deleted);
    }

    @Test
    void testDatabaseConstraints_ShouldEnforceUniqueUsername() {
        Trainee trainee1 = createSampleTraineeWithUsername("duplicate.user");
        Trainee trainee2 = createSampleTraineeWithUsername("duplicate.user");

        dao.create(trainee1);

        assertThrows(Exception.class, () -> dao.create(trainee2));
    }

    private Trainee createSampleTrainee() {
        User user = User.builder()
                .firstName("John")
                .lastName("Doe")
                .username("john.doe")
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

    private Trainee createSampleTraineeWithDetails(String lastName, String username,
                                                   LocalDate dateOfBirth, String address) {
        User user = User.builder()
                .firstName("Jane")
                .lastName(lastName)
                .username(username)
                .password("password123")
                .isActive(true)
                .build();

        return Trainee.builder()
                .user(user)
                .dateOfBirth(dateOfBirth)
                .address(address)
                .build();
    }
}
