package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.EmailAlreadyExistsException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public Collection<UserDto> findAll() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserDto addUser(UserDto userDto) {
        User saved = userRepository.save(UserMapper.toUser(userDto));
        log.info(String.format("Пользователь с id=%d успешно добавлен", saved.getId()));
        return UserMapper.toUserDto(saved);
    }

    @Override
    @Transactional
    public UserDto getUserById(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        User user = userOptional.orElseThrow(() -> new UserNotFoundException(String.format("Пользователь с id=%d не найден", userId)));
        log.info(String.format("Пользователь с id=%d успешно возвращен", userId));
        return UserMapper.toUserDto(user);
    }

    @Override
    @Transactional
    public UserDto updateUser(Long id, UserDto userDto) {
        User userForUpdate = UserMapper.toUser(getUserById(id));
        User newUser = UserMapper.toUser(userDto);

        Optional.ofNullable(newUser.getName()).ifPresent(userForUpdate::setName);
        Optional.ofNullable(newUser.getEmail()).ifPresent(email -> {
            if (!email.equals(userForUpdate.getEmail())) {
                checkUserEmail(email);
            }
            userForUpdate.setEmail(email);
        });
        userRepository.save(userForUpdate);
        log.info(String.format("Пользователь с id=%d успешно обновлен", id));
        return UserMapper.toUserDto(userForUpdate);
    }

    @Override
    @Transactional
    public void deleteUserById(Long userId) {
        userRepository.deleteById(userId);
    }

    private void checkUserEmail(String email) {
        if (userRepository.findAll().stream().anyMatch(user -> user.getEmail().equals(email))) {
            throw new EmailAlreadyExistsException("Пользователь с таким e-mail уже существует!");
        }
    }
}
