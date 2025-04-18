package ru.practicum.ewm.category.service;

import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto save(NewCategoryDto newCategoryDto);

    CategoryDto update(Long id, CategoryDto newCategoryDto);

    CategoryDto get(Long id);

    List<CategoryDto> getAll(int from, int size);

    void delete(Long id);
}
