package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Past;
import java.time.LocalDate;

@Data
@Builder
public class User extends BaseModel {
    //целочисленный идентификатор — id;
    //электронная почта — email;
    //логин пользователя — login;
    //имя для отображения — name;
    //дата рождения — birthday.
    @Email
    private String email;
    @NotBlank
    private String login;
    private String name;
    @Past
    private LocalDate birthday;
}
