package ru.yandex.practicum.filmorate.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.dao.UserStorage;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    @Qualifier("userDbStorage")
    @NonNull
    private final UserStorage userStorage;

    public Collection<User> findAll() {
        return userStorage.get();
    }

    public User findById(int userId) {
        return checkUserId(userId);
    }

    public Collection<User> getFriends(int userId) {
        User user = checkUserId(userId);
        Set<Integer> friendIds = user.getFriends();

        return friendIds.stream()
                .map(this::checkUserId)
                .collect(Collectors.toList());
    }

    public User createUser(User user) {
        user.setFriends(new HashSet<>());
        return userStorage.create(user);
    }

    public User updateUser(User user) {
        if (user.getFriends() == null) {
            user.setFriends(new HashSet<>());
        }
        return userStorage.update(user).orElseThrow(()
                -> new UserNotFoundException("Пользователь с ID = " + user.getId() + " не найден."));
    }

    public void addFriend(int userId, int friendId) {
        User user = checkUserId(userId);
        User friend = checkUserId(friendId);

        userStorage.addFriend(user, friend);

        log.debug("Добавлен в друзья пользователь ID = {} пользователю: {}", friendId, user);
    }

    public void removeFriend(int userId, int friendId) {
        User user = checkUserId(userId);
        User friend = checkUserId(friendId);

        userStorage.removeFriend(user, friend);

        log.debug("Удален из друзей пользователь ID = {} у пользователя: {}", friendId, user);
    }

    public Collection<User> getCommonFriends(int userId, int otherUserId) {
        User user = checkUserId(userId);
        User otherUser = checkUserId(otherUserId);

        Set<Integer> commonFriendIds = new HashSet<>(user.getFriends());
        Set<Integer> otherUserFriendIds = otherUser.getFriends();

        commonFriendIds.retainAll(otherUserFriendIds);

        return commonFriendIds.stream()
                .map(this::checkUserId)
                .collect(Collectors.toList());
    }

    public void removeUser(int userId) {
        User user = checkUserId(userId);

        userStorage.removeUser(userId);

        log.debug("Удален пользователь {}", user);
    }

    private User checkUserId(int id) {
        return userStorage.getById(id).orElseThrow(()
                -> new UserNotFoundException("Пользователь с ID = " + id + " не найден."));
    }
}
