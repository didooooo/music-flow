package app.scheduler;

import app.order.model.Order;
import app.order.model.OrderStatus;
import app.order.service.OrderService;
import app.shipment.model.Shipment;
import app.shipment.service.ShipmentService;
import app.statistics.model.Statistics;
import app.statistics.service.StatisticService;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Component
public class OrderScheduler {
    private final OrderService orderService;
    private final ShipmentService shipmentService;
    private final StatisticService statisticService;

    public OrderScheduler(OrderService orderService, ShipmentService shipmentService, StatisticService statisticService) {
        this.orderService = orderService;
        this.shipmentService = shipmentService;
        this.statisticService = statisticService;
    }

    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    public void makeOrderShipped() {
        List<Order> allOrders = orderService.getAllOrders();
        Shipment shipment = Shipment.builder()
                .orders( new ArrayList<>())
                .build();
        Shipment savedShipment = shipmentService.upsertShipment(shipment);
        Statistics statisticsForToday = statisticService.getStatisticsForToday();
        for (Order order : allOrders) {
            LocalDateTime now = LocalDateTime.now();
            long days = ChronoUnit.DAYS.between(order.getUpdatedAt(), now);
            if (order.getStatus().equals(OrderStatus.CONFIRMED) && days >= 1) {
                order.setStatus(OrderStatus.SHIPPED);
                statisticsForToday.setShippedOrders(statisticsForToday.getShippedOrders() + 1);
                statisticService.save(statisticsForToday);
                order.setUpdatedAt(now);
                Order saved = orderService.save(order);
                savedShipment.getOrders().add(saved);
                shipmentService.upsertShipment(savedShipment);
            }
        }
    }
    @Scheduled(cron = "0 0 0 * * *")
    public void makeOrderDelivered() {
        List<Order> allOrders = orderService.getAllOrders();
        for (Order order : allOrders) {
            LocalDateTime now = LocalDateTime.now();
            long days = ChronoUnit.DAYS.between(order.getUpdatedAt(), now);
            if (order.getStatus().equals(OrderStatus.SHIPPED) && days >= 1) {
                order.setStatus(OrderStatus.DELIVERED);
                order.setUpdatedAt(now);
                Order saved = orderService.save(order);
            }
        }
    }
}
