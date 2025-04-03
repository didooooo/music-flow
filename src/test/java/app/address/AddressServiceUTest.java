package app.address;

import app.address.model.Address;
import app.address.repository.AddressRepository;
import app.address.service.AddressService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AddressServiceUTest {
    @Mock
    private AddressRepository addressRepository;
    @InjectMocks
    private AddressService addressService;

    @Test
    void givenExistingAddress_whenSave_thenReturnExistingAddress() {
        // Given
        String street = "123 Main St";
        String city = "Springfield";
        String state = "IL";
        String zip = "62704";
        Address existingAddress = new Address(UUID.randomUUID(), street, city, state, zip);
        when(addressRepository.findAddressByStreetAndCity(street, city)).thenReturn(Optional.of(existingAddress));

        // When
        Address result = addressService.save(street, city, state, zip);

        // Then
        assertEquals(existingAddress, result);
        verify(addressRepository, never()).save(any());
    }
    @Test
    void givenNewAddress_whenSave_thenSaveAndReturnNewAddress() {
        // Given
        String street = "456 Elm St";
        String city = "Metropolis";
        String state = "NY";
        String zip = "10001";
        Address newAddress = new Address(UUID.randomUUID(),street, city, state, zip);
        when(addressRepository.findAddressByStreetAndCity(street, city)).thenReturn(Optional.empty());
        when(addressRepository.save(any(Address.class))).thenReturn(newAddress);

        // When
        Address result = addressService.save(street, city, state, zip);

        // Then
        assertEquals(newAddress, result);
        verify(addressRepository).save(any(Address.class));
    }
}
