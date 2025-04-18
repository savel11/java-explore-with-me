package ru.practicum.ewm.comment.dto;

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
public class NewCommentDto {
    @NotBlank(message = "Некорректный формат комментария: Комментарий не должен быть пустым.")
    @Size(min = 1, max = 500, message = "Некорректный формат комментария: Комментарий должен содержать" +
            " от 1 до 500 символов")
    private String text;
}
