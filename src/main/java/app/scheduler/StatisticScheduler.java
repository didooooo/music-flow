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
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
public class StatisticScheduler {
    private final StatisticService statisticService;
    private final UserService userService;
    private final OrderService orderService;
    private final RecordService recordService;

    public StatisticScheduler(StatisticService statisticService, UserService userService, OrderService orderService, RecordService recordService) {
        this.statisticService = statisticService;
        this.userService = userService;
        this.orderService = orderService;
        this.recordService = recordService;
    }

    @Scheduled(cron = "0 * * * * *")
    public void addStatisticForADay() {
        List<User> userList = userService.getAllUsers();
        int totalUsers = userList.size();
        int totalActiveUsers = (int) userList.stream().filter(User::isActive).count();
        int totalInactiveUsers = totalUsers - totalActiveUsers;
        List<Order> orders = orderService.getAllOrders();
        int totalOrders = orders.size();
        int pendingOrders = (int) orders.stream()
                .filter(order -> order.getStatus().equals(OrderStatus.PENDING))
                .count();
        int shippedOrders = (int) orders.stream()
                .filter(order -> order.getStatus().equals(OrderStatus.SHIPPED))
                .count();
        List<Record> recordList = recordService.getAllRecords();
        int totalRecords = recordList.size();
        BigDecimal totalPrice = new BigDecimal(0);
        for (Order order : orders) {
            for (OrderInfo product : order.getProducts()) {
                int quantity = product.getQuantity();
                BigDecimal price = product.getRecord().getPrice();
                price = price.multiply(new BigDecimal(quantity));
                totalPrice = totalPrice.add(price);
            }
        }
        Statistics statistics = Statistics.builder()
                .activeUsers(totalActiveUsers)
                .inactiveUsers(totalInactiveUsers)
                .totalRecords(totalRecords)
                .totalMoney(totalPrice)
                .pendingOrders(pendingOrders)
                .shippedOrders(shippedOrders)
                .totalOrders(totalOrders)
                .totalCustomers(totalUsers)
                .date(LocalDate.now())
                .build();
        statisticService.save(statistics);
    }
}
