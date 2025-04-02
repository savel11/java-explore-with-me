package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.EndpointHitDto;
import ru.practicum.ewm.ViewStatsDto;
import ru.practicum.ewm.exception.InvalidFormatException;
import ru.practicum.ewm.exception.NullDateException;
import ru.practicum.ewm.mapper.EndpointHitMapper;
import ru.practicum.ewm.mapper.ViewStatsMapper;
import ru.practicum.ewm.storage.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {
    private final StatsRepository statsRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ViewStatsDto> getStatistics(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        log.trace("Получен запрос к сервесу статистики");
        if (start == null || end == null) {
            log.warn("Даты начала и конца поиска обязательны для запроса статистики!");
            throw new NullDateException("Необходимо указать период за который нужно получить статистику!");
        }
        if (end.isBefore(start)) {
            log.warn("Неверно указаны даты поиска: конец не может быть раньше начала!");
            throw new InvalidFormatException("Неверный формат периода: Укажите положительный период поиска!");
        }
        if (unique) {
            log.trace("Получаем статистику уникальных запросов за период времени с " + start + " по" + end);
            if (uris.isEmpty()) {
                log.trace("Учитываем в статистики все пути");
                return statsRepository.findAllUniqueHits(start, end).stream()
                        .map(ViewStatsMapper::toViewStatsDto)
                        .toList();

            }
            log.trace("Учитываем в статистики пути: " + uris);
            return statsRepository.findAllUniqueHitsInUris(start, end, uris).stream()
                    .map(ViewStatsMapper::toViewStatsDto)
                    .toList();
        }
        log.trace("Получаем статистику всех запросов за период времени с " + start + " по" + end);
        if (uris.isEmpty()) {
            log.trace("Учитываем в статистики все пути");
            return statsRepository.findAllHits(start, end).stream()
                    .map(ViewStatsMapper::toViewStatsDto)
                    .toList();

        }
        log.trace("Учитываем в статистики пути: " + uris);
        return statsRepository.findAllHitsInUris(start, end, uris).stream()
                .map(ViewStatsMapper::toViewStatsDto)
                .toList();
    }

    @Override
    @Transactional
    public void save(EndpointHitDto endpointHitDto) {
        log.trace("Сохранение запроса:" + endpointHitDto);
        statsRepository.save(EndpointHitMapper.toEndpointHit(endpointHitDto));
    }
}
