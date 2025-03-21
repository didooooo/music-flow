package app.order.service;

import app.order.model.OrderInfo;
import app.order.repository.OrderInfoRepository;
import org.springframework.stereotype.Service;

@Service
public class OrderInfoService {
    private final OrderInfoRepository orderInfoRepository;

    public OrderInfoService(OrderInfoRepository orderInfoRepository) {
        this.orderInfoRepository = orderInfoRepository;
    }
    public OrderInfo save(OrderInfo orderInfo) {
        return orderInfoRepository.save(orderInfo);
    }
}
