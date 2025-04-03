package app.shipment;

import app.shipment.model.Shipment;
import app.shipment.repository.ShipmentRepository;
import app.shipment.service.ShipmentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ShipmentServiceUTest {
    @Mock
    private ShipmentRepository shipmentRepository;
    @InjectMocks
    private ShipmentService shipmentService;
    @Test
    void givenShipment_whenUpsertShipment_thenReturnSavedShipment() {
        // Given
        Shipment shipment = Shipment.builder()
                .id(UUID.randomUUID())
                .build();

        Shipment savedShipment = Shipment.builder()
                .id(shipment.getId())
                .build();

        when(shipmentRepository.save(shipment)).thenReturn(savedShipment);

        // When
        Shipment result = shipmentService.upsertShipment(shipment);

        // Then
        assertNotNull(result);
        assertEquals(shipment.getId(), result.getId());

        verify(shipmentRepository, times(1)).save(shipment);
    }

}
