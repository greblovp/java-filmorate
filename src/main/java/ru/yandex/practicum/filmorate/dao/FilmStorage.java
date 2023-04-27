package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {
    Collection<Film> get();

    Collection<Film> search(String query, Boolean director, Boolean film);

    Optional<Film> getById(int id);

    Film create(Film film);

    Optional<Film> update(Film film);

    void addLike(Film film, User user);

    void removeLike(Film film, User user);

    Collection<Film> getFilmsByDirector(int directorId, String sortType);
}
