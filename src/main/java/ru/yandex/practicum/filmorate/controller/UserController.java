package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.UserValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.service.ValidateService;

import javax.validation.Valid;
import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final ValidateService validateService;

    @GetMapping
    public Collection<User> findAll() {
        log.info("Вывести всех пользователей");
        return userService.findAll();
    }

    @GetMapping("/{userId}")
    public User findById(@PathVariable int userId) {
        log.info("Вывести пользователя ID = {}", userId);
        return userService.findById(userId);
    }

    @GetMapping("/{userId}/friends")
    public Collection<User> getFriends(@PathVariable int userId) {
        log.info("Вывести пользователя ID = {}", userId);
        return userService.getFriends(userId);
    }

    @GetMapping("/{userId}/friends/common/{otherUserId}")
    public Collection<User> getCommonFriends(@PathVariable int userId, @PathVariable int otherUserId) {
        log.info("Вывести общих друзей пользователя ID = {} и пользователя ID = {}", userId, otherUserId);
        return userService.getCommonFriends(userId, otherUserId);
    }

    @PutMapping("/{userId}/friends/{friendId}")
    public void addFriend(@PathVariable int userId, @PathVariable int friendId) {
        log.info("Добавляем пользователя ID = {} в друзья к пользователю ID = {}", friendId, userId);
        userService.addFriend(userId, friendId);
    }

    @DeleteMapping("/{userId}/friends/{friendId}")
    public void removeFriend(@PathVariable int userId, @PathVariable int friendId) {
        log.info("Удаляем пользователя ID = {} из друзей пользователя ID = {}", friendId, userId);
        userService.removeFriend(userId, friendId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User createUser(@RequestBody @Valid User user, BindingResult bindingResult) {
        log.info("Создаем пользователя: {}", user);
        generateCustomValidateException(user, bindingResult);
        validateService.validateUser(user);
        return userService.createUser(user);
    }

    @PutMapping
    public User updateUser(@RequestBody @Valid User user, BindingResult bindingResult) {
        log.info("Обновляем пользователя: {}", user);
        generateCustomValidateException(user, bindingResult);
        validateService.validateUser(user);
        return userService.updateUser(user);
    }

    @DeleteMapping("/{userId}")
    public void removeUser(@PathVariable int userId) {
        userService.removeUser(userId);
    }

    private void generateCustomValidateException(User user, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.warn("Ошибка в заполнении поля {} - {}. Пользователь - {}", bindingResult.getFieldError().getField(),
                    bindingResult.getFieldError().getDefaultMessage(), user);
            throw new UserValidationException("Ошибка в заполнении поля " + bindingResult.getFieldError().getField());
        }
    }
}
