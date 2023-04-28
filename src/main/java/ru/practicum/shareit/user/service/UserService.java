package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;

public interface UserService {
    Collection<UserDto> findAll();

    UserDto addUser(User user);

    UserDto getUserById(Long userId);

    UserDto updateUser(Long id, User user);

    void deleteUserById(Long userId);
}
