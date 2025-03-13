package app.address.service;

import app.address.model.Address;
import app.address.repository.AddressRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AddressService {
    private final AddressRepository addressRepository;

    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    public Address save(String street, String city, String state, String zip) {
        Optional<Address> addressByStreetAndCity = addressRepository.findAddressByStreetAndCity(street, city);
        if (addressByStreetAndCity.isPresent()) {
            return addressByStreetAndCity.get();
        }
        Address address = Address.builder()
                .street(street)
                .city(city)
                .state(state)
                .zip(zip)
                .build();
        return addressRepository.save(address);
    }
}
