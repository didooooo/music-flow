package app.web.converters;

import app.user.model.User;
import app.web.dto.EditAccountRequest;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class UserToEditAccountRequest implements Converter<User, EditAccountRequest> {
    @Override
    public EditAccountRequest convert(User source) {
        return EditAccountRequest.builder()
                .firstName(source.getFirstName())
                .lastName(source.getLastName())
                .email(source.getEmail())
                .phone(source.getPhone())
                .zip(source.getAddress().getZip())
                .state(source.getAddress().getState())
                .city(source.getAddress().getCity())
                .street(source.getAddress().getStreet())
                .build();
    }
}
