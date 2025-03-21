package app.payment.repository;

import app.payment.model.Paypal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface PaypalRepository extends JpaRepository<Paypal, UUID> {
    Optional<Paypal> findPaypalByEmail(String email);
}
