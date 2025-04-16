package ru.practicum.ewm.compilation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.event.dto.EventShortDto;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class CompilationDto {
    private List<EventShortDto> events;
    private Long id;
    private Boolean pinned;
    private String title;
}
