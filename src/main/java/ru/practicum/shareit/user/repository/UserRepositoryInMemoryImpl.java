package ru.practicum.shareit.user.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.EmailAlreadyExistsException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Repository
@Slf4j
public class UserRepositoryInMemoryImpl implements UserRepository {
    Map<Long, User> users = new HashMap<>();
    private long generatorId = 0;

    @Override
    public List<User> findAll() {
        log.info("Возращено пользователей " + users.size());
        return new ArrayList<>(users.values());
    }

    @Override
    public User addUser(User user) {
        checkUserEmail(user.getEmail());
        user.setId(++generatorId);
        users.put(user.getId(), user);
        log.info(String.format("Пользователь с id=%d успешно добавлен", user.getId()));
        return user;
    }

    @Override
    public User getUserById(Long userId) {
        if (!users.containsKey(userId)) {
            log.error(String.format("Пользователь с id=%d не найден", userId));
            throw new UserNotFoundException("Пользователь с id=%d не найден");
        }
        log.info(String.format("Пользователь с id=%d успешно возвращен", userId));
        return users.get(userId);
    }

    @Override
    public User updateUserById(Long userId, User user) {
        User userForUpdate = getUserById(userId);

        Optional.ofNullable(user.getName()).ifPresent(userForUpdate::setName);
        Optional.ofNullable(user.getEmail()).ifPresent(email -> {
            if (!email.equals(userForUpdate.getEmail())) {
                checkUserEmail(email);
            }
            userForUpdate.setEmail(email);
        });
        users.put(userId, userForUpdate);
        log.info(String.format("Пользователь с id=%d успешно обновлен", userId));
        return userForUpdate;

    }

    @Override
    public void deleteUserById(Long userId) {
        if (!users.containsKey(userId)) {
            log.warn(String.format("Пользователь с id=%d не найден", userId));
            return;
        }
        users.remove(userId);
        log.info(String.format("Пользователь с id=%d успешно удален", userId));
    }

    private void checkUserEmail(String email) {
        if (users.values().stream().anyMatch(user -> user.getEmail().equals(email))) {
            throw new EmailAlreadyExistsException("Пользователь с таким e-mail уже существует!");
        }
    }
}
