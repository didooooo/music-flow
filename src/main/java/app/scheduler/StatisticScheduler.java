package app.scheduler;

import app.order.model.Order;
import app.order.model.OrderInfo;
import app.order.model.OrderStatus;
import app.order.service.OrderService;
import app.record.model.Record;
import app.record.service.RecordService;
import app.statistics.model.Statistics;
import app.statistics.service.StatisticService;
import app.user.model.User;
import app.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
public class StatisticScheduler {
    private final StatisticService statisticService;

    public StatisticScheduler(StatisticService statisticService) {
        this.statisticService = statisticService;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void addStatisticForADay() {
        List<Statistics> all = statisticService.getAll();
        Statistics first = all.getFirst();
        Statistics statistics = Statistics.builder()
                .shippedOrders(first.getShippedOrders())
                .date(LocalDate.now())
                .totalMoney(first.getTotalMoney())
                .inactiveUsers(first.getInactiveUsers())
                .activeUsers(first.getActiveUsers())
                .totalRecords(first.getTotalRecords())
                .id(first.getId())
                .pendingOrders(first.getPendingOrders())
                .totalOrders(first.getTotalOrders())
                .totalCustomers(first.getTotalCustomers())
                .build();
        statisticService.save(statistics);
    }
}
