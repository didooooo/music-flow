package app.shopping_cart.model;

import app.record.model.Record;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShoppingCartInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne
    private Record record;
    @ManyToOne
    private ShoppingCart shoppingCart;
    private int quantity;
}
