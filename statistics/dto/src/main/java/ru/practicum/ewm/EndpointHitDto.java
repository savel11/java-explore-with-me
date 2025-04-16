package ru.practicum.ewm;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EndpointHitDto {
    private Long id;
    @NotBlank(message = "Название сервиса не должно отсутствовать или быть пустым!")
    private String app;
    @NotBlank(message = "URI не должно отсутствовать или быть пустым!")
    private String uri;
    @NotBlank(message = "IP пользователя не должно отсутствовать или быть пустым!")
    private String ip;
    @NotNull(message = "Дата не должно отсутствовать!")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
}
