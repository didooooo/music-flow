package app.payment;

import app.payment.model.BankTransfer;
import app.payment.repository.BankTransferRepository;
import app.payment.service.BankTransferService;
import app.web.dto.PaymentBankTransferRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.ConversionService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
public class BankTransferServiceUTest {
    @Mock
    private BankTransferRepository bankTransferRepository;
    @Mock
    private ConversionService conversionService;
    @InjectMocks
    private BankTransferService bankTransferService;
    @Test
    void givenExistingBankAccount_whenCreateNewBankTransferAccount_thenReturnExisting() {
        // Given
        PaymentBankTransferRequest request = PaymentBankTransferRequest.builder().iban("Iban123").build();
        BankTransfer existingBankTransfer = BankTransfer.builder().iban("Iban123").build();

        when(bankTransferRepository.findByIban(request.getIban())).thenReturn(Optional.of(existingBankTransfer));

        // When
        BankTransfer result = bankTransferService.createNewBankTransferAccount(request);

        // Then
        assertEquals(existingBankTransfer, result);
        verify(bankTransferRepository, never()).save(any());
    }

    @Test
    void givenNonExistingBankAccount_whenCreateNewBankTransferAccount_thenSaveNew() {
        // Given
        PaymentBankTransferRequest request = PaymentBankTransferRequest.builder().iban("Iban123").build();
        BankTransfer newBankTransfer = BankTransfer.builder().iban("Iban123").build();

        when(bankTransferRepository.findByIban(request.getIban())).thenReturn(Optional.empty());
        when(conversionService.convert(request, BankTransfer.class)).thenReturn(newBankTransfer);
        when(bankTransferRepository.save(newBankTransfer)).thenReturn(newBankTransfer);

        // When
        BankTransfer result = bankTransferService.createNewBankTransferAccount(request);

        // Then
        assertEquals(newBankTransfer, result);
        verify(bankTransferRepository,times(1)).save(newBankTransfer);
    }
}
