package ru.yandex.practicum.filmorate.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.DirectorStorage;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectorService {
    @Qualifier("directorDbStorage")
    @NonNull
    private final DirectorStorage directorStorage;

    public List<Director> get() {
        return directorStorage.get();
    }

    public Director getById(int id) {
        return directorStorage.getById(id);
    }

    public Director create(Director director) {
        return directorStorage.create(director);
    }

    public Director udpate(Director director) {
        return directorStorage.udpate(director);
    }

    public void delete(int id) {
        directorStorage.delete(id);
    }
}
