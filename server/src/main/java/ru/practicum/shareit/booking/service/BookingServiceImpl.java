package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BookingDto createBooking(Long userId, BookingShortDto bookingShortDto) {
        User booker = userRepository.findById(userId).orElseThrow(() ->
                new UserNotFoundException(String.format("Пользователь с id=%d не найден", userId)));
        Item item = itemRepository.findById(bookingShortDto.getItemId()).orElseThrow(() ->
                new ItemNotFoundException(String.format("Вещь c id=%d не найдена", bookingShortDto.getItemId())));
        if (!item.getAvailable()) {
            throw new BookingException("Данная вещь недоступна для бронирования!");
        }
        if (item.getOwner().getId().equals(userId)) {
            throw new BookingNotFoundException("Нелья сделать бронирование на собственную вещь");
        }
        validateBookingTime(bookingShortDto.getStart(), bookingShortDto.getEnd());
        Booking booking = BookingMapper.toBooking(bookingShortDto);
        booking.setBooker(booker);
        booking.setItem(item);
        bookingRepository.save(booking);
        log.info("Пользователь с id={} забронировал вещь с id={}", booker.getId(), item.getId());
        return BookingMapper.toBookingDto(booking);
    }

    @Override
    @Transactional
    public BookingDto approveBooking(Long bookingId, Long ownerId, Boolean isApproved) {
        Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);
        Booking booking = bookingOptional.orElseThrow(() ->
                new BookingNotFoundException(String.format("Бронирование с id=%d не найдено", bookingId)));

        if (!booking.getStatus().equals(BookingStatus.WAITING)) {
            throw new BookingException("Бронирование было подтверждено ранее или отменено");
        }
        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new BookingNotFoundException(String.format("Пользователь с id=%d не является владельцем вещи с бронированием id=%d", ownerId, bookingId));
        }
        booking.setStatus(isApproved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        bookingRepository.save(booking);
        log.info("Пользователь с id={} подтвердил бронирование вещи с id={}", ownerId, bookingId);

        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public BookingDto getBooking(Long bookingId, Long userId) {
        Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);
        Booking booking = bookingOptional.orElseThrow(() -> new BookingNotFoundException(String.format("Бронирование с id=%d не найдено", bookingId)));
        if (!booking.getItem().getOwner().getId().equals(userId) && !booking.getBooker().getId().equals(userId)) {
            throw new BookingNotFoundException(String.format("Пользователь с id=%d не является хозяином вещи и не делал бронирование id=%d", userId, bookingId));
        }
        log.info("Бронирование id={} успешно получено пользователем id={}", bookingId, userId);
        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public Collection<BookingDto> getAllBookingsByUser(Long userId, String state, Integer from, Integer size) {
        StateOfBookingRequest stateIn = getState(state);
        User user = userRepository.findById(userId).orElseThrow();

        PageRequest page = PageRequest.of(from / size, size, Sort.by("start").descending());
        List<Booking> userBookings = bookingRepository.findByBooker(user, page);
        log.info("Список всех бронирований со статусом {} пользователя с id={} успешно получен", state, userId);
        return getBookingsByState(userBookings, stateIn).stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
    }

    @Override
    public Collection<BookingDto> getBookingsForUserItems(Long userId, String state, Integer from, Integer size) {
        StateOfBookingRequest stateIn = getState(state);
        User user = userRepository.findById(userId).orElseThrow();

        PageRequest page = PageRequest.of(from / size, size, Sort.by("start").descending());
        List<Booking> userBookings = bookingRepository.findByItem_Owner(user, page);
        log.info("Список бронирований со статусом {} для вещей пользователя с id={} успешно получен", state, userId);
        return getBookingsByState(userBookings, stateIn).stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
    }

    private void validateBookingTime(LocalDateTime start, LocalDateTime end) {
        LocalDateTime now = LocalDateTime.now();
        if (end == null || start == null) {
            throw new BookingException("Время начала и окончания бронирования должно быть задано");
        }
        if (end.isBefore(now) || end.isBefore(start)) {
            throw new BookingException("Время конца бронирования указано некорректно.");
        }
        if (start.isBefore(now) || start.isAfter(end)) {
            throw new BookingException("Время начала бронирования указано некорректно.");
        }
        if (start.equals(end)) {
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
