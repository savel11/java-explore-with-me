package ru.practicum.ewm.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.storage.CategoryRepository;
import ru.practicum.ewm.error.exeptions.DuplicatedDataException;
import ru.practicum.ewm.error.exeptions.InvalidFormatException;
import ru.practicum.ewm.error.exeptions.NotFoundException;
import ru.practicum.ewm.event.storage.EventRepository;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CategoryDto save(NewCategoryDto newCategoryDto) {
        log.info("Добавление новой категории: " + newCategoryDto);
        if (isExistCategoryByName(newCategoryDto.getName())) {
            log.warn("Категории с названием: " + newCategoryDto.getName() + " уже существует!");
            throw new DuplicatedDataException("Категория не добавленна: Категории с данным названием уже существует!");
        }
        Category category = categoryRepository.save(CategoryMapper.toCategory(newCategoryDto));
        log.info("Категория успешно создана!");
        return CategoryMapper.toCategoryDto(category);
    }

    @Override
    @Transactional
    public CategoryDto update(Long id, CategoryDto categoryDto) {
        log.info("Обновление категории с id = " + id);
        Category category = getCategoryById(id);
        if (isExistCategoryByName(categoryDto.getName())) {
            log.warn("Категории с названием: " + categoryDto.getName() + " уже существует!");
            throw new DuplicatedDataException("Категория не обновленна: Категории с данным названием уже существует!");
        }
        category.setName(categoryDto.getName());
        Category newCategory = categoryRepository.save(category);
        log.info("Категория успешно обновленна!");
        return CategoryMapper.toCategoryDto(newCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto get(Long id) {
        log.info("Получение категории с id = " + id);
        Category category = getCategoryById(id);
        log.info("Категория найдена");
        return CategoryMapper.toCategoryDto(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getAll(int from, int size) {
        PageRequest pageable = PageRequest.of(from, size);
        return categoryRepository.findAll(pageable).getContent().stream()
                .map(CategoryMapper::toCategoryDto)
                .toList();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Удаление категории с id = " + id);
        getCategoryById(id);
        if (eventRepository.existsByCategoryId(id)) {
            log.warn("Категория не может быть уделена: Существуют события с данной категорией!");
            throw new InvalidFormatException("Невозможно удалить категорию для которой существуют события!");
        }
        categoryRepository.deleteById(id);
        log.info("Категория успешно удалена!");
    }

    private boolean isExistCategoryByName(String name) {
        log.trace("Проверка уникальности название категории: " + name);
        Optional<Category> categoryOptional = categoryRepository.findByName(name);
        return categoryOptional.isPresent();
    }

    private Category getCategoryById(Long id) {
        Optional<Category> categoryOptional = categoryRepository.findById(id);
        if (categoryOptional.isEmpty()) {
            log.warn("Категории с id = " + id + " не существует!");
            throw new NotFoundException("Категория не найдена!");
        }
        return categoryOptional.get();
    }
}
