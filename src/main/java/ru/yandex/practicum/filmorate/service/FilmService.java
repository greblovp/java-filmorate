package ru.yandex.practicum.filmorate.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.FilmStorage;
import ru.yandex.practicum.filmorate.dao.UserStorage;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.SortByValidationException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FilmService {

    @Qualifier("filmDbStorage")
    @NonNull
    private final FilmStorage filmStorage;
    @Qualifier("userDbStorage")
    @NonNull
    private final UserStorage userStorage;
    @NonNull
    private final DirectorService directorService;

    public Collection<Film> findAll() {
        return filmStorage.get();
    }

    public Film findById(int filmId) {
        return checkFilmId(filmId);
    }

    public Film createFilm(Film film) {
        film.setLikes(new HashSet<>());
        return filmStorage.create(film);
    }

    public Film updateFilm(Film film) {
        if (film.getLikes() == null) {
            film.setLikes(new HashSet<>());
        }
        return filmStorage.update(film).orElseThrow(()
                -> new FilmNotFoundException("Фильм с ID = " + film.getId() + " не найден."));
    }

    public void addLike(int filmId, int userId) {
        Film film = checkFilmId(filmId);
        User user = checkUserId(userId);

        filmStorage.addLike(film, user);

        log.debug("Добавлен лайк от пользователя ID = {} в фильм: {}", userId, film);
    }

    public void removeLike(int filmId, int userId) {
        Film film = checkFilmId(filmId);
        User user = checkUserId(userId);

        filmStorage.removeLike(film, user);

        log.debug("Удален лайк от пользователя ID = {} в фильме: {}", userId, film);
    }

    public Collection<Film> getTop(int count, int genreId, int year) {
        log.info("Получаем список из {} популярных фильмов", count);
        if (genreId == 0 && year == 0) {
            return filmStorage.get().stream()
                    .sorted(this::compare)
                    .limit(count)
                    .collect(Collectors.toList());
        }
        return filmStorage.getPopularByGenreAndYear(count, genreId, year);
    }

    public Collection<Film> getFilmsByDirector(int directorId, String sortBy) {
        directorService.checkIfDirectorExists(directorId);
        checkSortByParam(sortBy);
        List<Film> films = new ArrayList<>(filmStorage.getFilmsByDirector(directorId));
        if (sortBy.equals("likes")) {
            Collections.sort(films, this::compare);
        } else {
            Collections.sort(films,
                    (film1, film2) -> {
                        if (film1.getReleaseDate().isBefore(film2.getReleaseDate())) {
                            return -1;
                        } else {
                            return 1;
                        }
                    });
        }

        return films;
    }

    public void removeFilm(int filmId) {
        Film film = checkFilmId(filmId);

        filmStorage.removeFilm(filmId);

        log.debug("Удален фильм: {}", film);
    }

    public Collection<Film> getCommonFilms(int userId, int friendId) {
        checkUserId(userId);
        checkUserId(friendId);
        return filmStorage.getCommonFilms(userId, friendId).stream()
                .sorted(this::compare)
                .collect(Collectors.toList());
    }

    private int compare(Film f0, Film f1) {
        return -1 * (f0.getLikes().size() - f1.getLikes().size()); //обратный порядок
    }

    private User checkUserId(int id) {
        return userStorage.getById(id).orElseThrow(() -> new UserNotFoundException("Пользователь с ID = " + id + " не найден."));
    }

    private Film checkFilmId(int id) {
        return filmStorage.getById(id).orElseThrow(() -> new FilmNotFoundException("Фильм с ID = " + id + " не найден."));
    }

    private void checkSortByParam(String sortType) {
        if (!sortType.equals("year") && !sortType.equals("likes")) {
            throw new SortByValidationException("Некорректно введен параметр сортировки.");
        }
    }
}
