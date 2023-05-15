package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ItemRepository itemRepository;
    @Override
    @Transactional
    public BookingDto createBooking(Long userId, BookingShortDto bookingShortDto) {
        User booker = UserMapper.toUser(userService.getUserById(userId));
        Item item = itemRepository.findById(bookingShortDto.getItemId()).orElseThrow(() ->
                new ItemNotFoundException(String.format("Вещь не найдена", bookingShortDto.getItemId())));
        if (!item.getAvailable()) {
            throw new BookingException("Данная вещь недоступна для бронирования!");
        }
        if (item.getOwner().getId().equals(userId)) {
            throw new BookingNotFoundException("Нелья сделать бронирование на собственную вещь");
        }
        Booking booking = BookingMapper.toBooking(bookingShortDto);
        validateBookingTime(booking);
        booking.setBooker(booker);
        booking.setItem(item);
        bookingRepository.save(booking);
        log.info(String.format("Пользователь с id=%d забронировал вещь с id=%d", booker.getId(), item.getId()));
        return BookingMapper.toBookingDto(booking);
    }

    @Override
    @Transactional
    public BookingDto approveBooking(Long bookingId, Long ownerId, Boolean isApproved) {
        Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);
        Booking booking = bookingOptional.orElseThrow(() ->
                new BookingNotFoundException(String.format("Бронирование с id=%d не найдено", bookingId)));

        if (booking.getStatus().equals(BookingStatus.APPROVED)) {
            throw new BookingException("Бронирование было подтверждено ранее");

        }
       if (booking.getBooker().getId().equals(ownerId)) {
           throw new BookingNotFoundException(String.format("Пользователь с id=%d не является владельцем вещи с бронированием id=%d", ownerId, bookingId));
       }
       booking.setStatus(isApproved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
       bookingRepository.save(booking);
       log.info(String.format("Пользователь с id=%d подтвердил бронирование вещи с id=%d", ownerId, bookingId));

       return BookingMapper.toBookingDto(booking);
    }

    @Override
    @Transactional
    public BookingDto getBooking(Long bookingId, Long userId) {
        Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);
        Booking booking = bookingOptional.orElseThrow(() -> new BookingNotFoundException(String.format("Бронирование с id=%d не найдено", bookingId)));
        if (!booking.getItem().getOwner().getId().equals(userId) && !booking.getBooker().getId().equals(userId)) {
            throw new BookingNotFoundException(String.format("Пользователь с id=%d не является хозяином вещи и не делал бронирование id=%d", userId, bookingId));
        }
        log.info(String.format("Бронирование id=%d успешно получено пользователем id=%d", bookingId, userId));
        return BookingMapper.toBookingDto(booking);
    }

    @Override
    @Transactional
    public Collection<BookingDto> getAllBookingsByUser(Long userId, String state) {
        StateOfBookingRequest stateIn = getState(state);
        User user = UserMapper.toUser(userService.getUserById(userId));
        List<Booking> userBookings = bookingRepository.findByBooker(user);
        return getBookingsByState(userBookings, stateIn).stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Collection<BookingDto> getBookingsForUserItems(Long userId, String state) {
        StateOfBookingRequest stateIn = getState(state);
        User user = UserMapper.toUser(userService.getUserById(userId));
        List<Booking> userBookings = bookingRepository.findByItem_Owner(user);
        return getBookingsByState(userBookings, stateIn).stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<Booking> getItemBookings(Item item) {
        List<Booking> bookings = bookingRepository.findByItem(item);
        return new ArrayList<>(getBookingsByState(bookings, StateOfBookingRequest.ALL));
    }

    private void validateBookingTime(Booking booking) {
        if (booking.getEnd() == null || booking.getStart() == null) {
            throw new BookingException("Время начала и окончания бронирования должно быть задано");
        }
        if (booking.getEnd().isBefore(LocalDateTime.now()) || booking.getEnd().isBefore(booking.getStart())) {
            throw new BookingException("Время конца бронирования указано некорректно.");
        }
        if (booking.getStart().isBefore(LocalDateTime.now()) || booking.getStart().isAfter(booking.getEnd())) {
            throw new BookingException("Время начала бронирования указано некорректно.");
        }
        if (booking.getStart().equals(booking.getEnd())) {
            throw new BookingException("Время начала бронирования указано некорректно.");
        }
    }

    private Collection<Booking> getBookingsByState(List<Booking> allBookings, StateOfBookingRequest state) {
        Stream<Booking> bookingStream = allBookings.stream();
        LocalDateTime now = LocalDateTime.now();
        switch (state) {
            case CURRENT:
                bookingStream = bookingStream.filter(booking -> booking.getStart().isBefore(now) &&
                        booking.getEnd().isAfter(now));
                break;
            case PAST:
                bookingStream = bookingStream.filter(booking -> booking.getEnd().isBefore(now));
                break;
            case FUTURE:
                bookingStream = bookingStream.filter(booking -> booking.getStart().isAfter(now));
                break;
            case WAITING:
                bookingStream = bookingStream.filter(booking -> booking.getStatus().equals(BookingStatus.WAITING));
                break;
            case REJECTED:
                bookingStream = bookingStream.filter(booking -> booking.getStatus().equals(BookingStatus.REJECTED));
                break;
        }
        return bookingStream.sorted(Comparator.comparing(Booking::getStart).reversed()).collect(Collectors.toList());
    }

    private StateOfBookingRequest getState(String state) {
        try {
            return StateOfBookingRequest.valueOf(state);
        } catch (Throwable e) {
            throw new BadRequestException("Unknown state: " + state);
        }
    }
}
