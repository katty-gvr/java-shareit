package ru.practicum.shareit.exception;

public class ItemCannotBeUpdatedException extends RuntimeException {

    public ItemCannotBeUpdatedException(String message) {
        super(message);
    }
}
