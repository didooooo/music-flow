package app.payment.service;

import app.payment.model.Paypal;
import app.payment.repository.CreditCardRepository;
import app.payment.repository.PaypalRepository;
import app.web.dto.PaymentPayPalRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PaypalService {
    private final PaypalRepository paypalRepository;

    public PaypalService(PaypalRepository paypalRepository) {
        this.paypalRepository = paypalRepository;
    }

    public Paypal createNewPaypalAccount(PaymentPayPalRequest payPalRequest) {
        Optional<Paypal> paypal = paypalRepository.findPaypalByEmail(payPalRequest.getPaypalEmail());
        if(paypal.isPresent()) {
            return paypal.get();
        }
        return paypalRepository.save(Paypal.builder().email(payPalRequest.getPaypalEmail()).build());
    }
}
