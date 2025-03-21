package app.web.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class PaymentCreditCardRequest {
    private String cardNumber;
    private String cardHolderName;
    private String expiryDate;
}
