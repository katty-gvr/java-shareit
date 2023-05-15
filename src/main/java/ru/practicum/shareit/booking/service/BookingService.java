package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.List;

public interface BookingService {
    BookingDto createBooking(Long userId, BookingShortDto bookingShortDtoDto);

    BookingDto approveBooking(Long bookingId, Long ownerId, Boolean isApproved);

    BookingDto getBooking(Long bookingId, Long userId);

    Collection<BookingDto> getAllBookingsByUser(Long userId, String state);

    Collection<BookingDto> getBookingsForUserItems(Long userId, String state);

    List<Booking> getItemBookings(Item item);
}
