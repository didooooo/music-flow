package app.payment;

import app.payment.model.CreditCard;
import app.payment.repository.CreditCardRepository;
import app.payment.service.CreditCardService;
import app.web.dto.PaymentCreditCardRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.ConversionService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
public class CreditCardServiceUTest {
    @Mock
    private CreditCardRepository creditCardRepository;
    @Mock
    private ConversionService conversionService;
    @InjectMocks
    private CreditCardService creditCardService;
    @Test
    void whenCreateNewCreditCard_thenSaveNew() {
        // Given
        PaymentCreditCardRequest request = PaymentCreditCardRequest.builder()
                .cardNumber("1234567812345678")
                .build();
        CreditCard newCreditCard = CreditCard.builder().lastForDigit("5678").build();

        when(conversionService.convert(request, CreditCard.class)).thenReturn(newCreditCard);
        when(creditCardRepository.save(newCreditCard)).thenReturn(newCreditCard);

        // When
        CreditCard result = creditCardService.createNewCreditCard(request);

        // Then
        assertEquals(newCreditCard, result);
        verify(creditCardRepository,times(1)).save(newCreditCard);
    }
}
