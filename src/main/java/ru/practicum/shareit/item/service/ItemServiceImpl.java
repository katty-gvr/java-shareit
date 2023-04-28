package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.exception.ItemCannotBeUpdatedException;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final UserRepository userRepository;

    @Override
    public Collection<ItemDto> findAll() {
        return itemRepository.findAll()
                .stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<ItemDto> getUserItems(Long userId) {
       return itemRepository.findAll()
              .stream()
               .filter(item -> Objects.equals(item.getOwner().getId(), userId))
               .map(itemMapper::toItemDto)
               .collect(Collectors.toList());

    }

    @Override
    public ItemDto createItem(Long userId, ItemDto itemDto) {
        User owner = userRepository.getUserById(userId);
        Item item = itemMapper.toItem(itemDto);
        item.setOwner(owner);
        log.info("Пользователь с id=%d добавил новую вещь");
        itemRepository.addItem(item);
        return itemMapper.toItemDto(item);
    }

    @Override
    public ItemDto getItemById(Long itemId) {
        return itemMapper.toItemDto(itemRepository.getItemById(itemId));
    }

    @Override
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        Item itemForUpdate = itemRepository.getItemById(itemId);

        if (!itemForUpdate.getOwner().getId().equals(userId)) {
            throw new ItemCannotBeUpdatedException("Обновить вещь может только её хозяин!");
        }

        Optional.ofNullable(itemDto.getName()).ifPresent(itemForUpdate::setName);
        Optional.ofNullable(itemDto.getDescription()).ifPresent(itemForUpdate::setDescription);
        Optional.ofNullable(itemDto.getAvailable()).ifPresent(itemForUpdate::setAvailable);

        itemRepository.addItem(itemForUpdate);
        log.info(String.format("Вещь с id=%d успешно обновлена", userId));
        return itemMapper.toItemDto(itemForUpdate);
    }

    @Override
    public Collection<ItemDto> searchItem(String word) {
        if (word == null || word.isBlank()) {
            return Collections.emptyList();
        }
        return itemRepository.findAll().stream()
                .filter(Item::getAvailable)
                .filter(item -> item.getName().toLowerCase().contains(word.toLowerCase())
                || item.getDescription().toLowerCase().contains(word.toLowerCase()))
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteItem(Long itemId) {
        itemRepository.deleteItemById(itemId);
    }
}
