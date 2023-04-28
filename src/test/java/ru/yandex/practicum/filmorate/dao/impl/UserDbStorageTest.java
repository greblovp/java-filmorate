package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.jdbc.SqlGroup;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureTestDatabase
@SqlGroup({
        @Sql(scripts = "classpath:schema.sql", config = @SqlConfig(encoding = "UTF-8"), executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(scripts = "classpath:create_test_data.sql", config = @SqlConfig(encoding = "UTF-8"), executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {

    private final UserDbStorage userDbStorage;


    @Test
    void testGetUsers() {
        Collection<User> users = userDbStorage.get();

        assertEquals(4, users.size());
    }

    @Test
    public void testGetById() {
        Optional<User> userOptional = userDbStorage.getById(1);
        assertTrue(userOptional.isPresent());
        User user = userOptional.get();
        assertEquals(1, user.getId());
        assertEquals("user1@yandex.ru", user.getEmail());
        assertEquals("user1", user.getLogin());
        assertEquals("first user", user.getName());
        assertEquals(LocalDate.of(2000, 1, 1), user.getBirthday());
    }

    @Test
    public void testGetByIdNotFond() {
        Optional<User> userOptional = userDbStorage.getById(999);

        assertTrue(userOptional.isEmpty());
    }

    @Test
    public void testCreateUser() {
        User user = User.builder()
                .email("user5@yandex.ru")
                .login("user5")
                .name("new user")
                .birthday(LocalDate.of(2000, 12, 2))
                .build();
        User createdUser = userDbStorage.create(user);
        assertNotNull(createdUser.getId());
        assertEquals(user.getEmail(), createdUser.getEmail());
        assertEquals(user.getLogin(), createdUser.getLogin());
        assertEquals(user.getName(), createdUser.getName());
        assertEquals(user.getBirthday(), createdUser.getBirthday());
    }

    @Test
    public void testUpdateUser() {
        User user = userDbStorage.getById(1).orElse(null);
        assertNotNull(user);
        user.setName("Updated user");


        Optional<User> updatedUserOptional = userDbStorage.update(user);
        assertTrue(updatedUserOptional.isPresent());
        assertEquals(user.getName(), updatedUserOptional.get().getName());
    }

    @Test
    public void testUpdateUserNotFound() {
        User user = User.builder()
                .email("user5@yandex.ru")
                .login("user5")
                .name("new user")
                .birthday(LocalDate.of(2000, 12, 2))
                .build();
        user.setId(999);


        Optional<User> updatedUserOptional = userDbStorage.update(user);
        assertTrue(updatedUserOptional.isEmpty());
    }

    @Test
    public void testAddFriend() {
        User user1 = userDbStorage.getById(1).orElse(null);
        assertNotNull(user1);

        User user4 = userDbStorage.getById(4).orElse(null);
        assertNotNull(user4);

        userDbStorage.addFriend(user1, user4);
        User userWithNewFriend = userDbStorage.getById(1).orElse(null);
        assertNotNull(userWithNewFriend);

        assertEquals(3, userWithNewFriend.getFriends().size());
        assertTrue(userWithNewFriend.getFriends().contains(4));
    }

    @Test
    public void testRemoveFriend() {
        User user1 = userDbStorage.getById(1).orElse(null);
        assertNotNull(user1);

        User user3 = userDbStorage.getById(3).orElse(null);
        assertNotNull(user3);

        userDbStorage.removeFriend(user1, user3);
        User userWithOutFriend = userDbStorage.getById(1).orElse(null);
        assertNotNull(userWithOutFriend);

        assertEquals(1, userWithOutFriend.getFriends().size());
        assertFalse(userWithOutFriend.getFriends().contains(3));
    }

    @Test
    public void testRemoveUser() {
        User user = userDbStorage.getById(1).orElse(null);
        assertNotNull(user);

        userDbStorage.removeUser(1);
        user = userDbStorage.getById(1).orElse(null);
        assertNull(user);
    }

}