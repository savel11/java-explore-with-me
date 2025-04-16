package ru.practicum.ewm.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.EndpointHitDto;
import ru.practicum.ewm.StatisticsClient;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.service.EventService;

import java.time.LocalDateTime;
import java.util.List;


@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventPublicController {
    private final EventService eventService;
    private final StatisticsClient statisticsClient;
    private static final String APP = "main-service";

    @GetMapping
    public List<EventShortDto> getAllPublish(@RequestParam(required = false) String text,
                                             @RequestParam(required = false) List<Long> categories,
                                             @RequestParam(required = false) Boolean paid,
                                             @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                             @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                             @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                             @RequestParam(required = false) String sort, @RequestParam(defaultValue = "0") int from,
                                             @RequestParam(defaultValue = "10") int size, HttpServletRequest request) {
        List<EventShortDto> events = eventService.getAllPublishEvents(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);
        statisticsClient.save(EndpointHitDto.builder()
                .app(APP)
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build());
        return events;
    }

    @GetMapping("/{id}")
    public EventFullDto getPublishById(@PathVariable("id") Long id, HttpServletRequest request) {
        EventFullDto event = eventService.getPublishEventById(id);
        statisticsClient.save(EndpointHitDto.builder()
                .app(APP)
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build());
        return event;
    }
}
