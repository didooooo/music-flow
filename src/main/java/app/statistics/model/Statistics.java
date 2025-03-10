package app.statistics.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Statistics {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private BigDecimal totalMoney;
    private int totalCustomers;
    private int totalOrders;
    private int pendingOrders;
    private int shippedOrders;
    private int activeUsers;
    private int inactiveUsers;
    @Column(nullable = false)
    private LocalDate date;

}
