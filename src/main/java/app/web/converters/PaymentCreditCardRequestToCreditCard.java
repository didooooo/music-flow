package app.web.converters;

import app.payment.model.CreditCard;
import app.web.dto.PaymentCreditCardRequest;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class PaymentCreditCardRequestToCreditCard implements Converter<PaymentCreditCardRequest, CreditCard> {
    @Override
    public CreditCard convert(PaymentCreditCardRequest source) {
        return CreditCard.builder()
                .name(source.getCardHolderName())
                .expirationMonth(source.getExpiryDate().substring(0,2))
                .expirationYear(source.getExpiryDate().substring(3,5))
                .lastForDigit(source.getCardNumber().substring(source.getCardNumber().length() - 4))
                .build();
    }
}
