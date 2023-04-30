package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.enums.ActionType;
import ru.yandex.practicum.filmorate.enums.EventType;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FilmControllerTest {

    @MockBean
    private FilmService filmService;
    @MockBean
    private ValidateService validateService;
    @MockBean
    private EventService eventService;
    @MockBean
    private UserService userService;
    @MockBean
    private MPAService mpaService;
    @MockBean
    private GenreService genreService;
    @MockBean
    private DirectorService directorService;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;

    @SneakyThrows
    @Test
    void testGetFilms() {
        Film filmToCreate = Film.builder().name("name").description("description").releaseDate(LocalDate.of(2000, 1, 1)).duration(100).build();
        when(filmService.findAll()).thenReturn(List.of(filmToCreate));

        String response = mockMvc.perform(get("/films").contentType("application/json")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        verify(filmService).findAll();
        assertEquals(objectMapper.writeValueAsString(List.of(filmToCreate)), response);
    }

    @SneakyThrows
    @Test
    public void testFindById() {
        int filmId = 1;
        Film film = Film.builder().build();
        film.setId(filmId);

        when(filmService.findById(filmId)).thenReturn(film);

        mockMvc.perform(get("/films/" + filmId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(filmId));
    }

    @Test
    public void testFindByIdNotFound() throws Exception {
        int filmId = 1;

        when(filmService.findById(filmId)).thenThrow(new FilmNotFoundException(String.format("Фильм с ID = %d не найден.", filmId)));

        mockMvc.perform(get("/films/" + filmId))
                .andExpect(status().isNotFound());
    }

    @SneakyThrows
    @Test
    void testCreateValidFilm() {
        Film filmToCreate = Film.builder().name("name").description("description").releaseDate(LocalDate.of(2000, 1, 1)).duration(100).build();
        when(filmService.createFilm(filmToCreate)).thenReturn(filmToCreate);

        String response = mockMvc.perform(post("/films").contentType("application/json").content(objectMapper.writeValueAsString(filmToCreate))).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();

        verify(filmService).createFilm(filmToCreate);
        assertEquals(objectMapper.writeValueAsString(filmToCreate), response);
    }

    @SneakyThrows
    @Test
    void testCreateInvalidFilm() {
        Film filmToCreate = Film.builder().build();
        when(filmService.createFilm(filmToCreate)).thenReturn(filmToCreate);

        mockMvc.perform(post("/films").contentType("application/json").content(objectMapper.writeValueAsString(filmToCreate))).andExpect(status().isBadRequest());

        verify(filmService, never()).createFilm(any());
    }

    @SneakyThrows
    @Test
    void testUpdateValidFilm() {
        Film filmToUpdate = Film.builder().name("name").description("description").releaseDate(LocalDate.of(2000, 1, 1)).duration(100).build();
        when(filmService.updateFilm(filmToUpdate)).thenReturn(filmToUpdate);

        String response = mockMvc.perform(put("/films").contentType("application/json").content(objectMapper.writeValueAsString(filmToUpdate))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        verify(filmService).updateFilm(filmToUpdate);
        assertEquals(objectMapper.writeValueAsString(filmToUpdate), response);
    }

    @SneakyThrows
    @Test
    void testUpdateInvalidFilm() {
        Film filmToUpdate = Film.builder().build();
        when(filmService.updateFilm(filmToUpdate)).thenReturn(filmToUpdate);

        mockMvc.perform(put("/films").contentType("application/json").content(objectMapper.writeValueAsString(filmToUpdate))).andExpect(status().isBadRequest());

        verify(filmService, never()).updateFilm(any());
    }

    @Test
    @SneakyThrows
    public void testGetPopular() {
        int count = 5;
        when(filmService.getTop(count, 0, 0)).thenReturn(Collections.emptyList());

        mockMvc.perform(MockMvcRequestBuilders.get("/films/popular?count=" + count))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        verify(filmService, times(1)).getTop(count, 0, 0);
    }

    @Test
    @SneakyThrows
    public void testAddLike() {
        int filmId = 1;
        int userId = 2;

        mockMvc.perform(MockMvcRequestBuilders.put("/films/" + filmId + "/like/" + userId))
                .andExpect(status().isOk());
        verify(filmService, times(1)).addLike(filmId, userId);
        verify(eventService, times(1)).createEvent(userId, ActionType.ADD, EventType.LIKE, filmId);
    }

    @Test
    @SneakyThrows
    public void testRemoveLike() {
        int filmId = 1;
        int userId = 2;

        mockMvc.perform(MockMvcRequestBuilders.delete("/films/" + filmId + "/like/" + userId))
                .andExpect(status().isOk());
        verify(filmService, times(1)).removeLike(filmId, userId);
        verify(eventService, times(1)).createEvent(userId, ActionType.REMOVE, EventType.LIKE, filmId);
    }

    @Test
    @SneakyThrows
    public void testRemoveFilm() {
        int filmId = 1;

        mockMvc.perform(MockMvcRequestBuilders.delete("/films/" + filmId))
                .andExpect(status().isOk());
        verify(filmService, times(1)).removeFilm(filmId);
    }

    @Test
    @SneakyThrows
    void getFilmsByDirectorSortedByLikesOrYear() {
        Film filmToCreate1 = Film.builder()
                .name("name1")
                .description("description1")
                .releaseDate(LocalDate.of(2001, 1, 1))
                .duration(100)
                .directors(new HashSet<>(Arrays.asList(
                        new Director(1, "Director1"))))
                .build();
        Film filmToCreate2 = Film.builder()
                .name("name2")
                .description("description2")
                .releaseDate(LocalDate.of(2002, 2, 2))
                .duration(200)
                .directors(new HashSet<>(Arrays.asList(
                        new Director(1, "Director1"))))
                .build();

        when(filmService.getFilmsByDirector(1, "likes"))
                .thenReturn(List.of(filmToCreate1, filmToCreate2));

        String response = mockMvc.perform(
                        get("/films/director/" + 1 + "?sortBy=likes")
                                .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(filmService).getFilmsByDirector(1, "likes");
        assertEquals(objectMapper.writeValueAsString(List.of(filmToCreate1, filmToCreate2)), response);
    }

    @Test
    @SneakyThrows
    public void testGetPopularByGenreAndYear() {
        int count = 5;
        int genreId = 1;
        int year = 1999;
        when(filmService.getTop(count, genreId, year)).thenReturn(Collections.emptyList());

        mockMvc.perform(MockMvcRequestBuilders.get("/films/popular?count=" + count + "&genreId=" + genreId +
                        "&year=" + year))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        verify(filmService, times(1)).getTop(count, genreId, year);
    }

    @Test
    @SneakyThrows
    public void testGetCommonFilms() {
        int userId = 1;
        int friendId = 2;
        when(filmService.getCommonFilms(userId, friendId)).thenReturn(Collections.emptyList());

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/films/common?userId=" + userId + "&friendId=" + friendId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        verify(filmService, times(1)).getCommonFilms(userId, friendId);
    }

    @Test
    @SneakyThrows
    public void testGetCommonFilmsForNotExistedUser() {
        int userId = 123;
        int friendId = 2;
        when(filmService.getCommonFilms(userId, friendId))
                .thenThrow(new UserNotFoundException(String.format("Пользователь с ID = %d не найден.", userId)));

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/films/common?userId=" + userId + "&friendId=" + friendId))
                .andExpect(status().isNotFound());
    }
}