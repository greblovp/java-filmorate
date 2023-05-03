package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
    public Collection<Film> search(String query, Boolean director, Boolean film) {
        StringBuilder sqlQuery = new StringBuilder();
        sqlQuery.append("SELECT * FROM film f " +
                "LEFT JOIN film_x_director fxd ON f.film_id = fxd.film_id " +
                "LEFT JOIN director d ON d.director_id = fxd.director_id " +
                "WHERE 1=1 ");
        if (director && film)
            sqlQuery.append("AND (d.name ILIKE '%").append(query).append("%'").append("OR f.name ILIKE '%").append(query).append("%')");
        else {
            if (director) sqlQuery.append("AND d.name ILIKE '%").append(query).append("%'");
            if (film) sqlQuery.append("AND f.name ILIKE '%").append(query).append("%'");
        }

        return jdbcTemplate.query(sqlQuery.toString(), (rs, rowNum) -> makeFilm(rs));
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
        film.setId(filmId);

        if (film.getGenres() != null) {
            updateGenresSet(film);
        }

        if (film.getDirectors() != null) {
            updateDirectorsSet(film);
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

        deleteOldGenresSet(film);
        if (film.getGenres() != null) {
            updateGenresSet(film);
        }

        deleteOldDirectorsSet(film);
        if (film.getDirectors() != null) {
            updateDirectorsSet(film);
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
    public List<Film> getFilmsByDirector(int directorId) {
        String queryFilmSelect = "SELECT * " +
                "FROM film AS f " +
                "LEFT JOIN film_x_director AS fd ON f.film_id = fd.film_id " +
                "WHERE fd.director_id = ?;";
        return jdbcTemplate.query(queryFilmSelect, (rs, rowNum) -> makeFilm(rs), directorId);
    }

    public Collection<Film> getPopularByGenreAndYear(int count, int genreId, int year) {
        List<Film> films;
        String sqlQuery = "SELECT f.film_id, f.name, f.description, f.release_dt, f.duration, f.rating_id, " +
                "COUNT(fl.user_id) as likes " +
                "FROM film f " +
                "LEFT JOIN film_like fl ON f.film_id=fl.film_id " +
                "LEFT JOIN film_x_genre fg ON f.film_id=fg.film_id AND fg.genre_id = ?" +
                "WHERE COALESCE (fg.genre_id, 0) = ?" +
                "AND EXTRACT (year FROM COALESCE(f.release_dt, '1800-01-01')) = " +
                "CASE WHEN ? = 0 THEN EXTRACT (year FROM COALESCE(f.release_dt, '1800-01-01')) ELSE ? END " +
                "GROUP BY f.film_id, f.name, f.description, f.release_dt, f.duration, f.rating_id " +
                "ORDER BY likes DESC, film_id " +
                "LIMIT ?";

        films = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeFilm(rs), genreId, genreId, year, year, count);

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

    @Override
    public Collection<Film> getFilmRecommendations(int userId) throws EmptyResultDataAccessException {
        String sqlQuery =
                "WITH rec_user AS " +
                        "(SELECT t2.user_id " +
                        "FROM film_like t1 " +
                        "INNER JOIN film_like t2 ON t1.film_id = t2.film_id " +
                        "AND t2.user_id <> t1.user_id " +
                        "WHERE t1.user_id = ? " +
                        "GROUP BY t2.user_id " +
                        "ORDER BY COUNT(t2.film_id) DESC " +
                        "LIMIT 1) " +

                        "SELECT rec.film_id " +
                        "FROM film_like rec " +
                        "INNER JOIN rec_user ON rec.user_id = rec_user.user_id " +
                        "LEFT JOIN film_like base ON rec.film_id = base.film_id " +
                        "AND base.user_id = ? " +
                        "WHERE 1=1 " +
                        "AND base.film_id IS NULL";

        return jdbcTemplate.queryForList(sqlQuery,
                        Integer.class,
                        userId,
                        userId)
                .stream()
                .map(this::getById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<Film> getCommonFilms(int userId, int friendId) {
        String sqlQuery =
                "SELECT film_id " +
                        "FROM film_like " +
                        "WHERE user_id = ? " +
                        "INTERSECT " +
                        "SELECT film_id " +
                        "FROM film_like " +
                        "WHERE user_id = ?";
        return jdbcTemplate.queryForList(sqlQuery,
                        Integer.class,
                        userId,
                        friendId)
                .stream()
                .map(this::getById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
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

    private void updateDirectorsSet(Film film) {
        String queryFilmDirectorInsert = "INSERT INTO film_x_director(film_id, director_id) " +
                "VALUES(?, ?);";

        film.getDirectors().forEach(director -> {
            jdbcTemplate.update(queryFilmDirectorInsert, film.getId(), director.getId());
        });
    }

    private void updateGenresSet(Film film) {
        String genreSqlQuery =
                "INSERT INTO film_x_genre (film_id, genre_id) " +
                        "VALUES (?, ?)";

        film.getGenres().forEach(genre -> {
            jdbcTemplate.update(genreSqlQuery,
                    film.getId(),
                    genre.getId());
        });
    }

    private void deleteOldDirectorsSet(Film film) {
        String queryFilmDirectorDelete = "DELETE FROM film_x_director " +
                "WHERE film_id = ?;";

        jdbcTemplate.update(queryFilmDirectorDelete, film.getId());
    }

    private void deleteOldGenresSet(Film film) {
        String genreDeleteSqlQuery =
                "DELETE FROM film_x_genre " +
                        "WHERE film_id = ?";

        jdbcTemplate.update(genreDeleteSqlQuery, film.getId());
    }
}
