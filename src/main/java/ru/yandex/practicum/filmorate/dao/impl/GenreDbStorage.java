package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.GenreStorage;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component("genreDbStorage")
@Slf4j
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<Genre> get() {
        String sqlQuery =
                "SELECT *" +
                        "FROM genre";

        return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeGenre(rs));
    }

    @Override
    public Optional<Genre> getById(int id) {
        String sqlQuery =
                "SELECT * " +
                        "FROM genre " +
                        "WHERE genre_id = ?";
        List<Genre> genres = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeGenre(rs), id);

        // обрабатываем результат выполнения запроса
        if (genres.isEmpty()) {
            log.info("Жанр с идентификатором {} не найден.", id);
            return Optional.empty();
        }
        log.info("Найден жанр: {}", genres.get(0));
        return Optional.of(genres.get(0));
    }

    private Genre makeGenre(ResultSet rs) throws SQLException {
        return new Genre(rs.getInt("genre_id"), rs.getString("name"));
    }
}
