package com.gym.crm.dao.impl;

import com.gym.crm.exception.TransactionHandlerException;
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
    void testCreate_ShouldPersistTraineeSuccessfully() {
        Trainee sample = createSampleTrainee();
        Trainee actual = dao.create(sample);

        assertNotNull(actual.getId());
        assertEquals("Alex", actual.getUser().getFirstName());

        List<Trainee> all = dao.findAll();
        assertTrue(all.stream().anyMatch(t -> t.getId().equals(actual.getId())));
    }

    @Test
    void testCreate_ShouldPersistTraineeWithNullAddress() {
        Trainee t = Trainee.builder()
                .user(User.builder()
                        .firstName("John")
                        .lastName("Doe")
                        .username("john.doe")
                        .password("pass")
                        .isActive(false)
                        .build())
                .dateOfBirth(LocalDate.of(1990,1,1))
                .address(null)
                .build();

        Trainee actual = dao.create(t);
        assertNotNull(actual.getId());
        assertNull(actual.getAddress());
        assertFalse(actual.getUser().getIsActive());
    }

    @Test
    void testFindById_ShouldReturnTraineeWhenExists() {
        Long existingId = 1L;

        Optional<Trainee> found = dao.findById(existingId);

        assertTrue(found.isPresent());
        Trainee t = found.get();

        assertEquals(LocalDate.of(1995, 3, 15), t.getDateOfBirth());
        assertEquals("123 Main St, New York, NY 10001", t.getAddress());
        assertEquals("Emma", t.getUser().getFirstName());
        assertEquals("Miller", t.getUser().getLastName());
        assertEquals("emma.miller", t.getUser().getUsername());
    }

    @Test
    void testFindById_ShouldReturnEmptyWhenNotExists() {
        Long nonExistentId = 999L;

        Optional<Trainee> foundTrainee = dao.findById(nonExistentId);

        assertFalse(foundTrainee.isPresent());
    }

    @Test
    void testFindAll_ShouldReturnInitialTraineesFromLiquibase() {
        List<Trainee> allTrainees = dao.findAll();

        assertNotNull(allTrainees);
        assertEquals(5, allTrainees.size());

        List<String> expectedUsernames = List.of(
                "emma.miller", "james.garcia", "olivia.martinez",
                "william.anderson", "sophia.taylor"
        );

        List<String> actualUsernames = allTrainees.stream()
                .map(t -> t.getUser().getUsername())
                .toList();

        assertTrue(actualUsernames.containsAll(expectedUsernames));
    }

    @Test
    void testUpdate_ShouldUpdateExistingTrainee() {
        Long idToUpdate = 2L;
        Trainee original = dao.findById(idToUpdate).orElseThrow();

        User updatedUser = original.getUser().toBuilder()
                .firstName("UpdatedFirst")
                .lastName("UpdatedLast")
                .isActive(false)
                .build();

        Trainee modified = original.toBuilder()
                .user(updatedUser)
                .address("Updated Address 456")
                .dateOfBirth(LocalDate.of(1980, 12, 31))
                .build();

        Trainee result = dao.update(modified);

        assertEquals(idToUpdate, result.getId());
        assertEquals("UpdatedFirst", result.getUser().getFirstName());
        assertEquals("UpdatedLast",  result.getUser().getLastName());
        assertFalse(result.getUser().getIsActive());
        assertEquals("Updated Address 456", result.getAddress());
        assertEquals(LocalDate.of(1980, 12, 31), result.getDateOfBirth());
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
        boolean deleted = dao.delete(1L);
        assertTrue(deleted);

        Optional<Trainee> stillThere = dao.findById(1L);
        assertFalse(stillThere.isPresent());
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
        Trainee trainee2 = createSampleTraineeWithDetails(
                LocalDate.of(1990, 1, 1));

        Trainee saved1 = dao.create(trainee1);
        Trainee saved2 = dao.create(trainee2);

        User updatedUser = saved1.getUser().toBuilder()
                .firstName("John Updated")
                .build();

        dao.update(saved1.toBuilder().user(updatedUser).build());
        boolean deleted = dao.delete(saved2.getId());

        List<Trainee> allTrainees = dao.findAll();

        Optional<Trainee> updated = allTrainees.stream()
                .filter(t -> t.getUser().getUsername().equals("user1"))
                .findFirst();

        Optional<Trainee> deletedCheck = allTrainees.stream()
                .filter(t -> t.getUser().getUsername().equals("user2"))
                .findFirst();

        assertTrue(updated.isPresent());
        assertEquals("John Updated", updated.get().getUser().getFirstName());
        assertTrue(deleted);
        assertFalse(deletedCheck.isPresent());
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
