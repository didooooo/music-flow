package app.payment.repository;

import app.payment.model.Paypal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface PaypalRepository extends JpaRepository<Paypal, UUID> {
}
