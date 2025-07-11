package com.gym.crm.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class UserCredentialsGeneratorTest {
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String EXPECTED_USERNAME = "John.Doe";
    private static final String EXPECTED_USERNAME_WITH_SUFFIX = "John.Doe1";

    private UserCredentialsGenerator sut;

    @BeforeEach
    void setUp() {
        sut = new UserCredentialsGenerator();
    }

    @ParameterizedTest
    @MethodSource("usernameGenerationProvider")
    void generateUsername_ShouldGenerateExpectedUsername(String firstName, String lastName, List<String> existing, String expectedUsername) {
        String actual = sut.generateUsername(firstName, lastName, existing);

        assertEquals(expectedUsername, actual);
    }

    @Test
    void generateUsername_ShouldHandleLargeNumberOfConflicts() {
        List<String> existingUsernames = List.of(
                "Jane.Smith", "Jane.Smith1", "Jane.Smith2", "Jane.Smith3", "Jane.Smith4",
                "Jane.Smith5", "Jane.Smith6", "Jane.Smith7", "Jane.Smith8", "Jane.Smith9"
        );
        String expected = "Jane.Smith10";

        String actual = sut.generateUsername("Jane", "Smith", existingUsernames);

        assertEquals(expected, actual);
    }

    @Test
    void generateUsername_ShouldWorkWithSpecialCharactersInNames() {
        List<String> existingUsernames = Collections.emptyList();
        String expected = "Jean-Pierre.O'Connor";

        String actual = sut.generateUsername("Jean-Pierre", "O'Connor", existingUsernames);

        assertEquals(expected, actual);
    }

    @Test
    void generateUsername_ShouldHandleEmptyExistingUsernames() {
        List<String> existingUsernames = Collections.emptyList();
        String expected = "Test.User";

        String actual = sut.generateUsername("Test", "User", existingUsernames);

        assertEquals(expected, actual);
    }

    @Test
    void generateUsername_ShouldHandleNullValues() {
        assertThrows(NullPointerException.class, () ->
                sut.generateUsername("Test", "User", null));
    }

    @Test
    void generatePassword_ShouldReturnValidBCryptHash() {
        String hashedPassword = sut.generatePassword();

        assertNotNull(hashedPassword);
        assertFalse(hashedPassword.isBlank());
        assertTrue(hashedPassword.startsWith("$2"));
        assertTrue(hashedPassword.length() >= 60);
    }

    @Test
    void generatePassword_ShouldBeDifferentEachTime() {
        String hash1 = sut.generatePassword();
        String hash2 = sut.generatePassword();
        String hash3 = sut.generatePassword();

        assertNotEquals(hash1, hash2);
        assertNotEquals(hash2, hash3);
        assertNotEquals(hash1, hash3);
    }

    @Test
    void generatePassword_ShouldGenerateDifferentPasswords() {
        String first = sut.generatePassword();
        String second = sut.generatePassword();
        String third = sut.generatePassword();

        assertNotEquals(first, second);
        assertNotEquals(second, third);
        assertNotEquals(first, third);
    }

    @Test
    void generatePassword_ShouldNotReturnNullOrEmpty() {
        String actual = sut.generatePassword();

        assertNotNull(actual);
        assertFalse(actual.isEmpty());
        assertFalse(actual.isBlank());
    }

    @Test
    void generatePassword_ShouldUseAllAvailableCharacters() {
        String actual = sut.generatePassword();

        assertAll("Password must contain required character types",
                () -> assertTrue(actual.chars().anyMatch(Character::isUpperCase), "Must contain uppercase letter"),
                () -> assertTrue(actual.chars().anyMatch(Character::isLowerCase), "Must contain lowercase letter"),
                () -> assertTrue(actual.chars().anyMatch(Character::isDigit), "Must contain digit"));
    }

    @Test
    void generateUsername_ShouldHandleVeryLongNames() {
        String firstName = "VeryLongFirstNameThatExceedsNormalLength";
        String lastName = "VeryLongLastNameThatExceedsNormalLength";
        List<String> existingUsernames = Collections.emptyList();
        String expected = firstName + "." + lastName;

        String actual = sut.generateUsername(firstName, lastName, existingUsernames);

        assertEquals(expected, actual);
    }

    @Test
    void generateUsername_ShouldBeConsistentForSameInputs() {
        List<String> existingUsernames = List.of("John.Doe", "John.Doe1");

        String result1 = sut.generateUsername(FIRST_NAME, LAST_NAME, existingUsernames);
        String result2 = sut.generateUsername(FIRST_NAME, LAST_NAME, existingUsernames);

        assertEquals(result1, result2);
        assertEquals("John.Doe2", result1);
    }

    static Stream<Arguments> usernameGenerationProvider() {
        return Stream.of(
                Arguments.of(FIRST_NAME, LAST_NAME, List.of(), EXPECTED_USERNAME),
                Arguments.of(FIRST_NAME, LAST_NAME, List.of(EXPECTED_USERNAME), EXPECTED_USERNAME_WITH_SUFFIX),
                Arguments.of(FIRST_NAME, LAST_NAME, List.of(EXPECTED_USERNAME, EXPECTED_USERNAME_WITH_SUFFIX, "John.Doe2"), "John.Doe3")
        );
    }
}
