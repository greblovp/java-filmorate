package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;
import java.util.Optional;

public interface DirectorStorage {
    List<Director> get();

    Optional<Director> getById(int id);

    Director create(Director director);

    Optional<Director> udpate(Director director);

    void delete(int id);
}
