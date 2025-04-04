package app.payment.repository;

import app.payment.model.BankTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface BankTransferRepository extends JpaRepository<BankTransfer, UUID> {
    Optional<BankTransfer> findByIban(String iban);
}
