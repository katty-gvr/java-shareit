package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.CommentNotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentShortDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@RequiredArgsConstructor
public class CommentServiceImplTest {
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentMapper commentMapper;
    @InjectMocks
    private CommentServiceImpl commentService;
    private final User user = new User(1L, "user", "user@mail.ru");
    private final Item item = Item.builder().id(1L).name("item2Name").description("item2Desc").available(true).owner(user)
            .requestId(1L).build();
    private final CommentShortDto commentShortDto = CommentShortDto.builder().id(1L).text("comment1").itemId(item.getId())
            .authorName(user.getName()).created(LocalDateTime.now()).build();
    private final Comment comment = Comment.builder().id(1L).text("comment1").item(item).author(user)
            .created(LocalDateTime.now()).build();
    private final CommentDto commentDto = CommentDto.builder().id(1L).text("comment1").item(ItemMapper.toItemDto(item))
            .authorName(user.getName()).created(comment.getCreated()).build();

    @Test
    public void testAddNewComment() {
        when(bookingRepository.existsBookingByItemAndBookerAndStatusNotAndStart(any(), any(), any())).thenReturn(true);
        when(commentMapper.toComment(any(), anyLong(), anyLong())).thenReturn(comment);
        when(commentMapper.toCommentDto(any())).thenReturn(commentDto);
        when(commentRepository.save(comment)).thenReturn(comment);

        CommentDto addedComment = commentService.addNewComment(commentShortDto, item.getId(), user.getId());

        assertEquals(comment.getText(), addedComment.getText());
        assertEquals(comment.getId(), addedComment.getId());
        assertEquals(comment.getAuthor().getName(), addedComment.getAuthorName());
        assertEquals(comment.getItem().getId(), addedComment.getItem().getId());

        verify(bookingRepository).existsBookingByItemAndBookerAndStatusNotAndStart(eq(item), eq(user), any());
        verify(commentRepository).save(comment);
    }

    @Test
    public void testAddNewCommentWithWrongBooking() {
        when(bookingRepository.existsBookingByItemAndBookerAndStatusNotAndStart(any(), any(), any())).thenReturn(false);
        when(commentMapper.toComment(any(), anyLong(), anyLong())).thenReturn(comment);

        assertThrows(BadRequestException.class, () -> commentService.addNewComment(commentShortDto, item.getId(), user.getId()));

        verify(bookingRepository).existsBookingByItemAndBookerAndStatusNotAndStart(eq(item), eq(user), any());
        verify(commentRepository, never()).save(comment);
    }

    @Test
    public void testGetCommentById() {
        when(commentRepository.findById(anyLong())).thenReturn(Optional.of(comment));
        when(commentMapper.toCommentDto(any())).thenReturn(commentDto);

        CommentDto actualCommentDto = commentService.getCommentById(comment.getId());

        assertEquals(actualCommentDto, commentDto);
    }

    @Test
    public void testGetCommentWithWrongId() {
        when(commentRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(CommentNotFoundException.class, () -> commentService.getCommentById(comment.getId()));
    }
}
