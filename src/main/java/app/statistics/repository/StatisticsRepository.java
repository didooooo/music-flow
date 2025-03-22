package app.statistics.repository;

import app.statistics.model.Statistics;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface StatisticsRepository extends CrudRepository<Statistics, UUID> {
    Optional<Statistics> findFirstByDate(LocalDate date);

    List<Statistics> findAllByOrderByDateDesc();
}
