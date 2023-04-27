package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.DirectorValidationException;
import ru.yandex.practicum.filmorate.exception.FilmValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

@Component
@Slf4j
public class ValidateService {
    private static final LocalDate FIRST_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    public void validateUser(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    public void validateFilm(Film film) {
        if (film.getReleaseDate().isBefore(FIRST_RELEASE_DATE)) {
            log.warn("Дата релиза должна быть больше 28 декабря 1895 года: {}", film);
            throw new FilmValidationException(
                    "Дата релиза должна быть больше 28 декабря 1895 года - день рождения кино");
        }
    }

    public void validateDirector(Director director) {
        if (director.getName().isBlank() || director.getName() == null) {
            throw new DirectorValidationException("Имя режиссера не может быть пустым.");
        }
    }
}