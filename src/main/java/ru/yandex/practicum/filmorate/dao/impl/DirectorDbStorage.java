package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.DirectorStorage;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

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
    public Optional<Director> getById(int id) {
        String queryDirectorSelect = "SELECT * " +
                "FROM director " +
                "WHERE director_id = ?;";

        log.info("Получение режиссера с id = {}.", id);

        try {
            return Optional.of(jdbcTemplate.queryForObject(queryDirectorSelect, (rs, rowNum) -> makeDirector(rs), id));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    @Override
    public Director create(Director director) {
        String queryDirectorInsert = "INSERT INTO director(name) " +
                "VALUES(?);";

        log.info("Добавление режиссера {}.", director.getName());

        KeyHolder keyHolder = new GeneratedKeyHolder();
        int updatedRowsCount = jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(queryDirectorInsert, new String[]{"director_id"});
            stmt.setString(1, director.getName());
            return stmt;
        }, keyHolder);

        if (updatedRowsCount == 0) {
            log.info("Произошла ошибка при добавлении режиссера {} в базу данных", director);
            return null;
        }

        int directorId = (int) keyHolder.getKey().longValue();

        Director createdDirector = getById(directorId).orElse(null);
        log.info("Режиссер {} добавлен в базу данных", createdDirector);
        return createdDirector;
    }

    @Override
    public Optional<Director> udpate(Director director) {
        String queryDirectorUpdate = "UPDATE director " +
                "SET name = ? " +
                "WHERE director_id = ?;";

        log.info("Обновление режиссера с id = {}.", director.getId());

        jdbcTemplate.update(queryDirectorUpdate, director.getName(), director.getId());
        return getById(director.getId());
    }

    @Override
    public void delete(int id) {
        String queryDirectorDelete = "DELETE FROM director " +
                "WHERE director_id = ?;";

        log.info("Удаление режиссера с id = {}.", id);
        jdbcTemplate.update(queryDirectorDelete, id);
    }

    private Director makeDirector(ResultSet rs) throws SQLException {
        return new Director(
                rs.getInt("director_id"),
                rs.getString("name")
        );
    }
}
