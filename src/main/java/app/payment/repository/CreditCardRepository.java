package app.payment.repository;

import app.payment.model.CreditCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface CreditCardRepository extends JpaRepository<CreditCard, UUID> {
}
