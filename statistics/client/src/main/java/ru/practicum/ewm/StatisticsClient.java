package ru.practicum.ewm;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;



@Service
@Slf4j
public class StatisticsClient {
    private final RestTemplate restTemplate;
    private final String serverUrl;
    private static final String SAVE_SUFFIX = "/hit";
    private static final String STATS_SUFFIX = "/stats";


    @Autowired
    public StatisticsClient(@Value("${statistics-server.url}") String serverUrl) {
        restTemplate = new RestTemplate();
        this.serverUrl = serverUrl;
    }

    public void save(EndpointHitDto endpointHitDto) {
        log.trace("Получен запрос на сохранения:" + endpointHitDto);
        String url = serverUrl + SAVE_SUFFIX;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EndpointHitDto> request = new HttpEntity<>(endpointHitDto, headers);
        try {
            ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.POST, request, Void.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                log.trace("Запрос успешно выполнин!");
            } else {
                log.warn("Произошла ошибка! Код ошибки: " + response.getStatusCode());
            }
        } catch (HttpStatusCodeException e) {
            log.warn("Произошла ошибка! Статус ошибки: " + e.getStatusCode() + " тело ответа: "
                    + e.getResponseBodyAsString());
            throw new RuntimeException("Не удалось сохранить данные!", e);
        } catch (Exception e) {
            log.warn("Произошла ошибка при сохранении: " + e);
            throw new RuntimeException("Не удалось сохранить данные!", e);
        }
    }

    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        log.trace("Получен запрос на получения статистики!");
        String url = UriComponentsBuilder.fromHttpUrl(serverUrl + STATS_SUFFIX)
                .queryParam("start", dateTimeEncoding(start))
                .queryParam("end", dateTimeEncoding(end))
                .queryParam("uris", String.join(",", uris))
                .queryParamIfPresent("unique", Optional.ofNullable(unique))
                .toUriString();
        try {
            ResponseEntity<List<ViewStatsDto>> response = restTemplate.exchange(url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<>() {
                    });
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.trace("Запрос успешно выполнин!");
                return response.getBody();
            } else {
                log.warn("Произошла ошибка! Статус ошибки: " + response.getStatusCode());
                return Collections.emptyList();
            }
        } catch (HttpStatusCodeException e) {
            log.warn("Произошла ошибка! Статус ошибки: " + e.getStatusCode() + " тело ответа: "
                    + e.getResponseBodyAsString());
            return Collections.emptyList();
        } catch (Exception e) {
            log.warn("Произошла непредвиденная ошибка!");
            throw new RuntimeException("Не удалось получить статистику!", e);
        }
    }

    private String dateTimeEncoding(LocalDateTime date) {
        String str = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return URLEncoder.encode(str, StandardCharsets.UTF_8)
                .replace("+", " ")
                .replace("%3A", ":");
    }
}
