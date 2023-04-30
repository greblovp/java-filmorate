package ru.yandex.practicum.filmorate.exception;

public class DirectorValidationException extends RuntimeException {
    public DirectorValidationException(String message) {
        super(message);
    }
}
