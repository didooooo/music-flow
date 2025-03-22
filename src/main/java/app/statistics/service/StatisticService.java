package app.statistics.service;

import app.statistics.model.Statistics;
import app.statistics.repository.StatisticsRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class StatisticService {
    private final StatisticsRepository statisticsRepository;

    public StatisticService(StatisticsRepository statisticsRepository) {
        this.statisticsRepository = statisticsRepository;
    }

    public void save(Statistics statistics) {
        statisticsRepository.save(statistics);
    }
    public Statistics getStatisticsForToday() {
      return statisticsRepository.findFirstByDate(LocalDate.now()).orElseThrow(()->new RuntimeException("There is no statistic for today"));
    }
    public List<Statistics> getAll() {
       return statisticsRepository.findAllByOrderByDateDesc();
    }
}
