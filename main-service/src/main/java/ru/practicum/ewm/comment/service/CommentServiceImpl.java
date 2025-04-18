package ru.practicum.ewm.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.dto.NewCommentDto;
import ru.practicum.ewm.comment.mapper.CommentMapper;
import ru.practicum.ewm.comment.model.Comment;
import ru.practicum.ewm.comment.storage.CommentRepository;
import ru.practicum.ewm.error.exeptions.InvalidFormatException;
import ru.practicum.ewm.error.exeptions.NotFoundException;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.storage.EventRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private static final int COMMENT_EDIT_TIMEOUT_MINUTES = 30;

    @Override
    @Transactional
    public CommentDto createComment(NewCommentDto newCommentDto, Long eventId, Long userId) {
        log.info("Создание комментария к событию с id " + eventId + " пользователем с id " + userId);
        User user = getUser(userId);
        Event event = getEvent(eventId);
        Comment comment = commentRepository.save(CommentMapper.toComment(newCommentDto, user, event));
        log.info("Комментарий успешно добавлен!");
        return CommentMapper.toCommentDto(comment);
    }

    @Override
    @Transactional
    public CommentDto updateComment(NewCommentDto newCommentDto, Long userId, Long commentId) {
        log.info("Обновление комментария с id " + commentId + " пользователем с id " + userId);
        User user = getUser(userId);
        Comment comment = getComment(commentId);
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new InvalidFormatException("Обновить комментарий может только автор!");
        }
        if (comment.getCreatedOn().plusMinutes(COMMENT_EDIT_TIMEOUT_MINUTES).isBefore(LocalDateTime.now())) {
            throw new InvalidFormatException("Комментраий не обновлен: Обновить комментарий можно только в течение: " + COMMENT_EDIT_TIMEOUT_MINUTES + " минут!");
        }
        comment.setText(newCommentDto.getText());
        comment = commentRepository.save(comment);
        log.info("Комментарий успешно обновлен!");
        return CommentMapper.toCommentDto(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsByEvent(Long eventId, int from, int size) {
        Event event = getEvent(eventId);
        Pageable pageable = PageRequest.of(from, size);
        Page<Comment> commentPage = commentRepository.findAllByEventId(eventId, pageable);
        return commentPage.getContent().stream()
                .map(CommentMapper::toCommentDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsByAuthor(Long userId, int from, int size) {
        User user = getUser(userId);
        Pageable pageable = PageRequest.of(from, size);
        Page<Comment> commentPage = commentRepository.findAllByAuthorId(userId, pageable);
        return commentPage.getContent().stream()
                .map(CommentMapper::toCommentDto)
                .toList();
    }

    @Override
    public void deleteCommentByAuthor(Long commentId, Long userId) {
        log.info("Пользователь с id = " + userId + " удаляет комментарий с id = " + commentId);
        Comment comment = getComment(commentId);
        getUser(userId);
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new InvalidFormatException("Комментарий может удалить только автор!");
        }
        commentRepository.deleteById(commentId);
        log.info("Комментарий успешно удален!");
    }

    @Override
    public void deleteCommentByAdmin(Long commentId) {
        Comment comment = getComment(commentId);
        commentRepository.deleteById(commentId);
        log.info("Комментарий успешно удален!");
    }

    private User getUser(Long userId) {
        log.trace("Проверка существования пользователя");
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new NotFoundException("Пользователя с id = " + userId + " не существует");
        }
        log.trace("Проверка закончена: Пользователь существует");
        return optionalUser.get();
    }

    private Event getEvent(Long eventId) {
        log.trace("Проверка существование события с id = " + eventId);
        Optional<Event> eventOp = eventRepository.findById(eventId);
        if (eventOp.isEmpty()) {
            log.warn("Событие не найденно!");
            throw new NotFoundException("Собитие c id = " + eventId + " не найденно!");
        }
        return eventOp.get();
    }

    private Comment getComment(Long commentId) {
        log.trace("Проверка существование комментарий с id = " + commentId);
        Optional<Comment> commentOp = commentRepository.findById(commentId);
        if (commentOp.isEmpty()) {
            log.warn("Комментарий не найденно!");
            throw new NotFoundException("Комментраий c id = " + commentId + " не найден!");
        }
        return commentOp.get();
    }
}
