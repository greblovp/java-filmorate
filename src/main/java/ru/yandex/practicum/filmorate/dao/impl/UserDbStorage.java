package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.UserStorage;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Component("userDbStorage")
@Slf4j
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<User> get() {
        String sqlQuery =
                "SELECT * " +
                        "FROM \"user\" ";

        return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeUser(rs));
    }

    @Override
    public Optional<User> getById(int id) {
        String sqlQuery =
                "SELECT * " +
                        "FROM \"user\" " +
                        "WHERE user_id = ?";
        List<User> users = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeUser(rs), id);

        // обрабатываем результат выполнения запроса
        if (users.isEmpty()) {
            log.info("Пользователь с идентификатором {} не найден.", id);
            return Optional.empty();
        }
        log.info("Найден пользователь: {}", users.get(0));
        return Optional.of(users.get(0));
    }

    @Override
    public User create(User user) {
        String userSqlQuery =
                "INSERT INTO \"user\" (email, login, name, birth_dt) " +
                        "VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        int updatedRowsCount = jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(userSqlQuery, new String[]{"user_id"});
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getName());
            stmt.setDate(4, Date.valueOf(user.getBirthday()));
            return stmt;
        }, keyHolder);

        if (updatedRowsCount == 0) {
            log.info("Произошла ошибка при добавлении пользователя {} в базу данных", user);
            return null;
        }

        int userId = (int) keyHolder.getKey().longValue();

        User createdUser = getById(userId).orElse(null);

        log.info("Пользователь {} добавлен в базу данных. Присвоен идентификатор {}", createdUser, userId);
        return createdUser;
    }

    @Override
    public Optional<User> update(User user) {
        String userSqlQuery =
                "UPDATE \"user\" " +
                        "SET email = ?, login = ?, name = ?, birth_dt = ? " +
                        "WHERE user_id = ?";
        int updatedRowsCount = jdbcTemplate.update(userSqlQuery,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId());

        if (updatedRowsCount == 0) {
            log.info("Пользователь с идентификатором {} не найден.", user.getId());
            return Optional.empty();
        }

        Optional<User> updatedUser = getById(user.getId());
        log.info("Пользователь {} обновлен в базе данных", updatedUser);

        return updatedUser;
    }

    @Override
    public void addFriend(User user, User friend) {
        String sqlQuery =
                "MERGE INTO friend (user_id, friend_user_id) " +
                        "VALUES (?, ?)";
        jdbcTemplate.update(sqlQuery, user.getId(), friend.getId());
    }

    @Override
    public void removeFriend(User user, User friend) {
        String sqlQuery =
                "DELETE FROM friend " +
                        "WHERE user_id = ? AND friend_user_id = ?";
        jdbcTemplate.update(sqlQuery, user.getId(), friend.getId());
    }

    @Override
    public void removeUser(int userId) {
        String sqlQuery =
                "DELETE FROM \"user\" " +
                        "WHERE user_id = ?";
        jdbcTemplate.update(sqlQuery, userId);
    }

    private User makeUser(ResultSet rs) throws SQLException {
        int userId = rs.getInt("user_id");
        User user = User.builder()
                .name(rs.getString("name"))
                .email(rs.getString("email"))
                .login(rs.getString("login"))
                .birthday(rs.getDate("birth_dt").toLocalDate())
                .friends(getFriendsByUserId(userId))
                .build();
        user.setId(userId);
        return user;
    }

    private HashSet<Integer> getFriendsByUserId(int userId) {
        String sql =
                "SELECT friend_user_id " +
                        "FROM friend " +
                        "WHERE user_id = ?";
        List<Integer> friends = jdbcTemplate.queryForList(sql, Integer.class, userId);
        return new HashSet<>(friends);
    }
}
