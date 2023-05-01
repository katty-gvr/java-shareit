package ru.practicum.shareit.user.model;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.common.Create;
import ru.practicum.shareit.common.Update;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
public class User {
    private Long id;
    private String name;
    @NotNull(message = "Email пользователя должен быть задан", groups = {Create.class})
    @NotBlank(message = "Email пользователя должен быть задан", groups = {Create.class})
    @Email(groups = {Create.class, Update.class})
    private String email;
}
