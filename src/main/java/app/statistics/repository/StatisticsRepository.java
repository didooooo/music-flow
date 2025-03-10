package app.statistics.repository;

import app.statistics.model.Statistics;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface StatisticsRepository extends CrudRepository<Statistics, UUID> {
}
