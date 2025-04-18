package ru.practicum.ewm.comment.service;

import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.dto.NewCommentDto;

import java.util.List;

public interface CommentService {
    CommentDto createComment(NewCommentDto newCommentDto, Long eventId, Long userId);

    CommentDto updateComment(NewCommentDto newCommentDto, Long userId, Long commentId);

    List<CommentDto> getCommentsByEvent(Long eventId, int from, int size);

    List<CommentDto> getCommentsByAuthor(Long userId, int from, int size);

    void deleteCommentByAuthor(Long commentId, Long userId);

    void deleteCommentByAdmin(Long commentId);
}
