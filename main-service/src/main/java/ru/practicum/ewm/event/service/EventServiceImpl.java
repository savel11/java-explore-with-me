package ru.practicum.ewm.event.service;

import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.StatisticsClient;
import ru.practicum.ewm.ViewStatsDto;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.storage.CategoryRepository;
import ru.practicum.ewm.error.exeptions.DateTimeException;
import ru.practicum.ewm.error.exeptions.InvalidFormatException;
import ru.practicum.ewm.error.exeptions.NotFoundException;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.event.dto.UpdateEventUserRequest;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.model.Location;
import ru.practicum.ewm.event.model.QEvent;
import ru.practicum.ewm.event.model.StateActionAdmin;
import ru.practicum.ewm.event.storage.EventRepository;
import ru.practicum.ewm.event.storage.LocationRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.storage.UserRepository;


import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final LocationRepository locationRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final StatisticsClient statisticsClient;
    private static final int STATS_PERIOD_YEARS = 1;

    @Override
    @Transactional
    public EventFullDto create(NewEventDto newEventDto, Long userId) {
        log.info("Пользователь с id = " + userId + " создает новое событие: " + newEventDto);
        log.trace("Проверка пользователя");
        User user = getUser(userId);
        log.trace("Пользователь является авторизованным!");
        Category category = getCategory(newEventDto.getCategory());
        log.trace("Проверка временных ограничений");
        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new DateTimeException("Дата события не может быть раньше, чем через 2 часа от текущего момента!");
        }
        Event event = EventMapper.toEvent(newEventDto, category, user);
        Optional<Location> locationOp = locationRepository.findByLonAndLat(newEventDto.getLocation().getLon(),
                newEventDto.getLocation().getLat());
        Location location = locationOp.orElseGet(() -> locationRepository.save(newEventDto.getLocation()));
        event.setLocation(location);
        if (event.getPaid() == null) {
            event.setPaid(false);
        }
        if (event.getRequestModeration() == null) {
            event.setRequestModeration(true);
        }
        if (event.getParticipantLimit() == null) {
            event.setParticipantLimit(0);
        }
        log.info("Событие успешно создано!");
        return EventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEventById(Long eventId, Long userId) {
        log.info("Получаем полную информацию о событии " + eventId + " добавленное пользователем с id = " + userId);
        getUser(userId);
        Event event = getEvent(eventId);
        if (!event.getInitiator().getId().equals(userId)) {
            throw new InvalidFormatException("Невозможно просмотреть собыите: " +
                    "Пользоваетль с id = " + userId + " не является создателем события!");
        }
        setViews(List.of(event));
        log.info("Информация о событие с id = " + eventId);
        return EventMapper.toEventFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto updateEventById(Long eventId, Long userId, UpdateEventUserRequest updateEventDto) {
        log.info("Пользователь с id = " + userId + " начинает процесс обновления события с id = " + eventId);
        Long locationId = null;
        Event event = getEvent(eventId);
        getUser(userId);
        if (!event.getInitiator().getId().equals(userId)) {
            throw new InvalidFormatException("Невозможно обновить собыите: " +
                    "Пользоваетль с id = " + userId + " не является создателем события!");
        }
        log.trace("Проверка статуса события");
        if (EventState.PUBLISHED.equals(event.getState())) {
            throw new InvalidFormatException("Нельзя редактировать опубликованные события!");
        }
        if (updateEventDto.getStateAction() != null) {
            switch (updateEventDto.getStateAction()) {
                case CANCEL_REVIEW -> event.setState(EventState.CANCELED);
                case SEND_TO_REVIEW -> event.setState(EventState.PENDING);
            }
        }
        if (updateEventDto.getAnnotation() != null) {
            event.setAnnotation(updateEventDto.getAnnotation());
        }
        if (updateEventDto.getCategory() != null) {
            Category category = getCategory(updateEventDto.getCategory());
            event.setCategory(category);
        }
        if (updateEventDto.getDescription() != null) {
            event.setDescription(updateEventDto.getDescription());
        }
        if (updateEventDto.getEventDate() != null) {
            if (updateEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                throw new DateTimeException("Дата события не может быть раньше, чем через 2 часа от текущего момента!");
            } else {
                event.setEventDate(updateEventDto.getEventDate());
            }
        }
        if (updateEventDto.getLocation() != null) {
            Optional<Location> locationOp = locationRepository.findByLonAndLat(updateEventDto.getLocation().getLon(),
                    updateEventDto.getLocation().getLat());
            Location location = locationOp.orElseGet(() -> locationRepository.save(updateEventDto.getLocation()));
            locationId = event.getLocation().getId();
            event.setLocation(location);
        }
        if (updateEventDto.getPaid() != null) {
            event.setPaid(updateEventDto.getPaid());
        }
        if (updateEventDto.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventDto.getParticipantLimit());
        }
        if (updateEventDto.getRequestModeration() != null) {
            event.setRequestModeration(updateEventDto.getRequestModeration());
        }

        if (updateEventDto.getTitle() != null && !updateEventDto.getTitle().isBlank()) {
            event.setTitle(updateEventDto.getTitle());
        }
        eventRepository.save(event);
        setViews(List.of(event));
        if (locationId != null) {
            if (eventRepository.countByLocationId(locationId) == 0) {
                locationRepository.deleteById(locationId);
            }
        }
        log.info("Событие успешно обновленно!");
        return EventMapper.toEventFullDto(event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getAllEventsByUserId(Long userId, int from, int size) {
        log.info("Получаем информацию о событиях добавленных пользователям с id =  " + userId);
        PageRequest pageRequest = PageRequest.of(from, size);
        List<Event> events = eventRepository.findByInitiatorId(userId, pageRequest);
        setViews(events);
        return events.stream()
                .map(EventMapper::toEventShortDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getPublishEventById(Long eventId) {
        log.info("Получение опубликованного события с id = " + eventId);
        Event event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Событие с id: " + eventId + " не найдено."));
        setViews(List.of(event));
        log.info("Информация о событие с id = " + eventId);
        return EventMapper.toEventFullDto(event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getAllPublishEvents(String text, List<Long> categories, Boolean isPaid,
                                                   LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                   Boolean isOnlyAvailable, String sort, int from, int size) {
        log.info("Получаем список опубликованных событий с выбраннми параметрами");
        List<Event> events;
        PageRequest pageRequest = PageRequest.of(from, size, Sort.by("eventDate").ascending());
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        booleanBuilder.and(QEvent.event.state.eq(EventState.PUBLISHED));
        if ((rangeStart != null) && (rangeEnd != null) && (rangeEnd.isBefore(rangeStart))) {
            throw new DateTimeException("Некорректный диапазон поиска! Дата начала должна быть раньше, чем дата конца!");
        }
        if (rangeStart == null && rangeEnd == null) {
            booleanBuilder.and(QEvent.event.eventDate.after(LocalDateTime.now()));
        }
        if (rangeStart != null) {
            booleanBuilder.and(QEvent.event.eventDate.after(rangeStart));
        }
        if (rangeEnd != null) {
            booleanBuilder.and(QEvent.event.eventDate.before(rangeEnd));
        }
        if (text != null && !text.isBlank()) {
            booleanBuilder.and(
                    QEvent.event.annotation.containsIgnoreCase(text)
                            .or(QEvent.event.description.containsIgnoreCase(text))
            );
        }
        if (categories != null && !categories.isEmpty()) {
            booleanBuilder.and(QEvent.event.category.id.in(categories));
        }
        if (isPaid != null) {
            booleanBuilder.and(QEvent.event.paid.eq(isPaid));
        }
        if (isOnlyAvailable) {
            booleanBuilder.and(
                    QEvent.event.participantLimit.eq(0)
                            .or(QEvent.event.participantLimit.gt(QEvent.event.confirmedRequests))
            );
        }
        if (booleanBuilder.getValue() != null) {
            events = eventRepository.findAll(booleanBuilder.getValue(), pageRequest).getContent();
        } else {
            events = eventRepository.findAll(pageRequest).getContent();
        }
        setViews(events);
        if ("VIEWS".equals(sort)) {
            events = events.stream()
                    .sorted(Comparator.comparing(Event::getViews).reversed())
                    .toList();
        }

        return events.stream()
                .map(EventMapper::toEventShortDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> getAllEventsForAdmin(List<Long> users, List<EventState> states, List<Long> categories,
                                                   LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {
        log.info("Получение событий подходящих под все переданные условия");
        List<Event> events;
        PageRequest pageRequest = PageRequest.of(from, size);
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        if (users != null && !users.isEmpty()) {
            booleanBuilder.and(QEvent.event.initiator.id.in(users));
        }
        if (states != null && !states.isEmpty()) {
            booleanBuilder.and(QEvent.event.state.in(states));
        }
        if (categories != null && !categories.isEmpty()) {
            booleanBuilder.and(QEvent.event.category.id.in(categories));
        }
        if ((rangeStart != null) && (rangeEnd != null) && (rangeEnd.isBefore(rangeStart))) {
            throw new DateTimeException("Некорректный диапазон поиска! Дата начала должна быть раньше, чем дата конца!");
        }
        if (rangeStart == null && rangeEnd == null) {
            booleanBuilder.and(QEvent.event.eventDate.after(LocalDateTime.now()));
        }
        if (rangeStart != null) {
            booleanBuilder.and(QEvent.event.eventDate.after(rangeStart));
        }
        if (rangeEnd != null) {
            booleanBuilder.and(QEvent.event.eventDate.before(rangeEnd));
        }
        if (booleanBuilder.getValue() != null) {
            events = eventRepository.findAll(booleanBuilder.getValue(), pageRequest).getContent();
        } else {
            events = eventRepository.findAll(pageRequest).getContent();
        }
        setViews(events);
        return events.stream()
                .map(EventMapper::toEventFullDto)
                .toList();
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateEventDto) {
        log.info("Обновление события с id = " + eventId);
        Event event = getEvent(eventId);
        if (updateEventDto.getStateAction() != null) {
            if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(1)) &&
                    StateActionAdmin.PUBLISH_EVENT.equals(updateEventDto.getStateAction())) {
                throw new InvalidFormatException("Дата начала изменяемого события должна быть не ранее чем за час от даты публикации");
            }
            if (StateActionAdmin.PUBLISH_EVENT.equals(updateEventDto.getStateAction())) {
                if (!EventState.PENDING.equals(event.getState())) {
                    throw new InvalidFormatException("Событие не может быть опубликовано, если оно не в состоянии ожидания!");
                }
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else if (StateActionAdmin.REJECT_EVENT.equals(updateEventDto.getStateAction())) {
                if (EventState.PUBLISHED.equals(event.getState())) {
                    throw new InvalidFormatException("Событие не может быть отменнено, если оно опубликованно!");
                }
                event.setState(EventState.CANCELED);
            }
        }
        Long locationId = null;
        if (updateEventDto.getAnnotation() != null) {
            event.setAnnotation(updateEventDto.getAnnotation());
        }
        if (updateEventDto.getCategory() != null) {
            Category category = getCategory(updateEventDto.getCategory());
            event.setCategory(category);
        }
        if (updateEventDto.getDescription() != null) {
            event.setDescription(updateEventDto.getDescription());
        }
        if (updateEventDto.getEventDate() != null) {
            if (updateEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                throw new DateTimeException("Дата события не может быть раньше, чем через 2 часа от текущего момента!");
            } else {
                event.setEventDate(updateEventDto.getEventDate());
            }
        }
        if (updateEventDto.getLocation() != null) {
            Optional<Location> locationOp = locationRepository.findByLonAndLat(updateEventDto.getLocation().getLon(),
                    updateEventDto.getLocation().getLat());
            Location location = locationOp.orElseGet(() -> locationRepository.save(updateEventDto.getLocation()));
            locationId = event.getLocation().getId();
            event.setLocation(location);
        }
        if (updateEventDto.getPaid() != null) {
            event.setPaid(updateEventDto.getPaid());
        }
        if (updateEventDto.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventDto.getParticipantLimit());
        }
        if (updateEventDto.getRequestModeration() != null) {
            event.setRequestModeration(updateEventDto.getRequestModeration());
        }

        if (updateEventDto.getTitle() != null && !updateEventDto.getTitle().isBlank()) {
            event.setTitle(updateEventDto.getTitle());
        }
        eventRepository.save(event);
        setViews(List.of(event));
        if (locationId != null) {
            if (eventRepository.countByLocationId(locationId) == 0) {
                locationRepository.deleteById(locationId);
            }
        }
        log.info("Событие успешно обновленно!");
        return EventMapper.toEventFullDto(event);
    }

    private User getUser(Long userId) {
        log.trace("Проверка пользователя");
        Optional<User> userOp = userRepository.findById(userId);
        if (userOp.isEmpty()) {
            log.warn("Пользователь не авторизован!");
            throw new NotFoundException("Пользователь не авторизован!");
        }
        log.trace("Пользователь является авторизованным!");
        return userOp.get();
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

    private Category getCategory(Long categoryId) {
        log.trace("Проверка существование категории события");
        Optional<Category> categoryOp = categoryRepository.findById(categoryId);
        if (categoryOp.isEmpty()) {
            log.warn("Категория события не найдена!");
            throw new NotFoundException("Указана не существующая категория события!");
        }
        log.trace("Категория найдена!");
        return categoryOp.get();
    }


    private void setViews(List<Event> events) {
        if (events.isEmpty()) {
            return;
        }
        List<String> uri = events.stream()
                .map(event -> "/events/" + event.getId())
                .toList();
        List<ViewStatsDto> viewStatsDto = statisticsClient.getStats(LocalDateTime.now().minusYears(STATS_PERIOD_YEARS),
                LocalDateTime.now(), uri, true);

        Map<String, Long> uriHitMap = viewStatsDto.stream()
                .collect(Collectors.toMap(ViewStatsDto::getUri, ViewStatsDto::getHits));
        for (Event event : events) {
            event.setViews(uriHitMap.getOrDefault("/events/" + event.getId(), 0L));
        }
    }
}
