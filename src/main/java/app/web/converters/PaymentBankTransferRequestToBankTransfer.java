package app.web.converters;

import app.payment.model.BankTransfer;
import app.web.dto.PaymentBankTransferRequest;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class PaymentBankTransferRequestToBankTransfer implements Converter<PaymentBankTransferRequest, BankTransfer> {
    @Override
    public BankTransfer convert(PaymentBankTransferRequest source) {
        return BankTransfer.builder()
                .bank(source.getBankName())
                .bic(source.getBic())
                .accountHolder(source.getAccountHolderName())
                .iban(source.getIban())
                .build();
    }
}
