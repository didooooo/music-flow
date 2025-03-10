package app.payment.model;

import app.order.model.Order;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @OneToOne(cascade = CascadeType.ALL)
    private Paypal paypal;
    @OneToOne(cascade = CascadeType.ALL)
    private BankTransfer bankTransfer;
    @OneToOne(cascade = CascadeType.ALL)
    private CreditCard creditCard;
    @OneToOne
    private Order order;
}