package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.yandex.practicum.filmorate.dao.DirectorStorage;
import ru.yandex.practicum.filmorate.dao.impl.DirectorDbStorage;
import ru.yandex.practicum.filmorate.exception.DirectorNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DirectorServiceTest {
    private final DirectorStorage directorStorage = Mockito.mock(DirectorDbStorage.class);
    private final DirectorService directorService = new DirectorService(directorStorage);
    List<Director> directors = new ArrayList<>();
    Director director1;
    Director director2;

    @Test
    void getAllDirectors() {
        createDirectors();
        when(directorService.get()).thenReturn(directors);

        List<Director> result = directorService.get();

        assertEquals(directors.size(), result.size(), "Размеры списков режиссеров не совпадают");
        assertEquals(directors, result, "Списки добавленных и полученных режиссеров не совпадают");
        deleteDirectors();
    }

    @Test
    void getDirectorById() {
        createDirectors();
        directorService.create(director1);
        when(directorStorage.getById(director1.getId())).thenReturn(Optional.of(director1));

        Director director = directorService.getById(director1.getId());
        assertEquals(director1, director, "Добавленный и полученный режиссеры не совпадают");
        deleteDirectors();
    }

    @Test
    void getDirectorByNotExistingId() {
        createDirectors();
        int notExistingId = 333;
        DirectorNotFoundException directorNotFoundException = assertThrows(DirectorNotFoundException.class,
                () -> directorService.getById(notExistingId));

        assertEquals("Режиссер с id = " + notExistingId + " отсутствует в БД.",
                directorNotFoundException.getMessage(), "В БД найден режиссер с несуществующим id");
        deleteDirectors();
    }

    @Test
    void createDirector() {
        Director director3 = Director.builder().name("director3").build();

        when(directorService.create(director3)).thenReturn(director3);
        Director createdDirector = directorService.create(director3);

        assertEquals(director3, createdDirector, "Добавленный в БД режиссер не совпадает с созданным");
    }

    @Test
    void udpateDirector() {
        createDirectors();
        directorService.create(director1);

        Director directorUpdated = Director.builder().id(1).name("directorUpdated").build();
        when(directorStorage.udpate(directorUpdated)).thenReturn(Optional.of(directorUpdated));
        when(directorStorage.getById(directorUpdated.getId())).thenReturn(Optional.of(directorUpdated));

        Director directorUpdatedFromDb = directorService.udpate(directorUpdated);

        assertEquals(directorUpdated, directorUpdatedFromDb, "Режиссер в БД не совпадает с обновленным");
        deleteDirectors();
    }

    @Test
    void deleteDirector() {
        createDirectors();
        directorService.delete(director1.getId());
        verify(directorStorage).delete(director1.getId());
        deleteDirectors();
    }

    void createDirectors() {
        director1 = Director.builder().id(1).name("director1").build();
        director2 = Director.builder().id(2).name("director2").build();
        directors.add(director1);
        directors.add(director2);
    }

    void deleteDirectors() {
        directors.clear();
        director1 = null;
        director2 = null;
    }
}