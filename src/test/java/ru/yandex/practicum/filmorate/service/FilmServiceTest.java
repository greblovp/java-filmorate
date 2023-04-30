package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.yandex.practicum.filmorate.dao.FilmStorage;
import ru.yandex.practicum.filmorate.dao.UserStorage;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class FilmServiceTest {

    private final UserStorage userStorage = Mockito.mock(UserStorage.class);
    private final FilmStorage filmStorage = Mockito.mock(FilmStorage.class);
    private final DirectorService directorService = Mockito.mock(DirectorService.class);


    private final FilmService filmService = new FilmService(filmStorage, userStorage, directorService);

    private Film film = Film.builder()
            .name("name")
            .description("description")
            .releaseDate(LocalDate.of(2000, 1, 1))
            .duration(100)
            .build();

    private Film popularFilm = Film.builder()
            .name("name")
            .description("description")
            .releaseDate(LocalDate.of(2010, 1, 1))
            .duration(180)
            .build();

    private User user = User.builder()
            .email("test@test.test")
            .login("login")
            .name("name")
            .birthday(LocalDate.of(2000, 1, 1))
            .build();

    private final int filmId = 1;
    private final int userId = 10;

    @BeforeEach
    void setLikes() {
        film.setLikes(new HashSet<>());
        popularFilm.setLikes(Set.of(1, 2, 3));
    }

    @Test
    void addLikeWhenFilmIsNull() {
        when(filmStorage.getById(filmId)).thenReturn(Optional.empty());
        when(userStorage.getById(userId)).thenReturn(Optional.of(user));

        FilmNotFoundException exception = assertThrows(FilmNotFoundException.class, () -> filmService.addLike(filmId, userId));
        verify(filmStorage).getById(filmId);
        assertEquals(exception.getMessage(), "Фильм с ID = " + filmId + " не найден.");
    }

    @Test
    void addLikeWhenUserIsNull() {
        when(filmStorage.getById(filmId)).thenReturn(Optional.of(film));
        when(userStorage.getById(userId)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> filmService.addLike(filmId, userId));
        verify(filmStorage).getById(filmId);
        verify(userStorage).getById(userId);
        assertEquals(exception.getMessage(), "Пользователь с ID = " + userId + " не найден.");
    }

    @Test
    void addLike() {
        when(filmStorage.getById(filmId)).thenReturn(Optional.of(film));
        when(userStorage.getById(userId)).thenReturn(Optional.of(user));

        filmService.addLike(filmId, userId);

        verify(filmStorage).getById(filmId);
        verify(userStorage).getById(userId);
        verify(filmStorage).addLike(film, user);
    }

    @Test
    void removeLikeWhenFilmIsNull() {
        when(filmStorage.getById(filmId)).thenReturn(Optional.empty());
        when(userStorage.getById(userId)).thenReturn(Optional.of(user));

        FilmNotFoundException exception = assertThrows(FilmNotFoundException.class, () -> filmService.removeLike(filmId, userId));
        verify(filmStorage).getById(filmId);
        assertEquals(exception.getMessage(), "Фильм с ID = " + filmId + " не найден.");
    }

    @Test
    void removeLikeWhenUserIsNull() {
        when(filmStorage.getById(filmId)).thenReturn(Optional.of(film));
        when(userStorage.getById(userId)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> filmService.removeLike(filmId, userId));
        verify(filmStorage).getById(filmId);
        verify(userStorage).getById(userId);
        assertEquals(exception.getMessage(), "Пользователь с ID = " + userId + " не найден.");
    }

    @Test
    void removeLikeWhenZeroLikes() {
        when(filmStorage.getById(filmId)).thenReturn(Optional.of(film));
        when(userStorage.getById(userId)).thenReturn(Optional.of(user));

        filmService.removeLike(filmId, userId);

        verify(filmStorage).getById(filmId);
        verify(userStorage).getById(userId);
        assertEquals(0, film.getLikes().size());
    }

    @Test
    void removeLike() {
        film.getLikes().add(userId);
        when(filmStorage.getById(filmId)).thenReturn(Optional.of(film));
        when(userStorage.getById(userId)).thenReturn(Optional.of(user));

        filmService.removeLike(filmId, userId);

        verify(filmStorage).getById(filmId);
        verify(userStorage).getById(userId);
        verify(filmStorage).removeLike(film, user);
    }

    @Test
    void getTop() {
        when(filmStorage.get()).thenReturn(List.of(film, popularFilm));

        Collection<Film> top = filmService.getTop(3);

        verify(filmStorage).get();
        assertEquals(2, top.size());
        assertEquals(List.of(popularFilm, film), top);
    }

    @Test
    void getTopWithLimit() {
        when(filmStorage.get()).thenReturn(List.of(film, popularFilm));

        Collection<Film> top = filmService.getTop(1);

        verify(filmStorage).get();
        assertEquals(1, top.size());
        assertEquals(List.of(popularFilm), top);
    }

    @Test
    public void testFindAll() {
        when(filmStorage.get()).thenReturn(List.of(film, popularFilm));

        Collection<Film> result = filmService.findAll();

        assertEquals(2, result.size());
        assertEquals(List.of(film, popularFilm), result);
        verify(filmStorage, times(1)).get();
    }

    @Test
    public void testFindById() {
        int filmId = 1;
        when(filmStorage.getById(filmId)).thenReturn(Optional.of(film));

        Film result = filmService.findById(filmId);

        assertEquals(film, result);
        verify(filmStorage, times(1)).getById(filmId);
    }

    @Test
    public void testFindByIdNotFound() {
        int filmId = 3;
        when(filmStorage.getById(filmId)).thenReturn(Optional.empty());

        assertThrows(FilmNotFoundException.class, () -> filmService.findById(filmId));
    }

    @Test
    public void testCreateFilm() {
        when(filmStorage.create(film)).thenReturn(film);

        Film result = filmService.createFilm(film);

        assertEquals(film, result);
        verify(filmStorage, times(1)).create(film);
    }

    @Test
    public void testUpdateFilm() {
        when(filmStorage.update(film)).thenReturn(Optional.of(film));

        Film result = filmService.updateFilm(film);

        assertEquals(film, result);
        verify(filmStorage, times(1)).update(film);
    }

    @Test
    public void testUpdateFilmNotFound() {
        when(filmStorage.update(film)).thenReturn(Optional.empty());

        assertThrows(FilmNotFoundException.class, () -> filmService.updateFilm(film));
    }

    @Test
    public void testRemoveFilm() {
        when(filmStorage.getById(filmId)).thenReturn(Optional.of(film));

        filmService.removeFilm(filmId);

        verify(filmStorage).getById(filmId);
        verify(filmStorage).removeFilm(filmId);
    }

    @Test
    public void testRemoveFilmWhenFilmIsNull() {
        when(filmStorage.getById(filmId)).thenReturn(Optional.empty());

        FilmNotFoundException exception = assertThrows(FilmNotFoundException.class, () -> filmService.removeFilm(filmId));
        verify(filmStorage).getById(filmId);
        assertEquals(exception.getMessage(), "Фильм с ID = " + filmId + " не найден.");
    }

    @Test
    void getFilmsByDirectorSortByLikes() {
        Film film1 = Film.builder()
                .name("name1")
                .description("description1")
                .releaseDate(LocalDate.of(2001, 1, 1))
                .duration(100)
                .directors(new HashSet<>(Arrays.asList(
                        new Director(1, "Director1"))))
                .likes(new HashSet<>(Arrays.asList(2)))
                .build();
        Film film2 = Film.builder()
                .name("name2")
                .description("description2")
                .releaseDate(LocalDate.of(2002, 2, 2))
                .duration(200)
                .directors(new HashSet<>(Arrays.asList(
                        new Director(1, "Director1"))))
                .likes(new HashSet<>(Arrays.asList(1, 2)))
                .build();

        when(filmStorage.getFilmsByDirector(1)).thenReturn(List.of(film2, film1));

        Collection<Film> result = filmService.getFilmsByDirector(1, "likes");

        verify(filmStorage).getFilmsByDirector(1);

        assertEquals(2, result.size());
        assertEquals(List.of(film2, film1), result);
    }

    @Test
    void getFilmsByDirectorSortByYear() {
        Film film1 = Film.builder()
                .name("name1")
                .description("description1")
                .releaseDate(LocalDate.of(2001, 1, 1))
                .duration(100)
                .directors(new HashSet<>(Arrays.asList(
                        new Director(1, "Director1"))))
                .likes(new HashSet<>(Arrays.asList(2)))
                .build();
        Film film2 = Film.builder()
                .name("name2")
                .description("description2")
                .releaseDate(LocalDate.of(2002, 2, 2))
                .duration(200)
                .directors(new HashSet<>(Arrays.asList(
                        new Director(1, "Director1"))))
                .likes(new HashSet<>(Arrays.asList(1, 2)))
                .build();

        when(filmStorage.getFilmsByDirector(1)).thenReturn(List.of(film1, film2));

        Collection<Film> result = filmService.getFilmsByDirector(1, "year");

        verify(filmStorage).getFilmsByDirector(1);

        assertEquals(2, result.size());
        assertEquals(List.of(film1, film2), result);
    }
}