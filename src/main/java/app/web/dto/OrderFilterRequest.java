package app.web.dto;

import app.order.model.OrderStatus;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class OrderFilterRequest {
    private String productsName;
    private OrderStatus orderStatus;
    private LocalDate fromDate;
    private LocalDate toDate;
}
