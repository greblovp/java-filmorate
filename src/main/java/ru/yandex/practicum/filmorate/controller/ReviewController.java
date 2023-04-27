package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import javax.validation.Valid;
import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/{id}")
    public Review getReviewById(@PathVariable long id) {
        log.info("Вывести отзыв ID = {}", id);
        return reviewService.findById(id);
    }

    @GetMapping
    public Collection<Review> getFilmReviews(@RequestParam(defaultValue = "0") int filmId, @RequestParam(defaultValue = "10") int count) {
        log.info("Вывести список отзывов к фильму ID = {}, в количестве {}", filmId, count);
        return reviewService.getFilmReviews(filmId, count);
    }

    @PostMapping
    public Review createReview(@RequestBody @Valid Review review) {
        log.info("Создать отзыв: {}", review);
        return reviewService.createReview(review);
    }

    @PutMapping
    public Review updateReview(@RequestBody @Valid Review review) {
        log.info("Обновить отзыв: {}", review);
        return reviewService.updateReview(review);
    }

    @PutMapping("/{id}/like/{userId}")
    public void likeReview(@PathVariable int id, @PathVariable int userId) {
        log.info("User с ID = {} ставит лайк отзыву с ID = {}", userId, id);
        reviewService.likeReview(id, userId, true);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void dislikeReview(@PathVariable long id, @PathVariable int userId) {
        log.info("User с ID = {} ставит дизлайк отзыву с ID = {}", userId, id);
        reviewService.likeReview(id, userId, false);
    }

    @DeleteMapping("/{id}")
    public void removeReview(@PathVariable long id) {
        log.info("Удалить отзыв с ID: {}", id);
        reviewService.removeReview(id);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable long id, @PathVariable int userId) {
        log.info("User с ID = {} удаляет лайк к отзыву с ID = {}", userId, id);
        reviewService.removeLike(id, userId, true);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void removeDislike(@PathVariable long id, @PathVariable int userId) {
        log.info("User с ID = {} удаляет дизлайк к отзыву с ID = {}", userId, id);
        reviewService.removeLike(id, userId, false);
    }
}
