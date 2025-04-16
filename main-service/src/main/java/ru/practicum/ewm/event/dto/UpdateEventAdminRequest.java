package ru.practicum.ewm.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.event.model.Location;
import ru.practicum.ewm.event.model.StateActionAdmin;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class UpdateEventAdminRequest {
    @Size(min = 20, max = 2000, message = "Некорректный формат аннотации: Аннотация должно содержать" +
            " от 20 до 2000 символов")
    private String annotation;
    private Long category;
    @Size(min = 20, max = 7000, message = "Некорректный формат описания: Описание должно содержать" +
            " от 20 до 7000 символов")
    private String description;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;
    private Location location;
    private Boolean paid;
    @PositiveOrZero(message = "Некорректный формат огранечения: Огранечение не может быть отрицательным!")
    private Integer participantLimit;
    private Boolean requestModeration;
    private StateActionAdmin stateAction;
    @Size(min = 3, max = 120, message = "Некорректный формат заголовка: Заголовок должен содержать" +
            " от 3 до 120 символов")
    private String title;
}
