package app.statistics;

import app.statistics.model.Statistics;
import app.statistics.service.StatisticInit;
import app.statistics.service.StatisticService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StatisticsInitUTest {
    @Mock
    private StatisticService statisticService;
    @InjectMocks
    private StatisticInit statisticInit;
    //@Override
    //    public void run(String... args) throws Exception {
    //        List<Statistics> all = statisticService.getAll();
    //        if(all.isEmpty()){
    //            Statistics statistics = Statistics.builder()
    //                    .totalCustomers(0)
    //                    .date(LocalDate.now())
    //                    .totalMoney(BigDecimal.ZERO)
    //                    .totalRecords(0)
    //                    .totalOrders(0)
    //                    .activeUsers(0)
    //                    .inactiveUsers(0)
    //                    .pendingOrders(0)
    //                    .shippedOrders(0)
    //                    .build();
    //            statisticService.save(statistics);
    //            return;
    //        }
    //        Statistics first = all.getFirst();
    //        Statistics statistics = Statistics.builder()
    //                .shippedOrders(first.getShippedOrders())
    //                .date(LocalDate.now())
    //                .totalMoney(first.getTotalMoney())
    //                .inactiveUsers(first.getInactiveUsers())
    //                .activeUsers(first.getActiveUsers())
    //                .totalRecords(first.getTotalRecords())
    //                .pendingOrders(first.getPendingOrders())
    //                .totalOrders(first.getTotalOrders())
    //                .id(first.getId())
    //                .totalCustomers(first.getTotalCustomers())
    //                .build();
    //        statisticService.save(statistics);
    //    }

    @Test
    void givenEmptyListForStatistics_whenRun_thenShouldSaveNewStatistics() throws Exception {
        //Given
        //When
        when(statisticService.getAll()).thenReturn(new ArrayList<>());
        statisticInit.run();

        verify(statisticService, times(1)).getAll();
        verify(statisticService, times(1)).save(any());

    }

    @Test
    void givenListForStatistics_whenRun_thenShouldSaveNewStatisticsBasedOnTheOldOne() throws Exception {
        //Given
        Statistics statistics = Statistics.builder()
                .totalCustomers(0)
                .date(LocalDate.now())
                .totalMoney(BigDecimal.ZERO)
                .totalRecords(0)
                .totalOrders(0)
                .activeUsers(0)
                .inactiveUsers(0)
                .pendingOrders(0)
                .shippedOrders(0)
                .build();

        //When
        when(statisticService.getAll()).thenReturn(List.of(statistics));
        statisticInit.run();

        verify(statisticService, times(1)).getAll();
        verify(statisticService, times(1)).save(any());

    }
}
