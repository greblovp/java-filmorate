package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.MPAStorage;
import ru.yandex.practicum.filmorate.model.MPA;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component("mpaDbStorage")
@Slf4j
@RequiredArgsConstructor
public class MPADbStorage implements MPAStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<MPA> get() {
        String sqlQuery =
                "SELECT *" +
                        "FROM rating";

        return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeMPA(rs));
    }

    @Override
    public Optional<MPA> getById(int id) {
        String sqlQuery =
                "SELECT * " +
                        "FROM rating " +
                        "WHERE rating_id = ?";
        List<MPA> ratings = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeMPA(rs), id);

        // обрабатываем результат выполнения запроса
        if (ratings.isEmpty()) {
            log.info("Рейтинг с идентификатором {} не найден.", id);
            return Optional.empty();
        }
        log.info("Найден рейтинг: {}", ratings.get(0));
        return Optional.of(ratings.get(0));
    }

    private MPA makeMPA(ResultSet rs) throws SQLException {
        return new MPA(rs.getInt("rating_id"), rs.getString("name"));
    }
}
