package app.web.converters;

import app.order.model.Order;
import app.web.dto.OrderShippingRequest;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class OrderShippingRequestToOrder implements Converter<OrderShippingRequest, Order> {
    @Override
    public Order convert(OrderShippingRequest source) {
        return Order.builder()
                .email(source.getEmail())
                .firstName(source.getFirstName())
                .lastName(source.getLastName())
                .phone(source.getPhone())
                .build();
    }
}
