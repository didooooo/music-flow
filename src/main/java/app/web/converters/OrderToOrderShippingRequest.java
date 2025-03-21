package app.web.converters;

import app.order.model.Order;
import app.web.dto.OrderShippingRequest;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class OrderToOrderShippingRequest implements Converter<Order, OrderShippingRequest> {
    @Override
    public OrderShippingRequest convert(Order order) {
        return OrderShippingRequest.builder()
                .zip(order.getAddress().getZip())
                .city(order.getAddress().getCity())
                .state(order.getAddress().getState())
                .firstName(order.getFirstName())
                .lastName(order.getLastName())
                .phone(order.getPhone())
                .street(order.getAddress().getStreet())
                .email(order.getEmail())
                .orderId(order.getId())
                .build();
    }
}
