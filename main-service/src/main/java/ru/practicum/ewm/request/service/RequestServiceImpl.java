package ru.practicum.ewm.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.error.exeptions.DuplicatedDataException;
import ru.practicum.ewm.error.exeptions.InvalidFormatException;
import ru.practicum.ewm.error.exeptions.NotFoundException;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.storage.EventRepository;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.mapper.RequestMapper;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.request.model.Status;
import ru.practicum.ewm.request.storage.RequestRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.storage.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public ParticipationRequestDto create(Long userId, Long eventId) {
        log.info("Пользователь с id = " + userId + " создает заявку на участие в событие с id = " + eventId);
        User user = getUser(userId);
        Event event = getEvent(eventId);
        Optional<Request> requestOp = requestRepository.findByRequesterIdAndEventId(userId, eventId);
        if (requestOp.isPresent()) {
            throw new DuplicatedDataException("Заявка уже была создана!");
        }
        if (event.getInitiator().equals(user)) {
            throw new InvalidFormatException("Заявка не содана: Инициатор не может отправлять заявку на участия" +
                    " в своем же собитии!");
        }
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new InvalidFormatException("Заявка не содана: Событие еще не опубликованно!");
        }
        List<Request> confirmedRequests = requestRepository.findAllByStatusAndEventId(Status.CONFIRMED, eventId);
        if (!event.getParticipantLimit().equals(0) && event.getParticipantLimit() == confirmedRequests.size()) {
            throw new InvalidFormatException("Заявка не содана: Лимит запросов для события превышен!");
        }
        Status status;
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            status = Status.CONFIRMED;
        } else {
            status = Status.PENDING;
        }
        Request request = RequestMapper.toRequest(user, event, status);
        return RequestMapper.toParticipationRequestDto(requestRepository.save(request));
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult update(Long userId, Long eventId, EventRequestStatusUpdateRequest updateRequest) {
        log.info("Пользователь с id = " + userId + " редактирует заявки на участие в событие с id = " + eventId);
        User user = getUser(userId);
        Event event = getEvent(eventId);
        boolean isFull = false;
        List<Request> confirmedRequests = requestRepository.findAllByStatusAndEventId(Status.CONFIRMED, eventId);
        int numberConfirmedRequests = confirmedRequests.size();
        if (!event.getParticipantLimit().equals(0) && event.getParticipantLimit() == numberConfirmedRequests) {
            throw new InvalidFormatException("Заявки не могут быть одобренны: Лимит запросов для события превышен!");
        }
        List<Request> requests = requestRepository.findAllByIdIn(updateRequest.getRequestIds());
        List<Request> savedRequests = new ArrayList<>();
        if (!requests.isEmpty()) {
            if (requests.stream()
                    .map(Request::getStatus)
                    .anyMatch(status -> !status.equals(Status.PENDING))) {
                throw new InvalidFormatException("Статус заяки можно изменять только в состояние ожидание");
            }
            if (updateRequest.getStatus().equals(Status.CONFIRMED)) {
                if (event.getParticipantLimit() < numberConfirmedRequests + updateRequest.getRequestIds().size()) {
                    log.warn("Лимит запросов для данного события превышен!");
                    throw new InvalidFormatException("Заявки не могут быть одобренны: Лимит запросов для события превышен!");
                } else if (event.getParticipantLimit() == numberConfirmedRequests + updateRequest.getRequestIds().size()) {
                    isFull = true;
                }
            }
            requests.forEach(request -> request.setStatus(updateRequest.getStatus()));
            savedRequests = requestRepository.saveAll(requests);
        }
        List<ParticipationRequestDto> confirmedRequestsList = new ArrayList<>();
        List<ParticipationRequestDto> rejectedRequestsList = new ArrayList<>();
        if (updateRequest.getStatus().equals(Status.CONFIRMED)) {
            for (Request request : savedRequests) {
                ParticipationRequestDto requestDto = RequestMapper.toParticipationRequestDto(request);
                confirmedRequestsList.add(requestDto);
            }
            if (isFull) {
                List<Request> listRequest = requestRepository.findAllByStatusAndEventId(Status.PENDING, eventId);
                listRequest.forEach(request -> request.setStatus(Status.REJECTED));
                listRequest = requestRepository.saveAll(listRequest);
                for (Request request : listRequest) {
                    ParticipationRequestDto requestDto = RequestMapper.toParticipationRequestDto(request);
                    rejectedRequestsList.add(requestDto);
                }
            }
        } else if (updateRequest.getStatus().equals(Status.REJECTED)) {
            for (Request request : savedRequests) {
                ParticipationRequestDto requestDto = RequestMapper.toParticipationRequestDto(request);
                rejectedRequestsList.add(requestDto);
            }
        }
        event.setConfirmedRequests(event.getConfirmedRequests() + confirmedRequestsList.size());
        eventRepository.save(event);
        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmedRequestsList)
                .rejectedRequests(rejectedRequestsList)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getAll(Long userId) {
        getUser(userId);
        return requestRepository.findAllByRequesterId(userId).stream()
                .map(RequestMapper::toParticipationRequestDto)
                .toList();
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancel(Long userId, Long requestId) {
        getUser(userId);
        Optional<Request> requestOptional = requestRepository.findById(requestId);
        if (requestOptional.isEmpty()) {
            throw new NotFoundException("Запрос с id: " + requestId + " не найден!");
        }
        Request request = requestOptional.get();
        if (request.getStatus().equals(Status.CONFIRMED)) {
            Event event = request.getEvent();
            event.setConfirmedRequests(event.getConfirmedRequests() - 1);
            eventRepository.save(event);
        }
        request.setStatus(Status.CANCELED);
        return RequestMapper.toParticipationRequestDto(requestRepository.save(request));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getAllByEvent(Long userId, Long eventId) {
        getUser(userId);
        getEvent(eventId);
        return requestRepository.findAllByEventId(eventId).stream()
                .map(RequestMapper::toParticipationRequestDto)
                .toList();
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
}
