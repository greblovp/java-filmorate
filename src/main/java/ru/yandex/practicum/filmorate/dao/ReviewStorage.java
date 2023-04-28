package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;

public interface ReviewStorage {

    Review getById(long reviewId);

    Collection<Review> getReviewsByFilmId(int filmId, int count);

    Review createReview(Review review);

    Review updateReview(Review review);

    void likeReview(long reviewId, int userId, int i);

    void removeReview(long reviewId);

    void removeLike(long reviewId, int userId, int i);

    Collection<Review> getReviewsByAllFilms(int count);
}
