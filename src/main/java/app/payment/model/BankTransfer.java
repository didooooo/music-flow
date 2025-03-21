package app.payment.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BankTransfer {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private String bank;
    @Column(nullable = false)
    private String accountHolder;
    @Column(nullable = false, unique = true)
    private String iban;
    @Column(nullable = false)
    private String bic;
}
