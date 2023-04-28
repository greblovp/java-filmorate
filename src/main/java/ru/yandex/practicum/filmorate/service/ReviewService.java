package ru.yandex.practicum.filmorate.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.ReviewStorage;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    @Qualifier("reviewDbStorage")
    @NonNull
    private final ReviewStorage reviewStorage;
    private final FilmService filmService;
    private final UserService userService;


    public Review findById(long reviewId) {
        return reviewStorage.getById(reviewId);
    }

    public Collection<Review> getFilmReviews(int filmId, int count) {
        if (filmId == 0) {
            return reviewStorage.getReviewsByAllFilms(count);
        }
        return reviewStorage.getReviewsByFilmId(filmId, count);
    }

    public Review createReview(Review review) {
        userService.findById(review.getUserId());
        filmService.findById(review.getFilmId());
        return reviewStorage.createReview(review);
    }

    public Review updateReview(Review review) {
        findById(review.getReviewId());
        return reviewStorage.updateReview(review);
    }

    public void likeReview(long reviewId, int userId, boolean like) {
        userService.findById(userId);
        if (like) {
            reviewStorage.likeReview(reviewId, userId, 1);
        } else {
            reviewStorage.likeReview(reviewId, userId, -1);
        }
    }

    public void removeReview(long reviewId) {
        reviewStorage.removeReview(reviewId);
    }

    public void removeLike(long reviewId, int userId, boolean like) {
        if (like) {
            reviewStorage.removeLike(reviewId, userId, 1);
        } else {
            reviewStorage.removeLike(reviewId, userId, -1);
        }
    }
}
