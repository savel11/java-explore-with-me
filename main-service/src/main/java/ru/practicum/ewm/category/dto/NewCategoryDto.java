package ru.practicum.ewm.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class NewCategoryDto {
    @NotBlank(message = "Некорректный формат имени: Имя не должен быть пустым.")
    @Size(min = 1, max = 50, message = "Некорректный формат имени: Имя должно содержать от 1 до 50 символов")
    private String name;
}
