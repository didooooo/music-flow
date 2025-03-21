package app.payment.service;

import app.payment.model.CreditCard;
import app.payment.repository.BankTransferRepository;
import app.payment.repository.CreditCardRepository;
import app.payment.repository.PaypalRepository;
import app.web.dto.PaymentCreditCardRequest;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;

@Service
public class CreditCardService {
    private final CreditCardRepository creditCardRepository;
    private final ConversionService conversionService;

    public CreditCardService(CreditCardRepository creditCardRepository, ConversionService conversionService) {
        this.creditCardRepository = creditCardRepository;
        this.conversionService = conversionService;
    }

    public CreditCard createNewCreditCard(PaymentCreditCardRequest paymentCreditCardRequest) {
        CreditCard convert = conversionService.convert(paymentCreditCardRequest, CreditCard.class);
        return creditCardRepository.save(convert);
    }
}
