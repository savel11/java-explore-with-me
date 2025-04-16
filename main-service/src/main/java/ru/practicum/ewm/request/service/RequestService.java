package ru.practicum.ewm.request.service;

import ru.practicum.ewm.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;

import java.util.List;

public interface RequestService {

    ParticipationRequestDto create(Long userId, Long eventId);

    EventRequestStatusUpdateResult update(Long userId, Long eventId, EventRequestStatusUpdateRequest updateRequest);

    List<ParticipationRequestDto> getAll(Long userId);

    ParticipationRequestDto cancel(Long userId, Long requestId);

    List<ParticipationRequestDto> getAllByEvent(Long userId, Long eventId);
}
