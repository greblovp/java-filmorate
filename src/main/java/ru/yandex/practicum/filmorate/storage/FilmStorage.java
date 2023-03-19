package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {
    public Collection<Film> get();

    public Optional<Film> getById(int id);

    public Film create(Film film);

    public Film update(Film film);
}
