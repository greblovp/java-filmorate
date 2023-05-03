package ru.yandex.practicum.filmorate.dao.impl;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.UserStorage;
import ru.yandex.practicum.filmorate.model.User;

@Component("inMemoryUserStorage")
public class InMemoryUserStorage extends BaseModelStorage<User> implements UserStorage {
    @Override
    public void addFriend(User user, User friend) {
        user.getFriends().add(friend.getId());
    }

    @Override
    public void removeFriend(User user, User friend) {
        user.getFriends().remove(friend.getId());
    }

    @Override
    @Deprecated
    public void removeUser(int userId) {
        throw new UnsupportedOperationException("Реализация метода существует только при работе с БД");
    }
}
