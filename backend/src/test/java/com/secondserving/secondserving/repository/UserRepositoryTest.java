package com.secondserving.secondserving.repository;

import com.secondserving.secondserving.config.PostgisTestContainerConfig;
import com.secondserving.secondserving.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import(PostgisTestContainerConfig.class) // Use configured test container
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // We will use the test container...don't let spring use something else
class UserRepositoryTest {

    @Autowired
    private UserRepository underTest;

    @Test
    void findByUsername() {
        // given
        User user = new User(
                "nadif",
                "hashed-password",
                "Nadif Example",
                "nadif@example.com"
        );
        underTest.saveAndFlush(user);

        Optional<User> result = underTest.findByUsername("nadif");

        assertTrue(result.isPresent());
        assertEquals("nadif", result.get().getUsername());
        assertEquals("nadif@example.com", result.get().getEmail());
    }

    @Test
    void findByUsername_returnsEmptyWhenUserDoesNotExist() {
        Optional<User> result = underTest.findByUsername("missing-user");

        assertTrue(result.isEmpty());
    }

    @Test
    void save_generatesUserIdAndTimestamps() {
        User user = new User(
                "alice",
                "hashed-password",
                "Alice Example",
                "alice@example.com"
        );

        User saved = underTest.saveAndFlush(user);

        assertNotNull(saved.getUserId());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
    }

    @Test
    void save_persistsAllFieldsCorrectly() {
        User user = new User(
                "bob",
                "pw-hash-123",
                "Bob Example",
                "bob@example.com"
        );
        underTest.saveAndFlush(user);

        User found = underTest.findByUsername("bob").orElseThrow();

        assertEquals("bob", found.getUsername());
        assertEquals("pw-hash-123", found.getPasswordHash());
        assertEquals("Bob Example", found.getFullName());
        assertEquals("bob@example.com", found.getEmail());
    }

    @Test
    void save_twoUsersWithSameUsername_throwsException() {
        User first = new User(
                "duplicate-user",
                "hash-1",
                "First User",
                "first@example.com"
        );
        User second = new User(
                "duplicate-user",
                "hash-2",
                "Second User",
                "second@example.com"
        );

        underTest.saveAndFlush(first);

        assertThrows(DataIntegrityViolationException.class, () -> {
            underTest.saveAndFlush(second);
        });
    }

    @Test
    void save_twoUsersWithSameEmail_throwsException() {
        User first = new User(
                "first-user",
                "hash-1",
                "First User",
                "same@example.com"
        );
        User second = new User(
                "second-user",
                "hash-2",
                "Second User",
                "same@example.com"
        );

        underTest.saveAndFlush(first);

        assertThrows(DataIntegrityViolationException.class, () -> {
            underTest.saveAndFlush(second);
        });
    }

    @Test
    void findByUsername_returnsCorrectUserWhenMultipleUsersExist() {
        User first = new User(
                "user-one",
                "hash-1",
                "User One",
                "one@example.com"
        );
        User second = new User(
                "user-two",
                "hash-2",
                "User Two",
                "two@example.com"
        );

        underTest.saveAndFlush(first);
        underTest.saveAndFlush(second);

        User result = underTest.findByUsername("user-two").orElseThrow();

        assertEquals("user-two", result.getUsername());
        assertEquals("two@example.com", result.getEmail());
        assertEquals("User Two", result.getFullName());
    }
}