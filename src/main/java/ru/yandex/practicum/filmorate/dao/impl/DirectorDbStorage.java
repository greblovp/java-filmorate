package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.DirectorStorage;
import ru.yandex.practicum.filmorate.exception.DirectorNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component("directorDbStorage")
@Slf4j
@RequiredArgsConstructor
public class DirectorDbStorage implements DirectorStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Director> get() {
        String queryDirectorSelect = "SELECT * " +
                "FROM director;";

        log.info("Получение всех режиссеров.");
        return jdbcTemplate.query(queryDirectorSelect, (rs, rowNum) -> makeDirector(rs));
    }

    @Override
    public Director getById(int id) {
        String queryDirectorSelect = "SELECT * " +
                "FROM director " +
                "WHERE director_id = ?;";

        log.info("Получение режиссера с id = {}.", id);
        checkIfDirectorExists(id);
        return jdbcTemplate.queryForObject(queryDirectorSelect, (rs, rowNum) -> makeDirector(rs), id);
    }

    @Override
    public Director create(Director director) {
        String queryDirectorInsert = "INSERT INTO director(name) " +
                "VALUES(?);";

        log.info("Добавление режиссера {}.", director.getName());
        jdbcTemplate.update(queryDirectorInsert, director.getName());

        String queryDirectorSelect = "SELECT * " +
                "FROM director " +
                "WHERE name = ?;";

        return jdbcTemplate.queryForObject(queryDirectorSelect, (rs, rowNum) -> makeDirector(rs), director.getName());
    }

    @Override
    public Director udpate(Director director) {
        String queryDirectorUpdate = "UPDATE director " +
                "SET name = ? " +
                "WHERE director_id = ?;";

        log.info("Обновление режиссера с id = {}.", director.getId());
        checkIfDirectorExists(director.getId());
        jdbcTemplate.update(queryDirectorUpdate, director.getName(), director.getId());

        String queryDirectorSelect = "SELECT * " +
                "FROM director " +
                "WHERE director_id = ?;";

        return jdbcTemplate.queryForObject(queryDirectorSelect, (rs, rowNum) -> makeDirector(rs), director.getId());
    }

    @Override
    public void delete(int id) {
        String queryDirectorDelete = "DELETE FROM director " +
                "WHERE director_id = ?;";

        log.info("Удаление режиссера с id = {}.", id);
        checkIfDirectorExists(id);
        jdbcTemplate.update(queryDirectorDelete, id);
    }

    private Director makeDirector(ResultSet rs) throws SQLException {
        return new Director(
                rs.getInt("director_id"),
                rs.getString("name")
        );
    }

    public void checkIfDirectorExists(int id) {
        String queryDirectorSelect = "SELECT * " +
                "FROM director " +
                "WHERE director_id = ?;";

        try {
            jdbcTemplate.queryForObject(queryDirectorSelect, (rs, rowNum) -> makeDirector(rs), id);
        } catch (RuntimeException e) {
            throw new DirectorNotFoundException("Режиссер с id = " + id + " отсутствует в БД.");
        }
    }
}
