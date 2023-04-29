package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Review;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureTestDatabase
@SqlGroup({
        @Sql(scripts = "classpath:schema.sql", config = @SqlConfig(encoding = "UTF-8"),
                executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(scripts = "classpath:create_test_data.sql", config = @SqlConfig(encoding = "UTF-8"),
                executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@AutoConfigureMockMvc
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    public Review failReview = Review.builder()
            .userId(2)
            .filmId(1)
            .isPositive(true)
            .build();

    public Review review = Review.builder()
            .userId(2)
            .filmId(1)
            .content("Отличное кино")
            .isPositive(true)
            .build();

    @Test
    void shouldBeReturnStatusOkAndId1() throws Exception {
        mockMvc.perform(get("/reviews/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.reviewId").value("1"));
    }

    @Test
    void shouldBeReturnExceptionStatus404() throws Exception {
        mockMvc.perform(get("/reviews/10"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.description").value("Отзыв с ID 10 не существует"));
    }

    @Test
    void shouldBeReturnStatus404ForUser999() throws Exception {
        Review failUser999 = review.toBuilder()
                .userId(999)
                .build();

        mockMvc.perform(post("/reviews")
                        .content(objectMapper.writeValueAsString(failUser999))
                        .contentType("application/json"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.description").value("Пользователь с ID = 999 не найден."));
    }

    @Test
    void shouldReturnListSize2() throws Exception {
        mockMvc.perform(get("/reviews?filmId=1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void shouldReturnListWithAllReviews() throws Exception {
        mockMvc.perform(get("/reviews"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(6)));
    }

    @Test
    void shouldReturnNewReview7ThenDelete() throws Exception {
        mockMvc.perform(post("/reviews")
                        .content(objectMapper.writeValueAsString(review))
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.reviewId").value("7"));

        mockMvc.perform(delete("/reviews/7"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/reviews/7"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.description").value("Отзыв с ID 7 не существует"));
    }

    @Test
    void shouldReturnStatus400() throws Exception {
        mockMvc.perform(post("/reviews")
                        .content(objectMapper.writeValueAsString(failReview))
                        .contentType("application/json"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.description").value("Поле 'content' не должно быть пустым"));
    }

    @Test
    void shouldReturnUpdatedReview() throws Exception {
        Review updateReview = failReview.toBuilder()
                .reviewId(6)
                .content("Просто отзыв")
                .isPositive(false)
                .build();

        mockMvc.perform(put("/reviews/")
                        .content(objectMapper.writeValueAsString(updateReview))
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.reviewId").value("6"))
                .andExpect(jsonPath("$.content").value("Просто отзыв"));
    }

    @Test
    void shouldIncreaseScoreBy1() throws Exception {
        mockMvc.perform(get("/reviews/5"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.useful").value("-1"));

        mockMvc.perform(put("/reviews/{id}/like/{userId}", 5, 1))
                .andExpect(status().isOk());

        mockMvc.perform(get("/reviews/5"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.useful").value("0"));
    }

    @Test
    void shouldDecreaseScoreBy1() throws Exception {
        mockMvc.perform(get("/reviews/6"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.useful").value("3"));

        mockMvc.perform(put("/reviews/{id}/dislike/{userId}", 6, 4))
                .andExpect(status().isOk());

        mockMvc.perform(get("/reviews/6"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.useful").value("2"));
    }

    @Test
    void shouldRemoveLike() throws Exception {
        mockMvc.perform(get("/reviews/6"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.useful").value("3"));

        mockMvc.perform(delete("/reviews/{id}/like/{userId}", 6, 3))
                .andExpect(status().isOk());

        mockMvc.perform(get("/reviews/6"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.useful").value("2"));
    }

    @Test
    void shouldRemoveDislike() throws Exception {
        mockMvc.perform(get("/reviews/3"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.useful").value("-2"));

        mockMvc.perform(delete("/reviews/{id}/dislike/{userId}", 3, 1))
                .andExpect(status().isOk());

        mockMvc.perform(get("/reviews/3"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.useful").value("-1"));
    }
}