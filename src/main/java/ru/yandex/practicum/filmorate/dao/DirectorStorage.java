package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;

public interface DirectorStorage {
    List<Director> get();

    Director getById(int id);

    Director create(Director director);

    Director udpate(Director director);

    void delete(int id);

    void checkIfDirectorExists(int id);
}
