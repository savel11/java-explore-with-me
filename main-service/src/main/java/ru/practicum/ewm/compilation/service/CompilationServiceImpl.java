package ru.practicum.ewm.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationRequest;
import ru.practicum.ewm.compilation.mapper.CompilationMapper;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.compilation.storage.CompilationRepository;
import ru.practicum.ewm.error.exeptions.NotFoundException;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.storage.EventRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CompilationDto create(NewCompilationDto newCompilationDto) {
        log.info("Создание новой подборки событий");
        List<Event> events = new ArrayList<>();
        if (newCompilationDto.getEvents() != null && !newCompilationDto.getEvents().isEmpty()) {
            events = eventRepository.findAllByIdIn(newCompilationDto.getEvents());
        }
        Compilation compilation = compilationRepository.save(CompilationMapper.toCompilation(newCompilationDto, events));
        List<EventShortDto> eventsDto = events.stream()
                .map(EventMapper::toEventShortDto)
                .toList();
        return CompilationMapper.toCompilationDto(compilation, eventsDto);
    }

    @Override
    @Transactional
    public CompilationDto update(Long compId, UpdateCompilationRequest updateDto) {
        log.info("Обновелние подборки событий с id = " + compId);
        Compilation compilation = getCompilation(compId);
        if (updateDto.getEvents() != null) {
            compilation.setEvents(eventRepository.findAllByIdIn(updateDto.getEvents()));
        }
        if (updateDto.getPinned() != null) {
            compilation.setPinned(updateDto.getPinned());
        }
        if (updateDto.getTitle() != null) {
            compilation.setTitle(updateDto.getTitle());
        }
        List<EventShortDto> eventsDto = compilation.getEvents().stream()
                .map(EventMapper::toEventShortDto)
                .toList();
        return CompilationMapper.toCompilationDto(compilationRepository.save(compilation), eventsDto);
    }

    @Override
    @Transactional(readOnly = true)
    public CompilationDto getById(Long compId) {
        log.info("Получение подборки с id = " + compId);
        Compilation compilation = getCompilation(compId);
        List<EventShortDto> events = compilation.getEvents().stream()
                .map(EventMapper::toEventShortDto)
                .toList();
        return CompilationMapper.toCompilationDto(compilation, events);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompilationDto> getAll(Boolean isPinned, int from, int size) {
        PageRequest pageRequest = PageRequest.of(from, size);
        List<Compilation> compilations;
        if (isPinned != null) {
            compilations = compilationRepository.findByPinned(isPinned, pageRequest);
        } else {
            compilations = compilationRepository.findAll(pageRequest).getContent();
        }
        if (compilations.isEmpty()) {
            return Collections.emptyList();
        }
        return compilations.stream()
                .map(compilation -> CompilationMapper.toCompilationDto(compilation, compilation.getEvents().stream()
                        .map(EventMapper::toEventShortDto)
                        .toList()))
                .toList();
    }

    @Override
    @Transactional
    public void delete(Long compId) {
        log.info("Удаление подборки с id = " + compId);
        getCompilation(compId);
        compilationRepository.deleteById(compId);
    }

    private Compilation getCompilation(Long id) {
        Optional<Compilation> compilationOp = compilationRepository.findById(id);
        if (compilationOp.isEmpty()) {
            log.warn("Подборки с id = " + id + " не существует!");
            throw new NotFoundException("Не удалось обновить подборку: Подборки с id = " + id + " не существует!");
        }
        return compilationOp.get();
    }
}
