package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.DirectorStorage;
import ru.yandex.practicum.filmorate.dao.FilmStorage;
import ru.yandex.practicum.filmorate.dao.GenreStorage;
import ru.yandex.practicum.filmorate.dao.MPAStorage;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Component("filmDbStorage")
@Slf4j
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final GenreStorage genreStorage;
    private final MPAStorage mpaStorage;
    private final DirectorStorage directorStorage;

    @Override
    public Collection<Film> get() {
        String sqlQuery =
                "SELECT *" +
                        "FROM film";

        return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeFilm(rs));
    }

    @Override
    public Optional<Film> getById(int id) {
        String sqlQuery =
                "SELECT * " +
                        "FROM film " +
                        "WHERE film_id = ?";
        List<Film> films = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeFilm(rs), id);

        // обрабатываем результат выполнения запроса
        if (films.isEmpty()) {
            log.info("Фильм с идентификатором {} не найден.", id);
            return Optional.empty();
        }
        log.info("Найден фильм: {}", films.get(0));
        return Optional.of(films.get(0));
    }

    @Override
    public Film create(Film film) {
        String filmSqlQuery =
                "INSERT INTO film (name, description, release_dt, duration, rating_id) " +
                        "VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        int updatedRowsCount = jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(filmSqlQuery, new String[]{"film_id"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setDate(3, Date.valueOf(film.getReleaseDate()));
            stmt.setInt(4, film.getDuration());
            stmt.setInt(5, film.getMpa().getId());
            return stmt;
        }, keyHolder);

        if (updatedRowsCount == 0) {
            log.info("Произошла ошибка при добавлении фильма {} в базу данных", film);
            return null;
        }

        int filmId = (int) keyHolder.getKey().longValue();

        if (film.getGenres() != null) {
            String genreSqlQuery =
                    "INSERT INTO film_x_genre (film_id, genre_id) " +
                            "VALUES (?, ?)";

            film.getGenres().forEach(genre -> {
                jdbcTemplate.update(genreSqlQuery,
                        filmId,
                        genre.getId());
            });
        }

        if (film.getDirectors() != null) {
            String queryDirectorInsert = "INSERT INTO film_x_director(film_id, director_id) " +
                    "VALUES(?, ?);";

            film.getDirectors().forEach(director -> {
                jdbcTemplate.update(queryDirectorInsert, filmId, director.getId());
            });
        }

        Film createdFilm = getById(filmId).orElse(null);
        log.info("Фильм {} добавлен в базу данных", createdFilm);
        return createdFilm;
    }

    @Override
    public Optional<Film> update(Film film) {
        String filmSqlQuery =
                "UPDATE film " +
                        "SET name = ?, description = ?, release_dt = ?, duration = ?, rating_id = ? " +
                        "WHERE film_id = ?";
        int updatedRowsCount = jdbcTemplate.update(filmSqlQuery,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        if (updatedRowsCount == 0) {
            log.info("Фильм с идентификатором {} не найден.", film.getId());
            return Optional.empty();
        }

        String genreDeleteSqlQuery =
                "DELETE FROM film_x_genre " +
                        "WHERE film_id = ?";

        jdbcTemplate.update(genreDeleteSqlQuery, film.getId());

        if (film.getGenres() != null) {
            String genreSqlQuery =
                    "INSERT INTO film_x_genre (film_id, genre_id) " +
                            "VALUES (?, ?)";

            film.getGenres().forEach(genre -> {
                jdbcTemplate.update(genreSqlQuery,
                        film.getId(),
                        genre.getId());
            });
        }

        String queryFilmDirectorDelete = "DELETE FROM film_x_director " +
                "WHERE film_id = ?;";

        jdbcTemplate.update(queryFilmDirectorDelete, film.getId());

        if (film.getDirectors() != null) {
            String queryFilmDirectorInsert = "INSERT INTO film_x_director(film_id, director_id) " +
                    "VALUES(?, ?);";

            film.getDirectors().forEach(director -> {
                jdbcTemplate.update(queryFilmDirectorInsert, film.getId(), director.getId());
            });
        }

        Optional<Film> updatedFilm = this.getById(film.getId());
        log.info("Фильм {} обновлен в базе данных", updatedFilm);
        return updatedFilm;
    }

    @Override
    public void addLike(Film film, User user) {
        String sqlQuery =
                "MERGE INTO film_like (film_id, user_id) " +
                        "VALUES (?, ?)";
        jdbcTemplate.update(sqlQuery, film.getId(), user.getId());
    }

    @Override
    public void removeLike(Film film, User user) {
        String sqlQuery =
                "DELETE FROM film_like " +
                        "WHERE film_id = ? AND user_id = ?";

        jdbcTemplate.update(sqlQuery, film.getId(), user.getId());
    }

    @Override
    public Collection<Film> getFilmsByDirector(int directorId, String sortBy) {
        directorStorage.checkIfDirectorExists(directorId);
        String queryFilmsSelect = "SELECT * " +
                "FROM film AS f " +
                "LEFT JOIN film_x_director AS fd ON f.film_id = fd.film_id " +
                "LEFT JOIN director AS d ON fd.director_id = d.director_id " +
                "WHERE d.director_id = ?;";

        List<Film> allFilmsByDirector = jdbcTemplate.query(queryFilmsSelect, (rs, rowNum) -> makeFilm(rs), directorId);

        if (sortBy.equals("year")) {
            Collections.sort(allFilmsByDirector,
                    (film1, film2) -> {
                        if (film1.getReleaseDate().isAfter(film2.getReleaseDate())) {
                            return 1;
                        } else {
                            return -1;
                        }
                    });
        } else {
            Collections.sort(allFilmsByDirector,
                    Comparator.comparingInt(film -> film.getLikes().size()));
        }

        return allFilmsByDirector;
    }

    public Collection<Film> getPopularByGenreAndYear(int count, int genreId, int year) {
        String sqlQuery;
        List<Film> films;

        if (genreId == 0) {
            sqlQuery = "SELECT f.film_id, f.name, f.description, f.release_dt, f.duration, f.rating_id, " +
                    "COUNT(fl.user_id) as likes " +
                    "FROM film f " +
                    "LEFT JOIN film_like fl ON f.film_id=fl.film_id " +
                    "WHERE EXTRACT (year FROM f.release_dt) = ? " +
                    "GROUP BY f.film_id, f.name, f.description, f.release_dt, f.duration, f.rating_id " +
                    "ORDER BY likes DESC, film_id " +
                    "LIMIT ?";
            films = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeFilm(rs), year, count);

        } else if(year == 0) {
            sqlQuery = "SELECT f.film_id, f.name, f.description, f.release_dt, f.duration, f.rating_id, " +
                    "COUNT(fl.user_id) as likes " +
                    "FROM film f " +
                    "LEFT JOIN film_like fl ON f.film_id=fl.film_id " +
                    "JOIN film_x_genre fg ON f.film_id=fg.film_id " +
                    "WHERE fg.genre_id = ? " +
                    "GROUP BY f.film_id, f.name, f.description, f.release_dt, f.duration, f.rating_id " +
                    "ORDER BY likes DESC, film_id " +
                    "LIMIT ?";
            films = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeFilm(rs), genreId, count);

        } else {
            sqlQuery = "SELECT f.film_id, f.name, f.description, f.release_dt, f.duration, f.rating_id, " +
                    "COUNT(fl.user_id) as likes " +
                    "FROM film f " +
                    "LEFT JOIN film_like fl ON f.film_id=fl.film_id " +
                    "JOIN film_x_genre fg ON f.film_id=fg.film_id " +
                    "WHERE fg.genre_id = ? " +
                    "AND EXTRACT (year FROM f.release_dt) = ? " +
                    "GROUP BY f.film_id, f.name, f.description, f.release_dt, f.duration, f.rating_id " +
                    "ORDER BY likes DESC, film_id " +
                    "LIMIT ?";
            films = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeFilm(rs), genreId, year, count);
        }

        if (films.isEmpty()) {
            log.info("Популярные фильмы с жанром {} и годом {} не найдены.", genreId, year);
        }
        return films;
    }

    public void removeFilm(int filmId) {
        String sqlQuery =
                "DELETE FROM film " +
                        "WHERE film_id = ?";

        jdbcTemplate.update(sqlQuery, filmId);
    }

    private Film makeFilm(ResultSet rs) throws SQLException {
        int filmId = rs.getInt("film_id");
        Film film = Film.builder()
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .releaseDate(rs.getDate("release_dt").toLocalDate())
                .duration(rs.getInt("duration"))
                .mpa(mpaStorage.getById(rs.getInt("rating_id")).orElseGet(null))
                .genres(getGenresByFilmId(filmId))
                .likes(getLikesByFilmId(filmId))
                .directors(getDirectorsByFilmId(filmId))
                .build();
        film.setId(filmId);
        return film;
    }

    private Set<Genre> getGenresByFilmId(int filmId) {
        String sqlQuery =
                "SELECT genre_id " +
                        "FROM film_x_genre " +
                        "WHERE film_id = ?";

        return jdbcTemplate.queryForList(sqlQuery, Integer.class, filmId)
                .stream()
                .map(genreStorage::getById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }

    private Set<Integer> getLikesByFilmId(int filmId) {
        String sqlQuery =
                "SELECT user_id " +
                        "FROM film_like " +
                        "WHERE film_id = ?";
        List<Integer> likes = jdbcTemplate.queryForList(sqlQuery, Integer.class, filmId);
        return new HashSet<>(likes);
    }

    private Set<Director> getDirectorsByFilmId(int filmId) {
        String queryFilmXDirectorSelect = "SELECT director_id " +
                "FROM film_x_director " +
                "WHERE film_id = ?;";

        List<Integer> directorsIds = jdbcTemplate.queryForList(queryFilmXDirectorSelect, Integer.class, filmId);

        List<Director> allDirectors = directorStorage.get();
        return allDirectors.stream()
                .filter(director -> directorsIds.contains(director.getId()))
                .collect(Collectors.toSet());
    }
}
