package ru.practicum.ewm.request.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.request.model.Status;

import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class EventRequestStatusUpdateRequest {
    private Set<Long> requestIds;
    private Status status;
}
