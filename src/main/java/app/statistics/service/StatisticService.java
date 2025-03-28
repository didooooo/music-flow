package app.statistics.service;

import app.order.model.Order;
import app.order.service.OrderService;
import app.statistics.model.Statistics;
import app.statistics.repository.StatisticsRepository;
import app.utils.DateUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.IsoFields;
import java.util.*;

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
        return statisticsRepository.findFirstByDate(LocalDate.now()).orElseThrow(() -> new RuntimeException("There is no statistic for today"));
    }

    public List<Statistics> getAll() {
        return statisticsRepository.findAllByOrderByDateDesc();
    }
}
