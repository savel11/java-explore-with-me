package ru.practicum.ewm.event.service;

import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.event.dto.UpdateEventUserRequest;
import ru.practicum.ewm.event.model.EventState;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    EventFullDto create(NewEventDto newEventDto, Long userId);

    EventFullDto getEventById(Long eventId, Long userId);

    EventFullDto updateEventById(Long eventId, Long userId, UpdateEventUserRequest updateEventDto);

    List<EventShortDto> getAllEventsByUserId(Long userId, int from, int size);

    EventFullDto getPublishEventById(Long eventId);

    List<EventShortDto> getAllPublishEvents(String text, List<Long> categories, Boolean isPaid,
                                            LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean isOnlyAvailable,
                                            String sort, int from, int size);

    List<EventFullDto> getAllEventsForAdmin(List<Long> users, List<EventState> states, List<Long> categories,
                                            LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size);

    EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateEventAdminRequest);
}
