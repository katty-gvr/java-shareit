package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.Collection;

@RestController
@Validated
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public BookingShortDto createNewBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                            @RequestBody final BookingShortDto bookingShortDto) {
        return bookingService.createBooking(userId, bookingShortDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingShortDto approveBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                          @PathVariable Long bookingId,
                                          @RequestParam Boolean approved) {
        return bookingService.approveBooking(bookingId, userId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingShortDto getBooking(@PathVariable final Long bookingId,
                                      @RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingService.getBooking(bookingId, userId);

    }

    @GetMapping
    public Collection<BookingShortDto> getUserBookings(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                       @RequestParam(defaultValue = "ALL")String state,
                                                       @RequestParam(value = "from", required = false, defaultValue = "0")
                                                      //@PositiveOrZero(message = "Значение 'from' должно быть положительным")
                                                      final Integer from,
                                                       @RequestParam(value = "size", required = false, defaultValue = "10")
                                                      //@Positive(message = "Значение 'size' должно быть положительным")
                                                      final Integer size) {
        return bookingService.getAllBookingsByUser(userId, state, from, size);
    }

    @GetMapping("/owner")
    public Collection<BookingShortDto> getBookingsForUserItems(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                               @RequestParam(defaultValue = "ALL")String state,
                                                               @RequestParam(value = "from", required = false, defaultValue = "0")
                                                              //@PositiveOrZero(message = "Значение 'from' должно быть положительным")
                                                              final Integer from,
                                                               @RequestParam(value = "size", required = false, defaultValue = "10")
                                                              //@Positive(message = "Значение 'size' должно быть положительным")
                                                              final Integer size) {
        return bookingService.getBookingsForUserItems(userId, state, from, size);
    }
}
