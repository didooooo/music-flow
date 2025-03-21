package app.web.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class PaymentBankTransferRequest {
    private String bankName;
    private String iban;
    private String accountHolderName;
    private String bic;
}
