package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.yandex.practicum.filmorate.dao.UserStorage;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {


    private final UserStorage userStorage = Mockito.mock(UserStorage.class);

    private final UserService userService = new UserService(userStorage);

    private User user = User.builder()
            .email("test1@test.test")
            .login("login1")
            .name("name1")
            .birthday(LocalDate.of(2000, 1, 1))
            .build();

    private User otherUser = User.builder()
            .email("test2@test.test")
            .login("login2")
            .name("name2")
            .birthday(LocalDate.of(2005, 1, 1))
            .build();

    private final int userId = 1;
    private final int otherUserId = 10;
    private final int commonFriendId = 100;
    private final int otherFriendId = 1000;

    @BeforeEach
    void setFriends() {
        user.setFriends(new HashSet<>());
        otherUser.setFriends(new HashSet<>());
    }

    @Test
    void addFriendWhenUserIsNull() {
        when(userStorage.getById(userId)).thenReturn(Optional.empty());
        when(userStorage.getById(otherUserId)).thenReturn(Optional.of(otherUser));

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> userService.addFriend(userId, otherUserId));
        verify(userStorage).getById(userId);
        assertEquals(exception.getMessage(), "Пользователь с ID = " + userId + " не найден.");
    }

    @Test
    void addFriendWhenOtherUserIsNull() {
        when(userStorage.getById(userId)).thenReturn(Optional.of(user));
        when(userStorage.getById(otherUserId)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> userService.addFriend(userId, otherUserId));
        verify(userStorage).getById(userId);
        verify(userStorage).getById(otherUserId);
        assertEquals(exception.getMessage(), "Пользователь с ID = " + otherUserId + " не найден.");
    }

    @Test
    void addFriend() {
        when(userStorage.getById(userId)).thenReturn(Optional.of(user));
        when(userStorage.getById(otherUserId)).thenReturn(Optional.of(otherUser));

        userService.addFriend(userId, otherUserId);

        verify(userStorage).getById(userId);
        verify(userStorage).getById(otherUserId);
        verify(userStorage).addFriend(user, otherUser);
    }

    @Test
    void removeFriendWhenUserIsNull() {
        when(userStorage.getById(userId)).thenReturn(Optional.empty());
        when(userStorage.getById(otherUserId)).thenReturn(Optional.of(otherUser));

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> userService.removeFriend(userId, otherUserId));
        verify(userStorage).getById(userId);
        assertEquals(exception.getMessage(), "Пользователь с ID = " + userId + " не найден.");
    }

    @Test
    void removeFriendWhenOtherUserIsNull() {
        when(userStorage.getById(userId)).thenReturn(Optional.of(user));
        when(userStorage.getById(otherUserId)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> userService.removeFriend(userId, otherUserId));
        verify(userStorage).getById(userId);
        verify(userStorage).getById(otherUserId);
        assertEquals(exception.getMessage(), "Пользователь с ID = " + otherUserId + " не найден.");
    }

    @Test
    void removeFriendWhenZeroFriends() {
        when(userStorage.getById(userId)).thenReturn(Optional.of(user));
        when(userStorage.getById(otherUserId)).thenReturn(Optional.of(otherUser));

        userService.removeFriend(userId, otherUserId);

        verify(userStorage).getById(userId);
        verify(userStorage).getById(otherUserId);
        assertEquals(0, user.getFriends().size());
    }

    @Test
    void removeFriend() {
        user.getFriends().add(otherUserId);
        when(userStorage.getById(userId)).thenReturn(Optional.of(user));
        when(userStorage.getById(otherUserId)).thenReturn(Optional.of(otherUser));

        userService.removeFriend(userId, otherUserId);

        verify(userStorage).getById(userId);
        verify(userStorage).getById(otherUserId);
        verify(userStorage).removeFriend(user, otherUser);
    }

    @Test
    void getCommonFriends() {
        user.getFriends().add(commonFriendId);
        otherUser.setFriends(Set.of(commonFriendId, otherFriendId));
        when(userStorage.getById(userId)).thenReturn(Optional.of(user));
        when(userStorage.getById(otherUserId)).thenReturn(Optional.of(otherUser));
        when(userStorage.getById(commonFriendId)).thenReturn(Optional.of(user));

        Collection<User> commonFriends = userService.getCommonFriends(userId, otherUserId);

        verify(userStorage).getById(userId);
        verify(userStorage).getById(commonFriendId);
        verify(userStorage, never()).getById(otherFriendId);
        assertEquals(1, commonFriends.size());
        assertEquals(List.of(user), commonFriends);
    }

    @Test
    public void testFindAll() {
        when(userStorage.get()).thenReturn(List.of(user, otherUser));

        Collection<User> result = userService.findAll();

        assertEquals(2, result.size());
        assertEquals(List.of(user, otherUser), result);
        verify(userStorage, times(1)).get();
    }

    @Test
    public void testFindById() {
        int userId = 1;
        when(userStorage.getById(userId)).thenReturn(Optional.of(user));

        User result = userService.findById(userId);

        assertEquals(user, result);
        verify(userStorage, times(1)).getById(userId);
    }

    @Test
    public void testFindByIdNotFound() {
        int userId = 3;
        when(userStorage.getById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.findById(userId));
    }

    @Test
    public void testCreateUser() {
        when(userStorage.create(user)).thenReturn(user);

        User result = userService.createUser(user);

        assertEquals(user, result);
        verify(userStorage, times(1)).create(user);
    }

    @Test
    public void testUpdateUser() {
        when(userStorage.update(user)).thenReturn(Optional.of(user));

        User result = userService.updateUser(user);

        assertEquals(user, result);
        verify(userStorage, times(1)).update(user);
    }

    @Test
    public void testUpdateUserNotFound() {
        when(userStorage.update(user)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.updateUser(user));
    }


    @Test
    public void testGetFriends() {
        when(userStorage.getById(userId)).thenReturn(Optional.of(user));

        Collection<User> friends = userService.getFriends(userId);

        verify(userStorage).getById(userId);
        assertEquals(0, friends.size());
    }

    @Test
    public void testRemoveUser() {
        when(userStorage.getById(userId)).thenReturn(Optional.of(user));

        userService.removeUser(userId);

        verify(userStorage).getById(userId);
        verify(userStorage).removeUser(userId);
    }

    @Test
    public void testRemoveUserWhenUserIsNull() {
        when(userStorage.getById(userId)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> userService.removeUser(userId));
        verify(userStorage).getById(userId);
        assertEquals(exception.getMessage(), "Пользователь с ID = " + userId + " не найден.");
    }
}