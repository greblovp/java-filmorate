package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.yandex.practicum.filmorate.dao.DirectorStorage;
import ru.yandex.practicum.filmorate.dao.impl.DirectorDbStorage;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
        when(directorService.getById(director1.getId())).thenReturn(director1);

        Director director = directorService.getById(director1.getId());
        assertEquals(director1, director, "Добавленный и полученный режиссеры не совпадают");
        deleteDirectors();
    }

    @Test
    void getDirectorByNotExistingId() {
        createDirectors();
        when(directorService.getById(333)).thenReturn(null);

        Director director = directorService.getById(333);

        assertNull(director, "Найден несуществующий режиссер");
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
        Director director3 = Director.builder().name("director3").build();

        when(directorService.udpate(director3)).thenReturn(director3);
        Director updatedDirector = directorService.udpate(director3);

        assertEquals(director3, updatedDirector, "Режиссер в БД не совпадает с обновленным");
    }

    @Test
    void deleteDirector() {
        createDirectors();
        directorService.delete(director1.getId());
        verify(directorStorage).delete(director1.getId());
        deleteDirectors();
    }

    void createDirectors() {
        director1 = Director.builder().name("director1").build();
        director2 = Director.builder().name("director2").build();
        directors.add(director1);
        directors.add(director2);
    }

    void deleteDirectors() {
        directors.clear();
        director1 = null;
        director2 = null;
    }
}