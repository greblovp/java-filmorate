package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DirectorControllerTest {

    @MockBean
    private FilmService filmService;
    @MockBean
    private DirectorService directorService;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;

    @Test
    @SneakyThrows
    void getAllDirectors() {
        Director director = Director.builder().name("director1").build();
        when(directorService.get()).thenReturn(List.of(director));

        String response = mockMvc
                .perform(get("/directors").contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(directorService).get();
        assertEquals(objectMapper.writeValueAsString(List.of(director)), response);
    }

    @Test
    @SneakyThrows
    void getDirectorById() {
        Director director = Director.builder().id(1).name("director1").build();
        when(directorService.getById(1)).thenReturn(director);

        mockMvc.perform(get("/directors/" + 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("director1"));
    }

    @Test
    @SneakyThrows
    void createDirector() {
        Director director = Director.builder().id(1).name("director1").build();
        when(directorService.create(director)).thenReturn(director);

        String response = mockMvc
                .perform(post("/directors")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(director)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(directorService).create(director);
        assertEquals(objectMapper.writeValueAsString(director), response);
    }

    @Test
    @SneakyThrows
    void createDirectorWithInCorrectParams() {
        Director director4 = Director.builder().name(null).build();

        when(directorService.create(director4)).thenReturn(director4);

        mockMvc.perform(post("/directors")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(director4)))
                .andExpect(status().isBadRequest());

        verify(directorService, never()).create(any());

        Director director5 = Director.builder().name("     ").build();

        when(directorService.create(director5)).thenReturn(director5);

        mockMvc.perform(post("/directors").contentType("application/json").content(objectMapper.writeValueAsString(director5)))
                .andExpect(status().isBadRequest());

        verify(directorService, never()).create(any());
    }

    @Test
    @SneakyThrows
    void updateDirector() {
        Director director = Director.builder().id(1).name("director1").build();
        when(directorService.udpate(director)).thenReturn(director);

        String response = mockMvc
                .perform(put("/directors")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(director)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(directorService).udpate(director);
        assertEquals(objectMapper.writeValueAsString(director), response);
    }

    @Test
    @SneakyThrows
    void updateDirectorWithInCorrectParams() {
        Director director1 = Director.builder().id(1).name(null).build();
        Director director2 = Director.builder().id(2).name("     ").build();

        when(directorService.udpate(director1)).thenReturn(director1);

        mockMvc.perform(put("/directors")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(director1)))
                .andExpect(status().isBadRequest());

        verify(directorService, never()).udpate(any());

        when(directorService.udpate(director2)).thenReturn(director2);

        mockMvc.perform(put("/directors").contentType("application/json").content(objectMapper.writeValueAsString(director2)))
                .andExpect(status().isBadRequest());

        verify(directorService, never()).udpate(any());
    }
}