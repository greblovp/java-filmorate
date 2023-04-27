package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;



@Data
@AllArgsConstructor
public class Review {

    private long reviewId;  //идентификатор отзыва
    @NonNull
    private String content;  //содержание отзыва
    @NonNull
    @JsonProperty("isPositive")
    private Boolean isPositive;  //тип отзыва
    @NonNull
    private Integer userId;  //идентификатор пользователя, оставившего отзыв
    @NonNull
    private Integer filmId;  //идентификатор фильма, к которому создается отзыв
    private int useful;  //рейтинг полезности отзыва
}
