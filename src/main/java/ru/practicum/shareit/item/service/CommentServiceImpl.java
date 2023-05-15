package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.CommentNotFoundException;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;

    @Override
    @Transactional
    public Comment getCommentById(Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(() ->
                new CommentNotFoundException(String.format("Комментарий с id=%d не найден", commentId)));
    }

    @Override
    @Transactional
    public Comment addNewComment(Comment comment) {
        if (!bookingRepository.existsBookingByItemAndBookerAndStatusNotAndStart(comment.getItem(),
                comment.getAuthor(), LocalDateTime.now())) {
            throw new BadRequestException("Нелья оставить комменатрий к вещи, если она не была взята в аренду" +
                            "или аренда еще не началась");
        }
        comment.setCreated(LocalDateTime.now());
        log.info(String.format("Пользователь id=%d добавил комментарий id=%d к вещи id=%d",
                comment.getAuthor().getId(), comment.getId(), comment.getItem().getId()));
        return commentRepository.save(comment);
    }

    @Override
    @Transactional
    public List<Comment> getItemComments(Item item) {
        return commentRepository.findByItemOrderByIdAsc(item);
    }
}
