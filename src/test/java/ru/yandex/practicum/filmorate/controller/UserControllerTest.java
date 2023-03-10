package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.dao.FilmRepository;
import ru.yandex.practicum.filmorate.dao.UserRepository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.ValidateService;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
class UserControllerTest {

    @MockBean
    private UserRepository userRepository;
    @MockBean
    private FilmRepository filmRepository;
    @MockBean
    private ValidateService validateService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @SneakyThrows
    @Test
    void getUsers() {
        User userToCreate = User.builder()
                .email("test@test.test")
                .login("login")
                .name("name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
        when(userRepository.get()).thenReturn(List.of(userToCreate));

        String response = mockMvc.perform(get("/users")
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(userRepository).get();
        assertEquals(objectMapper.writeValueAsString(List.of(userToCreate)), response);
    }

    @SneakyThrows
    @Test
    void createValidUser() {
        User userToCreate = User.builder()
                .email("test@test.test")
                .login("login")
                .name("name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
        when(userRepository.create(userToCreate)).thenReturn(userToCreate);

        String response = mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userToCreate)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(userRepository).create(userToCreate);
        assertEquals(objectMapper.writeValueAsString(userToCreate), response);
    }

    @SneakyThrows
    @Test
    void createInValidUser() {
        User userToCreate = User.builder().build();
        when(userRepository.create(userToCreate)).thenReturn(userToCreate);

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userToCreate)))
                .andExpect(status().isBadRequest());

        verify(userRepository, never()).create(any());
    }

    @SneakyThrows
    @Test
    void updateValidUser() {
        User userToUpdate = User.builder()
                .email("test@test.test")
                .login("login")
                .name("name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
        when(userRepository.update(userToUpdate)).thenReturn(userToUpdate);

        String response = mockMvc.perform(put("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userToUpdate)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(userRepository).update(userToUpdate);
        assertEquals(objectMapper.writeValueAsString(userToUpdate), response);
    }

    @SneakyThrows
    @Test
    void updateInValidUser() {
        User userToUpdate = User.builder().build();
        when(userRepository.update(userToUpdate)).thenReturn(userToUpdate);

        mockMvc.perform(put("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userToUpdate)))
                .andExpect(status().isBadRequest());

        verify(userRepository, never()).update(any());
    }

    @SneakyThrows
    @Test
    void updateUserWithWrongId() {
        User userToUpdate = User.builder()
                .email("test@test.test")
                .login("login")
                .name("name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
        when(userRepository.update(userToUpdate)).thenReturn(null);

        String response = mockMvc.perform(put("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userToUpdate)))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(userRepository).update(userToUpdate);
        assertEquals("", response);
    }
}