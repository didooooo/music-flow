package app.payment;

import app.exception.PaymentMethodDeclinedException;
import app.order.model.Order;
import app.payment.model.BankTransfer;
import app.payment.model.CreditCard;
import app.payment.model.Payment;
import app.payment.repository.PaymentRepository;
import app.payment.service.*;
import app.payment.model.Paypal;
import app.payment.repository.PaypalRepository;
import app.payment.service.PaypalService;
import app.web.dto.PaymentBankTransferRequest;
import app.web.dto.PaymentCreditCardRequest;
import app.web.dto.PaymentPayPalRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceUTest {
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private BankTransferService bankTransferService;
    @Mock
    private PaypalService paypalService;
    @Mock
    private CreditCardService creditCardService;
    @InjectMocks
    private PaymentService paymentService;
    @Test
    void givenBankTransferRequest_whenCreatePayment_thenReturnSavedPayment() {
        // Given
        Order order = new Order();
        PaymentBankTransferRequest bankTransferRequest = PaymentBankTransferRequest.builder().iban("IBAN123").build();
        BankTransfer bankTransfer = BankTransfer.builder().iban("IBAN123").build();
        Payment payment = Payment.builder().bankTransfer(bankTransfer).order(order).build();

        when(bankTransferService.createNewBankTransferAccount(bankTransferRequest)).thenReturn(bankTransfer);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // When
        Payment result = paymentService.createPayment(order, bankTransferRequest, null,null);

        // Then
        assertEquals(payment, result);
        verify(paymentRepository,times(1)).save(any(Payment.class));
    }

    @Test
    void givenCreditCardRequest_whenCreatePayment_thenReturnSavedPayment() {
        // Given
        Order order = new Order();
        PaymentCreditCardRequest creditCardRequest = PaymentCreditCardRequest.builder().cardNumber("1234567812345678").build();
        CreditCard creditCard = CreditCard.builder().lastForDigit("5678").build();
        Payment payment = Payment.builder().creditCard(creditCard).order(order).build();

        when(creditCardService.createNewCreditCard(creditCardRequest)).thenReturn(creditCard);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // When
        Payment result = paymentService.createPayment(order, new PaymentBankTransferRequest(), creditCardRequest, new PaymentPayPalRequest());

        // Then
        assertEquals(payment, result);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void givenPayPalRequest_whenCreatePayment_thenReturnSavedPayment() {
        // Given
        Order order = new Order();
        PaymentPayPalRequest payPalRequest = new PaymentPayPalRequest("test@example.com");
        Paypal paypal = Paypal.builder().email("test@example.com").build();
        Payment payment = Payment.builder().paypal(paypal).order(order).build();

        when(paypalService.createNewPaypalAccount(payPalRequest)).thenReturn(paypal);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // When
        Payment result = paymentService.createPayment(order, new PaymentBankTransferRequest(), new PaymentCreditCardRequest(), payPalRequest);

        // Then
        assertEquals(payment, result);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void givenNoValidPaymentRequest_whenCreatePayment_thenThrowException() {
        // Given
        Order order = new Order();
        PaymentBankTransferRequest bankTransferRequest = PaymentBankTransferRequest.builder().iban(null).build();
        PaymentCreditCardRequest creditCardRequest = PaymentCreditCardRequest.builder().cardNumber(null).build();
        PaymentPayPalRequest payPalRequest = new PaymentPayPalRequest(null);

        // When & Then
        assertThrows(PaymentMethodDeclinedException.class, () ->
                paymentService.createPayment(order, bankTransferRequest, creditCardRequest, payPalRequest)
        );
    }
}
