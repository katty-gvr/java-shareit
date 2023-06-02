package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BookingException;
import ru.practicum.shareit.exception.BookingNotFoundException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceImplTest {
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private BookingServiceImpl bookingService;
    private final LocalDateTime now = LocalDateTime.now();
    private final User owner = new User(1L, "user", "user@mail.ru");
    private final User booker = new User(2L, "userBooker", "booker@mail.ru");
    private final Item item = Item.builder().id(1L).name("item2Name").description("item2Desc")
            .available(true).owner(owner).requestId(1L).build();
    private final BookingShortDto bookingShortDto = BookingShortDto.builder().id(1L).start(now.plusHours(1))
            .end(now.plusHours(2)).itemId(1L).bookerId(2L).build();
    private Booking booking;

    @BeforeEach
    void init() {
        booking = BookingMapper.toBooking(bookingShortDto);
        booking.setItem(item);
        booking.setBooker(booker);
    }

    @Test
    void testCreateBooking() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.save(any())).thenReturn(booking);

        BookingDto actualBookingDto = bookingService.createBooking(booker.getId(), bookingShortDto);

        assertEquals(bookingShortDto.getId(), actualBookingDto.getId());
        assertNotNull(actualBookingDto.getStart());
        assertNotNull(actualBookingDto.getEnd());
        assertEquals(bookingShortDto.getBookerId(), actualBookingDto.getBooker().getId());
        assertEquals(bookingShortDto.getItemId(), actualBookingDto.getItem().getId());
        assertEquals(actualBookingDto.getStatus(), BookingStatus.WAITING);

        verify(bookingRepository).save(any());
    }

    @Test
    void testCreateBookingWithWrongBookerId() {
        Long wrongBookerId = 100L;
        when(userRepository.findById(wrongBookerId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> bookingService.createBooking(wrongBookerId, bookingShortDto));

        verify(userRepository).findById(wrongBookerId);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void testCreateBookingWithWrongItemId() {
        Long wrongItemId = 100L;
        bookingShortDto.setItemId(wrongItemId);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(wrongItemId)).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class, () -> bookingService.createBooking(anyLong(), bookingShortDto));

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void testCreateBookingWhenItemIsNotAvailable() {
        item.setAvailable(false);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        assertThrows(BookingException.class, () -> bookingService.createBooking(anyLong(), bookingShortDto));

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void testCreateBookingWhenOwnerIsEqualBooker() {
        item.setOwner(booker);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        assertThrows(BookingNotFoundException.class, () -> bookingService.createBooking(booker.getId(), bookingShortDto));

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void testApproveBooking() {
        Long bookingItemOwner = booking.getItem().getOwner().getId();

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        BookingDto actualDto = bookingService.approveBooking(booking.getId(), bookingItemOwner, true);

        assertEquals(booking.getId(), actualDto.getId());
        assertEquals(booking.getStart(), actualDto.getStart());
        assertEquals(booking.getEnd(), actualDto.getEnd());
        assertEquals(booking.getItem().getId(), actualDto.getItem().getId());
        assertEquals(booking.getBooker().getId(), actualDto.getBooker().getId());
        assertEquals(actualDto.getStatus(), BookingStatus.APPROVED);

        verify(bookingRepository).save(booking);
    }

    @Test
    void testApproveBookingWhenItemOwnerWantsReject() {
        booking.setItem(item);
        booking.setBooker(booker);

        Long bookingItemOwner = booking.getItem().getOwner().getId();
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        BookingDto actualDto = bookingService.approveBooking(booking.getId(), bookingItemOwner, false);

        assertEquals(booking.getId(), actualDto.getId());
        assertEquals(booking.getStart(), actualDto.getStart());
        assertEquals(booking.getEnd(), actualDto.getEnd());
        assertEquals(booking.getItem().getId(), actualDto.getItem().getId());
        assertEquals(booking.getBooker().getId(), actualDto.getBooker().getId());
        assertEquals(actualDto.getStatus(), BookingStatus.REJECTED);

        verify(bookingRepository).save(booking);
    }

    @Test
    void testApproveBookingWithWrongBookingId() {
        Long wrongBookingId = 100L;
        when(bookingRepository.findById(wrongBookingId)).thenReturn(Optional.empty());

        assertThrows(BookingNotFoundException.class, () ->
                bookingService.approveBooking(wrongBookingId, booking.getItem().getOwner().getId(), true));

        verify(bookingRepository).findById(wrongBookingId);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void testApproveBookingWithWrongBookingStatus() {
        Long bookingItemOwner = booking.getItem().getOwner().getId();
        booking.setStatus(BookingStatus.REJECTED);

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        assertThrows(BookingException.class, () ->
                bookingService.approveBooking(booking.getId(), bookingItemOwner, true));

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void testApproveBookingWithWrongItemOwner() {
        User notItemOwner = new User(3L, "User3", "user3@mail.ru");

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        assertThrows(BookingNotFoundException.class, () ->
                bookingService.approveBooking(booking.getId(), notItemOwner.getId(), true));

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void testGetBookingByItemOwner() {
        Long bookingItemOwner = booking.getItem().getOwner().getId();

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        BookingDto actualDto = bookingService.getBooking(booking.getId(), bookingItemOwner);

        assertEquals(booking.getId(), actualDto.getId());
        assertEquals(booking.getStart(), actualDto.getStart());
        assertEquals(booking.getEnd(), actualDto.getEnd());
        assertEquals(booking.getItem().getId(), actualDto.getItem().getId());
        assertEquals(booking.getBooker().getId(), actualDto.getBooker().getId());
        assertEquals(actualDto.getStatus(), BookingStatus.WAITING);

        verify(bookingRepository).findById(booking.getId());
    }

    @Test
    void testGetBookingByBooker() {
        Long bookerId = booking.getBooker().getId();
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        BookingDto actualDto = bookingService.getBooking(booking.getId(), bookerId);

        assertEquals(booking.getId(), actualDto.getId());
        assertEquals(booking.getStart(), actualDto.getStart());
        assertEquals(booking.getEnd(), actualDto.getEnd());
        assertEquals(booking.getItem().getId(), actualDto.getItem().getId());
        assertEquals(booking.getBooker().getId(), actualDto.getBooker().getId());
        assertEquals(actualDto.getStatus(), BookingStatus.WAITING);

        verify(bookingRepository).findById(booking.getId());
    }

    @Test
    void testGetBookingByOtherUser() {
        User otherUser = new User(4L, "user4", "user4@mail.ru");

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        assertThrows(BookingNotFoundException.class, () -> bookingService.getBooking(booking.getId(), otherUser.getId()));
    }

    @Test
    void testGetAllBookingsByUser() {
        List<Booking> userBookings = new ArrayList<>();
        userBookings.add(new Booking(1L, now.plusHours(1), now.plusHours(2), item, booker, BookingStatus.WAITING));
        userBookings.add(new Booking(2L, now.plusHours(3), now.plusHours(4), item, booker, BookingStatus.APPROVED));

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBooker(any(User.class), any(Pageable.class))).thenReturn(userBookings);

        Collection<BookingDto> results = bookingService.getAllBookingsByUser(booker.getId(), "WAITING", 0, 10);

        assertEquals(1, results.size());
        assertEquals(BookingStatus.WAITING, results.iterator().next().getStatus());

        verify(userRepository).findById(booker.getId());
    }

    @Test
    void testGetBookingsForUserItems() {
        List<Booking> userBookings = new ArrayList<>();
        userBookings.add(new Booking(1L, now.plusHours(1), now.plusHours(2), item, booker, BookingStatus.WAITING));
        userBookings.add(new Booking(2L, now.plusHours(3), now.plusHours(4), item, booker, BookingStatus.APPROVED));

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository.findByItem_Owner(any(User.class), any(Pageable.class))).thenReturn(userBookings);

        Collection<BookingDto> results = bookingService.getBookingsForUserItems(owner.getId(), "WAITING", 0, 10);

        assertEquals(1, results.size());
        assertEquals(BookingStatus.WAITING, results.iterator().next().getStatus());
    }
}
