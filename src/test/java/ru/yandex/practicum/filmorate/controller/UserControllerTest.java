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
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @MockBean
    private UserService userService;
    @MockBean
    private ValidateService validateService;
    @MockBean
    private FilmService filmService;
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
    void testGetUsers() {
        User userToCreate = User.builder()
                .email("test@test.test")
                .login("login")
                .name("name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
        when(userService.findAll()).thenReturn(List.of(userToCreate));

        String response = mockMvc.perform(get("/users")
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(userService).findAll();
        assertEquals(objectMapper.writeValueAsString(List.of(userToCreate)), response);
    }

    @SneakyThrows
    @Test
    void testCreateValidUser() {
        User userToCreate = User.builder()
                .email("test@test.test")
                .login("login")
                .name("name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
        when(userService.createUser(userToCreate)).thenReturn(userToCreate);

        String response = mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userToCreate)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(userService).createUser(userToCreate);
        assertEquals(objectMapper.writeValueAsString(userToCreate), response);
    }

    @SneakyThrows
    @Test
    void testCreateInValidUser() {
        User userToCreate = User.builder().build();
        when(userService.createUser(userToCreate)).thenReturn(userToCreate);

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userToCreate)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any());
    }

    @SneakyThrows
    @Test
    void testUpdateValidUser() {
        User userToUpdate = User.builder()
                .email("test@test.test")
                .login("login")
                .name("name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
        when(userService.updateUser(userToUpdate)).thenReturn(userToUpdate);

        String response = mockMvc.perform(put("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userToUpdate)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(userService).updateUser(userToUpdate);
        assertEquals(objectMapper.writeValueAsString(userToUpdate), response);
    }

    @SneakyThrows
    @Test
    void testUpdateInValidUser() {
        User userToUpdate = User.builder().build();
        when(userService.updateUser(userToUpdate)).thenReturn(userToUpdate);

        mockMvc.perform(put("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userToUpdate)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).updateUser(any());
    }

    @Test
    @SneakyThrows
    public void testFindById() {
        int userId = 1;
        User user = User.builder().build();
        user.setId(userId);

        when(userService.findById(userId)).thenReturn(user);

        mockMvc.perform(get("/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId));
    }

    @Test
    @SneakyThrows
    public void testFindByIdNotFound() {
        int userId = 1;

        when(userService.findById(userId)).thenThrow(new UserNotFoundException(String.format("Пользователь с ID = %d не найден.", userId)));

        mockMvc.perform(get("/users/" + userId))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetFriends() throws Exception {
        int userId = 1;

        when(userService.getFriends(userId)).thenReturn(Collections.emptyList());

        mockMvc.perform(MockMvcRequestBuilders.get("/users/" + userId + "/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        verify(userService, times(1)).getFriends(userId);
    }

    @Test
    public void testGetCommonFriends() throws Exception {
        int userId1 = 1;
        int userId2 = 2;
        when(userService.getCommonFriends(userId1, userId2)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/users/" + userId1 + "/friends/common/" + userId2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(userService).getCommonFriends(userId1, userId2);
    }

    @Test
    @SneakyThrows
    public void testAddFriend() {
        int userId1 = 1;
        int userId2 = 2;
        mockMvc.perform(put("/users/" + userId1 + "/friends/" + userId2))
                .andExpect(status().isOk());

        verify(userService).addFriend(userId1, userId2);
    }

    @Test
    public void testRemoveFriend() throws Exception {
        int userId1 = 1;
        int userId2 = 2;
        mockMvc.perform(delete("/users/" + userId1 + "/friends/" + userId2))
                .andExpect(status().isOk());

        verify(userService).removeFriend(userId1, userId2);
    }

    @Test
    @SneakyThrows
    public void testRemoveUser() {
        int userId = 1;

        mockMvc.perform(MockMvcRequestBuilders.delete("/users/" + userId))
                .andExpect(status().isOk());
        verify(userService, times(1)).removeUser(userId);
    }

}