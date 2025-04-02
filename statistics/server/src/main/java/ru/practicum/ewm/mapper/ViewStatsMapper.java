package ru.practicum.ewm.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.ViewStatsDto;
import ru.practicum.ewm.model.ViewStats;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ViewStatsMapper {
    public static ViewStatsDto toViewStatsDto(ViewStats viewStats) {
        return ViewStatsDto.builder()
                .app(viewStats.getApp())
                .uri(viewStats.getUri())
                .hits(viewStats.getHits())
                .build();
    }

    public static ViewStats toViewStats(ViewStatsDto viewStatsDto) {
        ViewStats viewStats = new ViewStats();
        viewStats.setApp(viewStatsDto.getApp());
        viewStats.setUri(viewStatsDto.getUri());
        viewStats.setHits(viewStatsDto.getHits());
        return viewStats;
    }
}
