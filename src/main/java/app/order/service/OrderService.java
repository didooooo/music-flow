package app.order.service;

import app.order.model.Order;
import app.order.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {
    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> getLastThreeOrders() {
        return orderRepository.findTop3ByOrderByCreatedAtDesc();
    }
}
