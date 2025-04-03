package app.payment.service;

import app.exception.PaymentMethodDeclinedException;
import app.order.model.Order;
import app.payment.model.BankTransfer;
import app.payment.model.CreditCard;
import app.payment.model.Payment;
import app.payment.model.Paypal;
import app.payment.repository.PaymentRepository;
import app.web.dto.PaymentBankTransferRequest;
import app.web.dto.PaymentCreditCardRequest;
import app.web.dto.PaymentPayPalRequest;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final BankTransferService bankTransferService;
    private final PaypalService paypalService;
    private final CreditCardService creditCardService;

    public PaymentService(PaymentRepository paymentRepository, BankTransferService bankTransferService, PaypalService paypalService, CreditCardService creditCardService) {
        this.paymentRepository = paymentRepository;
        this.bankTransferService = bankTransferService;
        this.paypalService = paypalService;
        this.creditCardService = creditCardService;
    }

    public Payment createPayment(Order order, PaymentBankTransferRequest paymentBankTransferRequest, PaymentCreditCardRequest paymentCreditCardRequest, PaymentPayPalRequest payPalRequest) {
        if (paymentBankTransferRequest.getIban() != null) {
            BankTransfer bankTransfer = bankTransferService.createNewBankTransferAccount(paymentBankTransferRequest);
            Payment payment = Payment.builder().bankTransfer(bankTransfer).order(order).build();
            return paymentRepository.save(payment);
        } else if (paymentCreditCardRequest.getCardNumber() != null) {
            CreditCard creditCard = creditCardService.createNewCreditCard(paymentCreditCardRequest);
            Payment payment = Payment.builder().creditCard(creditCard).order(order).build();
            return paymentRepository.save(payment);
        } else if (payPalRequest.getPaypalEmail() != null) {
            Paypal paypal = paypalService.createNewPaypalAccount(payPalRequest);
            Payment payment = Payment.builder().paypal(paypal).order(order).build();
            return paymentRepository.save(payment);
        }
        throw new PaymentMethodDeclinedException("Payment not created. Have to choose between these three methods");
    }
}
