package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface CommentService {

    Comment getCommentById(Long commentId);

    Comment addNewComment(Comment comment);

    List<Comment> getItemComments(Item item);
}
