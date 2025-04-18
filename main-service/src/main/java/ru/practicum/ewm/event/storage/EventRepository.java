package ru.practicum.ewm.event.storage;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>, QuerydslPredicateExecutor<Event> {
    Long countByLocationId(Long locationId);

    Optional<Event> findByIdAndState(Long id, EventState eventState);


    List<Event> findAllByIdIn(List<Long> ids);

    boolean existsByCategoryId(Long categoryId);

    List<Event> findByInitiatorId(Long userId, PageRequest pageRequest);

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long userId);
}
