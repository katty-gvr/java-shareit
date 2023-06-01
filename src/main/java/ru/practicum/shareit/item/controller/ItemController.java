package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.common.Create;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentShortDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.common.Update;
import ru.practicum.shareit.item.service.CommentService;
import ru.practicum.shareit.item.service.ItemService;

import java.util.Collection;
import java.util.Collections;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;
    private final CommentService commentService;

    @PostMapping
    public ItemDto createNewItem(@RequestHeader("X-Sharer-User-Id") final Long userId,
                                 @Validated(Create.class) @RequestBody ItemDto itemDto) {
        return itemService.createItem(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader("X-Sharer-User-Id") final Long userId,
                              @Validated(Update.class) @RequestBody final ItemDto itemDto,
                              @PathVariable final Long itemId) {
        return itemService.updateItem(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItem(@PathVariable final Long itemId,
                           @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.getItemById(itemId, userId);
    }

    @GetMapping
    public Collection<ItemDto> getUserItems(@RequestHeader("X-Sharer-User-Id") final Long userId,
                                            @RequestParam(value = "from", required = false, defaultValue = "0") final Integer from,
                                            @RequestParam(value = "size", required = false, defaultValue = "10") final Integer size) {
        if (from < 0 || size < 0) {
            throw new BadRequestException("Некорректно переданный параметр запроса");
        }
        return itemService.getUserItems(userId, from, size);
    }

    @GetMapping("/search")
    public Collection<ItemDto> searchItem(@RequestParam("text") final String text,
                                          @RequestParam(value = "from", required = false, defaultValue = "0") final Integer from,
                                          @RequestParam(value = "size", required = false, defaultValue = "10") final Integer size) {
        if (from < 0 || size < 0) {
            throw new BadRequestException("Некорректно переданный параметр запроса");
        }
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        return itemService.searchItem(text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto createItemComment(@RequestBody final CommentShortDto commentShortDto,
                                        @PathVariable final Long itemId,
                                        @RequestHeader("X-Sharer-User-Id") Long userId) {
        if (commentShortDto.getText().isBlank()) {
            throw new BadRequestException("Текст комментария не может быть пустым");
        }
        return commentService.addNewComment(commentShortDto, itemId, userId);
    }
}
