package ru.practicum.ewm.comment.storage;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.comment.model.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findAllByAuthorId(Long userId, Pageable pageable);

    Page<Comment> findAllByEventId(Long eventId, Pageable pageable);
}
