package ru.practicum.ewm.compilation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class NewCompilationDto {
    List<Long> events;
    Boolean pinned;
    @NotBlank(message = "Некорректный формат заголовка: Заголовок не должен быть пустым.")
    @Size(min = 1, max = 50, message = "Некорректный формат заголовка: Заголовок должен содержать от 1 до 50 символов")
    String title;
}
