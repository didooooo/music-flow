package app.statistics.service;

import app.statistics.model.Statistics;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
public class StatisticInit implements CommandLineRunner {
    private final StatisticService statisticService;

    public StatisticInit(StatisticService statisticService) {
        this.statisticService = statisticService;
    }

    @Override
    public void run(String... args) throws Exception {
        List<Statistics> all = statisticService.getAll();
        if(all.isEmpty()){
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
            statisticService.save(statistics);
            return;
        }
        Statistics first = all.getFirst();
        Statistics statistics = Statistics.builder()
                .shippedOrders(first.getShippedOrders())
                .date(LocalDate.now())
                .totalMoney(first.getTotalMoney())
                .inactiveUsers(first.getInactiveUsers())
                .activeUsers(first.getActiveUsers())
                .totalRecords(first.getTotalRecords())
                .pendingOrders(first.getPendingOrders())
                .totalOrders(first.getTotalOrders())
                .id(first.getId())
                .totalCustomers(first.getTotalCustomers())
                .build();
        statisticService.save(statistics);
    }
}
