package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.DirectorStorage;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class DirectorDbStorage implements DirectorStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Director> get() {
        String queryDirectorSelect = "SELECT * " +
                "FROM director;";

        return jdbcTemplate.query(queryDirectorSelect, (rs, rowNum) -> makeDirector(rs));
    }

    @Override
    public Director getById(int id) {
        return null;
    }

    @Override
    public Director create(Director director) {
        return null;
    }

    @Override
    public Director udpate(Director director) {
        return null;
    }

    @Override
    public void delete(Director director) {

    }

    private Director makeDirector(ResultSet rs) throws SQLException {
        return new Director(
                rs.getInt("director_id"),
                rs.getString("name")
        );
    }
}
