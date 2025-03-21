package app.web.converters;

import app.user.model.User;
import app.web.dto.OrderShippingRequest;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class UserToOrderShippingRequest implements Converter<User, OrderShippingRequest> {
    @Override
    public OrderShippingRequest convert(User source) {
        return OrderShippingRequest.builder()
                .phone(source.getPhone())
                .email(source.getEmail())
                .city(source.getAddress().getCity())
                .state(source.getAddress().getState())
                .firstName(source.getFirstName())
                .lastName(source.getLastName())
                .zip(source.getAddress().getZip())
                .street(source.getAddress().getStreet())
                .build();
    }
}
