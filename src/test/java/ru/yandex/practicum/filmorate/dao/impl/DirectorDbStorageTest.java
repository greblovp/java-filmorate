package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.jdbc.SqlGroup;
import ru.yandex.practicum.filmorate.exception.DirectorNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@SqlGroup({
        @Sql(scripts = "classpath:schema.sql",
                config = @SqlConfig(encoding = "UTF-8"),
                executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(scripts = "classpath:create_test_data.sql",
                config = @SqlConfig(encoding = "UTF-8"),
                executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DirectorDbStorageTest {

    private final DirectorDbStorage directorDbStorage;


    @Test
    void getAllDirectors() {
        Collection<Director> directors = directorDbStorage.get();
        assertEquals(3, directors.size());
    }

    @Test
    void getDirectorByExistingId() {
        Director director = directorDbStorage.getById(1);
        assertEquals("Director1", director.getName(),
                "Имя полученного режиссера не совпадает с именем в БД");

    }

    @Test
    void getDirectorByNotExistingId() {
        int notExistingId = 1111;

        DirectorNotFoundException directorNotFoundException = assertThrows(DirectorNotFoundException.class,
                () -> directorDbStorage.getById(notExistingId));
        assertEquals("Режиссер с id = " + notExistingId + " отсутствует в БД.",
                directorNotFoundException.getMessage(), "Из БД получен режиссер по несуществующему id");
    }

    @Test
    void createDirectorWithCorrectParams() {
        int currentDirectorsListSize = directorDbStorage.get().size();
        Director director4 = Director.builder().name("Director4").build();
        directorDbStorage.create(director4);

        assertTrue(directorDbStorage.get().size() == currentDirectorsListSize + 1,
                "После добавления режиссера размер списка режиссеров не корректный");
        assertEquals(directorDbStorage.getById(4).getName(), director4.getName(),
                "Имена добавленного и полученного режиссера не совпадают");
    }

    @Test
    void udpateDirector() {
        int currentDirectorsListSize = directorDbStorage.get().size();
        Director updatedDirector1 = Director.builder().id(1).name("Not director1").build();
        directorDbStorage.udpate(updatedDirector1);

        assertTrue(directorDbStorage.get().size() == currentDirectorsListSize,
                "После изменения режиссера изменился размер списка режиссеров");
        assertEquals(directorDbStorage.getById(1).getName(), updatedDirector1.getName(),
                "Именя измененного и полученного режиссера не совпадают");
    }

    @Test
    void deleteExistingDirector() {
        List<Director> directorList = directorDbStorage.get();
        int currentDirectorsListSize = directorList.size();
        directorDbStorage.delete(directorList.get(0).getId());

        assertTrue(directorDbStorage.get().size() == currentDirectorsListSize - 1,
                "После удаления режиссера размер списка режиссеров стал некорректным");
        assertFalse(directorDbStorage.get().contains(directorList.get(0).getId()),
                "Режиссер с id = " + directorList.get(0).getId() + " не удален из БД");

        for (int i = 1; i < directorList.size(); i++) {
            directorDbStorage.delete(directorList.get(i).getId());
        }

        assertTrue(directorDbStorage.get().isEmpty(),
                "После удаления всех режиссеров размер списка режиссеров не пуст");
    }

    @Test
    void deleteNotExistingDirector() {
        List<Director> directorList = directorDbStorage.get();
        int currentDirectorsListSize = directorList.size();
        int notExistingId = currentDirectorsListSize + 100;

        DirectorNotFoundException directorNotFoundException = assertThrows(DirectorNotFoundException.class,
                () -> directorDbStorage.delete(notExistingId));

        assertEquals("Режиссер с id = " + notExistingId + " отсутствует в БД.",
                directorNotFoundException.getMessage(), "Из БД удален режиссер с несуществующим id");
    }

    @Test
    void checkIfDirectorExistsForNotExistingDirector() {
        int notExistingId = 3333;
        DirectorNotFoundException directorNotFoundException = assertThrows(DirectorNotFoundException.class,
                () -> directorDbStorage.checkIfDirectorExists(notExistingId));

        assertEquals("Режиссер с id = " + notExistingId + " отсутствует в БД.",
                directorNotFoundException.getMessage(), "В БД найден режиссер с несуществующим id");
    }
}