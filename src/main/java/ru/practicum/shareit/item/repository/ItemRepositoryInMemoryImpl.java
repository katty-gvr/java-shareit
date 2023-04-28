package ru.practicum.shareit.item.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Repository
@Slf4j
public class ItemRepositoryInMemoryImpl implements ItemRepository {

    Map<Long, Item> items = new HashMap<>();
    private long generatorId = 0;

    @Override
    public List<Item> findAll() {
        log.info("Возращено вещей " + items.size());
        return new ArrayList<>(items.values());
    }

    @Override
    public Item addItem(Item item) {
        if (!items.containsKey(item.getId())) {
            item.setId(++generatorId);
        }
        items.put(item.getId(), item);
        log.info(String.format("Вещь с id=%d успешно добавлена", item.getId()));
        return item;
    }

    @Override
    public Item getItemById(Long itemId) {
        if (!items.containsKey(itemId)) {
            log.error(String.format("Вещь с id=%d не найдена", itemId));
            throw new ItemNotFoundException("Вещь не найдена");
        }
        log.info(String.format("вещь с id=%d успешно возвращена", itemId));
        return items.get(itemId);
    }

    @Override
    public void deleteItemById(Long id) {
        if (!items.containsKey(id)) {
            log.warn(String.format("Вещь с id=%d не найдена", id));
            return;
        }
        items.remove(id);
        log.info(String.format("Вещь с id=%d успешно удалена", id));
    }
}
