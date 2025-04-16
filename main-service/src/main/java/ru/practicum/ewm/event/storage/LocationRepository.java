package ru.practicum.ewm.event.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.event.model.Location;

import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {
    Optional<Location> findByLonAndLat(Float lon, Float lat);
}
