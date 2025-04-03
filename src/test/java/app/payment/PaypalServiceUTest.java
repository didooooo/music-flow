package app.payment;

import app.payment.model.Paypal;
import app.payment.repository.PaypalRepository;
import app.payment.service.PaypalService;
import app.web.dto.PaymentPayPalRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
public class PaypalServiceUTest {
    @Mock
    private PaypalRepository paypalRepository;
    @InjectMocks
    private PaypalService paypalService;
    @Test
    void givenExistingPaypalEmail_whenCreateNewPaypalAccount_thenReturnExisting() {
        // Given
        PaymentPayPalRequest request = new PaymentPayPalRequest("test@example.com");
        Paypal existingPaypal = Paypal.builder().email("test@example.com").build();

        when(paypalRepository.findPaypalByEmail(request.getPaypalEmail())).thenReturn(Optional.of(existingPaypal));

        // When
        Paypal result = paypalService.createNewPaypalAccount(request);

        // Then
        assertEquals(existingPaypal, result);
        verify(paypalRepository, never()).save(any());
    }

    @Test
    void givenNonExistingPaypalEmail_whenCreateNewPaypalAccount_thenSaveNew() {
        // Given
        PaymentPayPalRequest request = new PaymentPayPalRequest("test@example.com");
        Paypal newPaypal = Paypal.builder().email("test@example.com").build();

        when(paypalRepository.findPaypalByEmail(request.getPaypalEmail())).thenReturn(Optional.empty());
        when(paypalRepository.save(any(Paypal.class))).thenReturn(newPaypal);

        // When
        Paypal result = paypalService.createNewPaypalAccount(request);

        // Then
        assertEquals(newPaypal, result);
        verify(paypalRepository).save(any(Paypal.class));
    }
}
