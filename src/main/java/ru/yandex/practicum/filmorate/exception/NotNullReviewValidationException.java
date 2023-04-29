package ru.yandex.practicum.filmorate.exception;

public class NotNullReviewValidationException extends RuntimeException {

    public NotNullReviewValidationException(String message) {
        super(message);
    }
}
