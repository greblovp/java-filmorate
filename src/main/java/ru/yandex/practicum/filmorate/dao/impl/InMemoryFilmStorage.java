package ru.yandex.practicum.filmorate.dao.impl;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.FilmStorage;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

import java.util.List;

import java.util.Collection;
import java.util.Collections;

@Component("inMemoryFilmStorage")
public class InMemoryFilmStorage extends BaseModelStorage<Film> implements FilmStorage {
    @Override
    public Collection<Film> search(String query, Boolean director, Boolean film) {
        throw new UnsupportedOperationException("Реализация метода существует только при работе с БД");
    }

    @Override
    public void addLike(Film film, User user) {
        film.getLikes().add(user.getId());
    }

    @Override
    public void removeLike(Film film, User user) {
        film.getLikes().remove(user.getId());
    }

    @Override
    public List<Film> getFilmsByDirector(int directorId) {
        return null; // это заглушка
    }

    @Deprecated
    public void removeFilm(int filmId) {
    }

    @Deprecated
    public Collection<Film> getPopularByGenreAndYear(int count, int genreId, int year) {
        return null;
    }

    @Deprecated
    public Collection<Film> getFilmRecommendations(int userId) {
        return Collections.emptyList();
    }

    @Deprecated
    public Collection<Film> getCommonFilms(int userId, int friendId) {
        return Collections.emptyList();
    }
}