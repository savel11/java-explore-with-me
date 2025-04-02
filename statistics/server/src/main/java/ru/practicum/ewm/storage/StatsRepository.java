package ru.practicum.ewm.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.model.EndpointHit;
import ru.practicum.ewm.model.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<EndpointHit, Long> {
    @Query("select new ru.practicum.ewm.model.ViewStats(endpoint.app, endpoint.uri, count(endpoint.id)) " +
            "from EndpointHit as endpoint " +
            "where (endpoint.timestamp between ?1 and ?2) " +
            "and (endpoint.uri in (?3)) " +
            "group by endpoint.app, endpoint.uri " +
            "order by count(endpoint.id) desc")
    List<ViewStats> findAllHitsInUris(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("select new ru.practicum.ewm.model.ViewStats(endpoint.app, endpoint.uri, count(DISTINCT endpoint.ip)) " +
            "from EndpointHit as endpoint " +
            "where (endpoint.timestamp between ?1 and ?2) " +
            "and (endpoint.uri in (?3)) " +
            "group by endpoint.app, endpoint.uri " +
            "order by count(DISTINCT endpoint.ip) desc")
    List<ViewStats> findAllUniqueHitsInUris(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("select new ru.practicum.ewm.model.ViewStats(endpoint.app, endpoint.uri, count(endpoint.id)) " +
            "from EndpointHit as endpoint " +
            "where (endpoint.timestamp between ?1 and ?2) " +
            "group by endpoint.app, endpoint.uri " +
            "order by count(endpoint.id) desc")
    List<ViewStats> findAllHits(LocalDateTime start, LocalDateTime end);

    @Query("select new ru.practicum.ewm.model.ViewStats(endpoint.app, endpoint.uri, count(DISTINCT endpoint.ip)) " +
            "from EndpointHit as endpoint " +
            "where (endpoint.timestamp between ?1 and ?2) " +
            "group by endpoint.app, endpoint.uri " +
            "order by count(DISTINCT endpoint.ip) desc")
    List<ViewStats> findAllUniqueHits(LocalDateTime start, LocalDateTime end);
}
