package app.shipment.service;

import app.shipment.model.Shipment;
import app.shipment.repository.ShipmentRepository;
import org.springframework.stereotype.Service;

@Service
public class ShipmentService {
    private final ShipmentRepository shipmentRepository;

    public ShipmentService(ShipmentRepository shipmentRepository) {
        this.shipmentRepository = shipmentRepository;
    }
    public Shipment upsertShipment(Shipment shipment) {
        return shipmentRepository.save(shipment);
    }
}
