package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;


@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class Review {

    private long reviewId;  //идентификатор отзыва
    @NotNull(message = "review notnull")
    private String content;  //содержание отзыва
    @NotNull(message = "review notnull")
    @JsonProperty("isPositive")
    private Boolean isPositive;  //тип отзыва
    @NotNull(message = "review notnull")
    private Integer userId;  //идентификатор пользователя, оставившего отзыв
    @NotNull(message = "review notnull")
    private Integer filmId;  //идентификатор фильма, к которому создается отзыв
    private int useful;  //рейтинг полезности отзыва
}
