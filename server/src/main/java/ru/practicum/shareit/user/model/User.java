package ru.practicum.shareit.user.model;

import lombok.*;


import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    @NotNull
    private String name;

    @NotNull(message = "Email пользователя должен быть задан")
    @NotBlank(message = "Email пользователя должен быть задан")
    @Email
    @Column(name = "email", nullable = false, unique = true)
    private String email;
}
