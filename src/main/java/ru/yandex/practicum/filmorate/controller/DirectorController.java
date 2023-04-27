package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;
import ru.yandex.practicum.filmorate.service.ValidateService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/directors")
public class DirectorController {
    private final DirectorService directorService;
    private final ValidateService validateService;

    @GetMapping
    public List<Director> getAllDirectors() {
        log.info("Вывести всех режиссеров.");
        return directorService.get();
    }

    @GetMapping("/{id}")
    public Director getById(@PathVariable int id) {
        log.info("Вывести режиссера с id = {}.");
        return directorService.getById(id);
    }

    @PostMapping
    public Director create(@RequestBody @Valid Director director) {
        log.info("Добавить режиссера {}", director.getName());
        validateService.validateDirector(director);
        return directorService.create(director);
    }

    @PutMapping
    public Director update(@RequestBody @Valid Director director) {
        log.info("Обновить режиссера с id = {}", director.getId());
        validateService.validateDirector(director);
        return directorService.udpate(director);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        log.info("Удалить режиссера с id = {}", id);
        directorService.delete(id);
    }
}
