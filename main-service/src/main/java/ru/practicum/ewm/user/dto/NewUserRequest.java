package ru.practicum.ewm.user.dto;

import jakarta.validation.constraints.Email;
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
public class NewUserRequest {
    @NotBlank(message = "Некорректный формат имени: Имя не должен быть пустым.")
    @Size(min = 2, max = 250, message = "Некорректный формат имени: Имя должно содержать от 2 до 250 символов")
    private String name;
    @Email(regexp = ".+[@].+[\\.].+", message = "Некорректный формат электронной почты.")
    @NotBlank(message = "Некорректный формат почты: Почта не должена быть пустой.")
    @Size(min = 6, max = 254, message = "Некорректный формат почты: Почта должно содержать от 2 до 250 символов")
    private String email;
}
