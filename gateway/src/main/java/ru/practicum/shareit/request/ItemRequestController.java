package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;


@RestController
@Validated
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private final ItemRequestClient requestClient;

    @PostMapping
    public ResponseEntity<Object> createNewRequest(@RequestHeader("X-Sharer-User-Id") final Long userId,
                                                   @RequestBody ItemRequestDto itemRequestDto) {
       /* if (itemRequestDto.getDescription() == null || itemRequestDto.getDescription().isBlank()) {
            throw new BadRequestException("Описание запроса не может быть пустым!");
        }*/
        return requestClient.addNewRequest(userId, itemRequestDto);
    }

    @GetMapping
    public ResponseEntity<Object> getUserRequests(@RequestHeader("X-Sharer-User-Id") final Long userId) {
        return requestClient.getUserRequests(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequests(@RequestHeader("X-Sharer-User-Id") final Long userId,
                                                 @RequestParam(value = "from", defaultValue = "0") final Integer from,
                                                 @RequestParam(value = "size", defaultValue = "10") final Integer size) {
        /*if (from < 0) {
            throw new BadRequestException("Некорректно переданный параметр запроса");
        }*/
        return requestClient.getAllRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequestById(@RequestHeader("X-Sharer-User-Id") final Long userId,
                                                 @PathVariable final Long requestId) {
        return requestClient.getRequestById(userId, requestId);
    }
}
