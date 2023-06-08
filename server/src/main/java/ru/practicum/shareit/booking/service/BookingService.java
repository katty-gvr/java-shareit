package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingShortDto;

import java.util.Collection;

public interface BookingService {
    BookingShortDto createBooking(Long userId, BookingShortDto bookingShortDtoDto);

    BookingShortDto approveBooking(Long bookingId, Long ownerId, Boolean isApproved);

    BookingShortDto getBooking(Long bookingId, Long userId);

    Collection<BookingShortDto> getAllBookingsByUser(Long userId, String state, Integer from, Integer size);

    Collection<BookingShortDto> getBookingsForUserItems(Long userId, String state, Integer from, Integer size);
}
