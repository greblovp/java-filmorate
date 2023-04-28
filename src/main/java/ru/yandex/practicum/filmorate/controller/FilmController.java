package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.FilmValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.ValidateService;

import javax.validation.Valid;
import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {

    private final FilmService filmService;
    private final ValidateService validateService;

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Вывести все фильмы");
        return filmService.findAll();
    }

    @GetMapping("/{filmId}")
    public Film findById(@PathVariable int filmId) {
        log.info("Вывести фильм ID = {}", filmId);
        return filmService.findById(filmId);
    }

    @GetMapping("/popular")
    public Collection<Film> getPopular(@RequestParam(defaultValue = "10") int count,
                                       @RequestParam(required = false) String genreId,
                                       @RequestParam(required = false) String year) {
        if (genreId == null && year == null) {
            log.info("Вывести ТОП {} фильмов", count);
            return filmService.getTop(count);
        }
        log.info("Получаем список из {} популярных фильмов", count);
        return filmService.getPopularFilmsByGenreAndYear(count, genreId == null ? 0 : Integer.parseInt(genreId),
                year == null ? 0 : Integer.parseInt(year));
    }

    @PutMapping("/{filmId}/like/{userId}")
    public void addLike(@PathVariable int filmId, @PathVariable int userId) {
        log.info("Добавляем лайк фильму ID = {} от пользователя ID = {}", filmId, userId);
        filmService.addLike(filmId, userId);
    }

    @DeleteMapping("/{filmId}/like/{userId}")
    public void removeLike(@PathVariable int filmId, @PathVariable int userId) {
        log.info("Удаляем лайк у фильма ID = {} от пользователя ID = {}", filmId, userId);
        filmService.removeLike(filmId, userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film createFilm(@RequestBody @Valid Film film, BindingResult bindingResult) {
        log.info("Создаем фильм: {}", film);
        generateCustomValidateException(film, bindingResult);
        validateService.validateFilm(film);
        return filmService.createFilm(film);
    }

    @PutMapping
    public Film updateFilm(@RequestBody @Valid Film film, BindingResult bindingResult) {
        log.info("Обновляем фильм: {}", film);
        generateCustomValidateException(film, bindingResult);
        validateService.validateFilm(film);
        return filmService.updateFilm(film);
    }

    @DeleteMapping("/{filmId}")
    public void removeFilm(@PathVariable int filmId) {
        log.info("Удаляем фильм: {}", filmId);
        filmService.removeFilm(filmId);
    }

    @GetMapping("/director/{directorId}")
    public Collection<Film> findFilmsByDirector(@PathVariable int directorId, @RequestParam String sortBy) {
        log.info("Вывести все фильмы режиссера {} с сортировкой по {}.", directorId, sortBy);
        return filmService.getFilmsByDirector(directorId, sortBy);
    }

    private void generateCustomValidateException(Film film, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.warn("Ошибка в заполнении поля {} - {}. Фильм - {}", bindingResult.getFieldError().getField(),
                    bindingResult.getFieldError().getDefaultMessage(), film);
            throw new FilmValidationException("Ошибка в заполнении поля " + bindingResult.getFieldError().getField());
        }
    }
}
