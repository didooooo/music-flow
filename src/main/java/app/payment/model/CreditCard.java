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
public class CreditCard {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private String lastForDigit;
    @Column(nullable = false)
    private String expirationMonth;
    @Column(nullable = false)
    private String expirationYear;
    @Column(nullable = false)
    private String name;
}
