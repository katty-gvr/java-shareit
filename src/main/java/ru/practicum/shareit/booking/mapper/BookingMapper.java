package ru.practicum.shareit.booking.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;

import java.time.LocalDateTime;

import static ru.practicum.shareit.common.DateTimeParameters.DATE_TIME_FORMAT;

@Component
@RequiredArgsConstructor
public class BookingMapper {
    public static BookingDto toBookingDto(Booking booking) {
        ItemDto itemDto = ItemMapper.toItemDto(booking.getItem());
        UserDto bookerDto = UserMapper.toUserDto(booking.getBooker());
        return BookingDto.builder()
                .id(booking.getId())
                .start(LocalDateTime.parse(booking.getStart().format(DATE_TIME_FORMAT)))
                .end(LocalDateTime.parse(booking.getEnd().format(DATE_TIME_FORMAT)))
                .start(booking.getStart())
                .end(booking.getEnd())
                .item(itemDto)
                .booker(bookerDto)
                .status(booking.getStatus())
                .build();
    }

    public static BookingShortDto toShortBookingDto(Booking booking) {
        return BookingShortDto.builder()
                .id(booking.getId())
                .start(LocalDateTime.parse(booking.getStart().format(DATE_TIME_FORMAT)))
                .end(LocalDateTime.parse(booking.getEnd().format(DATE_TIME_FORMAT)))
                .start(booking.getStart())
                .end(booking.getEnd())
                .itemId(booking.getItem().getId())
                .bookerId(booking.getBooker().getId())
                .status(booking.getStatus().name())
                .build();
    }

    public static Booking toBooking(BookingShortDto bookingShortDto) {
        return Booking.builder()
                .id(bookingShortDto.getId())
                .start(bookingShortDto.getStart())
                .end(bookingShortDto.getEnd())
                .status(BookingStatus.WAITING)
                .build();
    }
}
