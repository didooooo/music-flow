package app.statistics;

import app.exception.StatisticsForTodayDoesNotExist;
import app.statistics.model.Statistics;
import app.statistics.repository.StatisticsRepository;
import app.statistics.service.StatisticService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StatisticsServiceUTest {
    @Mock
    private StatisticsRepository statisticsRepository;
    @InjectMocks
    private StatisticService statisticService;

    @Test
    void givenStatistics_whenSave_thenSaveStatistics() {
        // Given
        Statistics statistics = new Statistics();

        // When
        statisticService.save(statistics);

        // Then
        verify(statisticsRepository, times(1)).save(statistics);
    }

    @Test
    void givenStatisticsForToday_whenGetStatisticsForToday_thenReturnStatistics() {
        // Given
        Statistics statistics = new Statistics();
        when(statisticsRepository.findFirstByDate(LocalDate.now())).thenReturn(Optional.of(statistics));

        // When
        Statistics result = statisticService.getStatisticsForToday();

        // Then
        assertNotNull(result);
        assertEquals(statistics, result);
        verify(statisticsRepository, times(1)).findFirstByDate(LocalDate.now());
    }
    @Test
    void givenNoStatisticsForToday_whenGetStatisticsForToday_thenThrowException() {
        // Given
        when(statisticsRepository.findFirstByDate(LocalDate.now())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(StatisticsForTodayDoesNotExist.class, () -> statisticService.getStatisticsForToday());
        verify(statisticsRepository, times(1)).findFirstByDate(LocalDate.now());
    }
    @Test
    void givenStatisticsList_whenGetAll_thenReturnSortedStatisticsList() {
        // Given
        Statistics stat1 = new Statistics();
        stat1.setDate(LocalDate.of(2024, 4, 1));

        Statistics stat2 = new Statistics();
        stat2.setDate(LocalDate.of(2024, 4, 2));

        List<Statistics> statisticsList = List.of(stat2, stat1);
        when(statisticsRepository.findAllByOrderByDateDesc()).thenReturn(statisticsList);

        // When
        List<Statistics> result = statisticService.getAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(stat2, result.get(0));
        assertEquals(stat1, result.get(1));
        verify(statisticsRepository, times(1)).findAllByOrderByDateDesc();
    }


}
